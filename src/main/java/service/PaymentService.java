package service;
 /**
 * Author: HE190438 Thân Bình Minh
 * Created: 2026-03-19
 */
import controller.VnpayConfig;
import dao.PaymentDAO;
import model.Order;
import model.Payment;
import model.Refund;
import util.VnpayUtil;
import java.text.NumberFormat;
import java.util.Locale;
 
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
 
/**
 * Orchestrates all payment use cases (UC-19 to UC-28).
 */
public class PaymentService {
 
    private final PaymentDAO           dao     = new PaymentDAO();
    private final NotificationService  notifSvc = new NotificationService();
    private final VnpayService  vnpay   = new VnpayService();
 
    // ── UC-19: Initiate Payment ────────────────────────────────────────────
 
    /**
     * Validate an existing order is eligible for payment initiation.
     * Returns null if valid, error message if not.
     */
    public String validateOrderForPayment(int orderId, int sessionUserId) throws SQLException {
        Order o = dao.getOrderById(orderId);
        if (o == null) return "Đơn hàng không tồn tại.";
        if (o.getUserId() != sessionUserId) return "Bạn không có quyền thanh toán đơn hàng này.";
        if (!"pending".equals(o.getStatus())) return "Đơn hàng này không ở trạng thái chờ thanh toán.";
        Payment existing = dao.getPaymentByOrderId(orderId);
        if (existing != null && "success".equals(existing.getStatus())) return "Đơn hàng đã được thanh toán.";
        return null; // valid
    }
 
    // ── UC-22: COD ─────────────────────────────────────────────────────────
 
    /**
     * Confirm COD payment: create payment record, update order to "confirmed".
     */
    public int processCod(int orderId) throws SQLException {
        Order o = dao.getOrderById(orderId);
        Payment payment = new Payment(orderId, "cod", o.getTotalAmount());
        payment.setStatus("pending"); // paid on delivery
        int pid = dao.createPayment(payment);
        dao.updateOrderStatus(orderId, "confirmed");
        notifSvc.notifyOrderConfirmed(o.getUserId(), orderId);
        return pid;
    }
 
    // ── UC-21: VNPay (Online Banking) ──────────────────────────────────────
 
    /**
     * Create pending payment record & return VNPay redirect URL.
     * The txnRef is stored as transaction_ref so we can match on return.
     */
    public String initiateVnpay(int orderId, String ipAddr) throws SQLException {
        Order o = dao.getOrderById(orderId);
        String txnRef = VnpayUtil.generateTxnRef();
 
        Payment payment = new Payment(orderId, "vnpay", o.getTotalAmount());
        payment.setStatus("pending");
        payment.setTransactionRef(txnRef);
        dao.createPayment(payment);
 
        return vnpay.buildPaymentUrl(orderId, txnRef, o.getTotalAmount(), ipAddr);
    }
 
    // ── UC-23: Confirm Payment Result ──────────────────────────────────────
 
    /**
     * Handle VNPay return callback.
     * Returns PaymentResult with success flag + payment record.
     */
    public PaymentResult handleVnpayReturn(Map<String, String[]> params) throws SQLException {
        // 1. Verify HMAC signature (FIX: was missing / wrong in original)
        boolean validSig = VnpayUtil.verifyChecksum(params, VnpayConfig.VNP_HASH_SECRET);
        if (!validSig) {
            return new PaymentResult(false, null, "Chữ ký không hợp lệ (invalid signature).");
        }
 
        String responseCode = param(params, "vnp_ResponseCode");
        String txnRef       = param(params, "vnp_TxnRef");
        String bankCode     = param(params, "vnp_BankCode");
        String bankTxnNo    = param(params, "vnp_BankTranNo");
        String payDate      = param(params, "vnp_PayDate");
        String vnpAmount    = param(params, "vnp_Amount");
        String orderInfo    = param(params, "vnp_OrderInfo");
 
        // 2. Find payment by txnRef
        Payment payment = dao.getPaymentByTxnRef(txnRef);
        if (payment == null) {
            return new PaymentResult(false, null, "Không tìm thấy giao dịch.");
        }
 
        // 3. Prevent double-processing
        if ("success".equals(payment.getStatus())) {
            return new PaymentResult(true, payment, "Giao dịch đã được xử lý trước đó.");
        }
 
        boolean success = "00".equals(responseCode);
        String  newStatus = success ? "success" : "failed";
 
        String gatewayJson = String.format(
            "{\"responseCode\":\"%s\",\"txnRef\":\"%s\",\"bankCode\":\"%s\"," +
            "\"bankTxnNo\":\"%s\",\"payDate\":\"%s\",\"amount\":\"%s\"}",
            responseCode, txnRef, bankCode, bankTxnNo, payDate, vnpAmount);
 
        LocalDateTime paidAt = success ? LocalDateTime.now() : null;
 
        // 4. Update payment
        dao.updatePaymentAfterCallback(payment.getPaymentId(), newStatus, txnRef, gatewayJson, paidAt);
 
        // 5. Update order
        String orderStatus = success ? "confirmed" : "pending";
        dao.updateOrderStatus(payment.getOrderId(), orderStatus);
 
        payment.setStatus(newStatus);

        // Gửi thông báo cho khách hàng
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        Order payOrder = dao.getOrderById(payment.getOrderId());
        int notifUserId = (payOrder != null) ? payOrder.getUserId() : 0;
        String amtStr = nf.format(payment.getAmount());
        if (success) {
            notifSvc.notifyPaymentSuccess(notifUserId, payment.getOrderId(),
                payment.getPaymentId(), txnRef, amtStr);
        } else {
            notifSvc.notifyPaymentFailed(notifUserId, payment.getOrderId(),
                payment.getPaymentId(), vnpayErrorMessage(responseCode));
        }

        String msg = success
            ? "Thanh toán thành công! Mã giao dịch: " + txnRef
            : vnpayErrorMessage(responseCode);
 
        return new PaymentResult(success, payment, msg);
    }
 
    // ── UC-26: Retry / Change Method ──────────────────────────────────────
 
    /**
     * Allow customer to retry with VNPay for an existing pending order.
     * Creates a new payment record (previous failed one stays in history).
     */
    public String retryVnpay(int orderId, int sessionUserId, String ipAddr) throws SQLException {
        String err = validateOrderForPayment(orderId, sessionUserId);
        if (err != null) return null; // invalid
 
        // Mark previous failed payment (if any) — already failed, leave it
        return initiateVnpay(orderId, ipAddr);
    }
 
    // ── UC-25: Refund ─────────────────────────────────────────────────────
 
    public void requestRefund(int paymentId, BigDecimal amount, String reason) throws SQLException {
        Payment p = dao.getPaymentByPaymentId(paymentId);
        if (p == null) {
            throw new IllegalArgumentException("Giao dịch không tồn tại.");
        }
        if (!"success".equals(p.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hoàn tiền giao dịch đã thanh toán thành công.");
        }
        if (amount.compareTo(p.getAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền hoàn không được vượt quá số tiền giao dịch.");
        }
        Refund r = new Refund(paymentId, amount, reason);
        dao.createRefund(r);
    }
 
    public void approveRefund(int refundId, int adminUserId) throws SQLException {
        dao.updateRefundStatus(refundId, "approved", adminUserId);
        Refund r = dao.getRefundById(refundId);
        if (r != null) {
            dao.updatePaymentAfterCallback(r.getPaymentId(), "refunded", null, null, null);
            dao.updateOrderStatus(r.getOrderId(), "refunded");
            Order refOrder = dao.getOrderById(r.getOrderId());
            if (refOrder != null) {
                java.text.NumberFormat nf2 = java.text.NumberFormat.getInstance(new java.util.Locale("vi","VN"));
                nf2.setMaximumFractionDigits(0);
                notifSvc.notifyRefundApproved(refOrder.getUserId(), r.getOrderId(),
                    r.getPaymentId(), nf2.format(r.getAmount()));
            }
        }
    }
 
    public void rejectRefund(int refundId, int adminUserId) throws SQLException {
        dao.updateRefundStatus(refundId, "rejected", adminUserId);
        Refund rr = dao.getRefundById(refundId);
        if (rr != null) {
            Order rrOrder = dao.getOrderById(rr.getOrderId());
            if (rrOrder != null) {
                notifSvc.notifyRefundRejected(rrOrder.getUserId(), rr.getOrderId(), rr.getPaymentId());
            }
        }
    }
 
    // ── UC-28: Cancel Payment ─────────────────────────────────────────────
 
    public void cancelPendingPayment(int orderId) throws SQLException {
        Payment p = dao.getPaymentByOrderId(orderId);
        if (p != null && "pending".equals(p.getStatus())) {
            dao.updatePaymentAfterCallback(p.getPaymentId(), "failed", null,
                "{\"note\":\"Cancelled by customer\"}", null);
            Order cancelOrder = dao.getOrderById(orderId);
            if (cancelOrder != null) {
                notifSvc.notifyOrderCancelled(cancelOrder.getUserId(), orderId);
            }
        }
        // Order remains "pending" (customer can retry)
    }
 
    // ── UC-24: View Transactions (admin) ──────────────────────────────────
 
    public List<Payment> getAllPayments(String statusFilter, String methodFilter,
                                        int page, int pageSize) throws SQLException {
        return dao.getAllPayments(statusFilter, methodFilter, pageSize, (page - 1) * pageSize);
    }
 
    public int countAllPayments(String statusFilter, String methodFilter) throws SQLException {
        return dao.countAllPayments(statusFilter, methodFilter);
    }
 
    // ── UC-27: View Payment History (customer) ────────────────────────────
 
    public List<Payment> getPaymentHistory(int userId) throws SQLException {
        return dao.getPaymentsByUserId(userId);
    }
 
    // ── Refund management (admin) ─────────────────────────────────────────
 
    public List<Refund> getAllRefunds(String statusFilter) throws SQLException {
        return dao.getAllRefunds(statusFilter);
    }
 
    public PaymentDAO getDao() { return dao; }
 
    // ── Helpers ───────────────────────────────────────────────────────────
 
    private String param(Map<String, String[]> m, String k) {
        String[] v = m.get(k);
        return (v != null && v.length > 0) ? v[0] : "";
    }
 
    private String vnpayErrorMessage(String code) {
        return switch (code) {
            case "07" -> "Giao dịch bị nghi ngờ gian lận.";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ Internet Banking.";
            case "10" -> "Xác thực thông tin thẻ/tài khoản quá 3 lần.";
            case "11" -> "Hết thời gian chờ thanh toán.";
            case "12" -> "Thẻ/Tài khoản bị khóa.";
            case "13" -> "Sai mật khẩu OTP.";
            case "24" -> "Khách hàng hủy giao dịch.";
            case "51" -> "Tài khoản không đủ số dư.";
            case "65" -> "Tài khoản vượt hạn mức giao dịch.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Nhập sai mật khẩu quá số lần quy định.";
            default   -> "Thanh toán thất bại. Mã lỗi: " + code;
        };
    }
 
    /** Result of processing VNPay return */
    public static class PaymentResult {
        public final boolean success;
        public final Payment payment;
        public final String  message;
 
        public PaymentResult(boolean success, Payment payment, String message) {
            this.success = success;
            this.payment = payment;
            this.message = message;
        }
    }
}