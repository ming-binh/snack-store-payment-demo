package service;
/**
 * Author: HE190438 Thân Bình Minh
 * Created: 2026-03-19
 */
import dao.NotificationDAO;
import model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO dao = new NotificationDAO();

    // ── Trigger methods (gọi từ PaymentService) ────────────────────────

    public void notifyPaymentSuccess(int userId, int orderId, int paymentId, String txnRef, String amount) {
        send(new Notification(userId, "payment_success",
            "Thanh toán thành công",
            "Đơn hàng #" + orderId + " đã được thanh toán thành công." +
            (txnRef != null ? " Mã GD: " + txnRef + "." : "") +
            " Số tiền: " + amount + " ₫. Chúng tôi sẽ xử lý đơn hàng ngay!"),
            orderId, paymentId);
    }

    public void notifyPaymentFailed(int userId, int orderId, int paymentId, String reason) {
        send(new Notification(userId, "payment_failed",
            "Thanh toán thất bại",
            "Đơn hàng #" + orderId + " thanh toán không thành công." +
            (reason != null ? " Lý do: " + reason : "") +
            " Bạn có thể thử lại hoặc chọn phương thức khác."),
            orderId, paymentId);
    }

    public void notifyOrderConfirmed(int userId, int orderId) {
        send(new Notification(userId, "order_confirmed",
            "Đơn hàng đã xác nhận",
            "Đơn hàng #" + orderId + " đã được xác nhận thành công (COD)." +
            " Đơn hàng sẽ được giao trong 2–3 ngày làm việc."),
            orderId, 0);
    }

    public void notifyOrderCancelled(int userId, int orderId) {
        send(new Notification(userId, "order_cancelled",
            "Giao dịch đã hủy",
            "Giao dịch của đơn hàng #" + orderId + " đã được hủy theo yêu cầu của bạn." +
            " Nếu cần hỗ trợ, vui lòng liên hệ chúng tôi."),
            orderId, 0);
    }

    public void notifyRefundApproved(int userId, int orderId, int paymentId, String amount) {
        send(new Notification(userId, "refund_approved",
            "Yêu cầu hoàn tiền được duyệt",
            "Yêu cầu hoàn tiền cho đơn hàng #" + orderId + " đã được admin duyệt." +
            " Số tiền " + amount + " ₫ sẽ được hoàn về tài khoản trong 3–5 ngày làm việc."),
            orderId, paymentId);
    }

    public void notifyRefundRejected(int userId, int orderId, int paymentId) {
        send(new Notification(userId, "refund_rejected",
            "Yêu cầu hoàn tiền bị từ chối",
            "Rất tiếc, yêu cầu hoàn tiền cho đơn hàng #" + orderId +
            " không được chấp thuận. Vui lòng liên hệ hỗ trợ để biết thêm chi tiết."),
            orderId, paymentId);
    }

    // ── Customer-facing methods ────────────────────────────────────────

    public List<Notification> getByUser(int userId) throws SQLException {
        return dao.getByUserId(userId);
    }

    public int countUnread(int userId) throws SQLException {
        return dao.countUnread(userId);
    }

    public void markRead(int notificationId, int userId) throws SQLException {
        dao.markRead(notificationId, userId);
    }

    public void markAllRead(int userId) throws SQLException {
        dao.markAllRead(userId);
    }

    // ── Admin methods ──────────────────────────────────────────────────

    public List<Notification> getAll(String typeFilter, int page, int pageSize) throws SQLException {
        return dao.getAll(typeFilter, pageSize, (page - 1) * pageSize);
    }

    public int countAll(String typeFilter) throws SQLException {
        return dao.countAll(typeFilter);
    }

    // ── Internal helper ────────────────────────────────────────────────

    private void send(Notification n, int orderId, int paymentId) {
        n.setOrderId(orderId);
        n.setPaymentId(paymentId);
        try {
            dao.create(n);
        } catch (SQLException e) {
            // Không để lỗi notification làm fail luồng chính
            System.err.println("[NotificationService] Failed to save notification: " + e.getMessage());
        }
    }

    public List<Notification> getForUser(int userId) throws SQLException {
        return dao.getByUserId(userId);
    }
}
