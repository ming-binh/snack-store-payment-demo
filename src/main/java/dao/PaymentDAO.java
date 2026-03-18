package dao;
 
import model.Order;
import model.Payment;
import model.Refund;
import util.DBUtil;
 
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
public class PaymentDAO {
 
    // ═══════════════════════════════════════════════════════════════════════
    // ORDERS
    // ═══════════════════════════════════════════════════════════════════════
 
    public int createOrder(Order o) throws SQLException {
        String sql = "INSERT INTO orders "
                + "(user_id, address_id, promotion_id, status, subtotal, "
                + " discount_amount, shipping_fee, total_amount, note) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getUserId());
            ps.setInt(2, o.getAddressId());
            setNullableInt(ps, 3, o.getPromotionId());
            ps.setString(4, o.getStatus() != null ? o.getStatus() : "pending");
            ps.setBigDecimal(5, o.getSubtotal());
            ps.setBigDecimal(6, nvl(o.getDiscountAmount()));
            ps.setBigDecimal(7, nvl(o.getShippingFee()));
            ps.setBigDecimal(8, o.getTotalAmount());
            ps.setString(9, o.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("createOrder: no generated key");
    }
 
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.*, u.full_name AS user_full_name, u.email AS user_email, "
                   + "a.recipient_name, a.address "
                   + "FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "JOIN user_addresses a ON o.address_id = a.address_id "
                   + "WHERE o.order_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapOrder(rs, true);
            }
        }
        return null;
    }
 
    public List<Order> getOrdersByUser(int userId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name AS user_full_name, u.email AS user_email, "
                   + "a.recipient_name, a.address "
                   + "FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "JOIN user_addresses a ON o.address_id = a.address_id "
                   + "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs, true));
            }
        }
        return list;
    }
 
    public void updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status=?, updated_at=GETDATE() WHERE order_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════
    // PAYMENTS
    // ═══════════════════════════════════════════════════════════════════════
 
    public int createPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (order_id, method, status, amount, transaction_ref) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getOrderId());
            ps.setString(2, p.getMethod());
            ps.setString(3, p.getStatus());
            ps.setBigDecimal(4, p.getAmount());
            ps.setString(5, p.getTransactionRef());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("createPayment: no generated key");
    }
 
    public Payment getPaymentByOrderId(int orderId) throws SQLException {
        String sql = "SELECT p.*, o.status AS order_status, "
                   + "u.full_name AS user_full_name, u.email AS user_email "
                   + "FROM payments p "
                   + "JOIN orders o ON p.order_id = o.order_id "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE p.order_id = ? ORDER BY p.created_at DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayment(rs, true);
            }
        }
        return null;
    }
 
    public Payment getPaymentByPaymentId(int paymentId) throws SQLException {
        String sql = "SELECT p.*, o.status AS order_status, "
                   + "u.full_name AS user_full_name, u.email AS user_email "
                   + "FROM payments p "
                   + "JOIN orders o ON p.order_id = o.order_id "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE p.payment_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayment(rs, true);
            }
        }
        return null;
    }

    public Payment getPaymentByTxnRef(String txnRef) throws SQLException {
        String sql = "SELECT p.*, o.status AS order_status, "
                   + "u.full_name AS user_full_name, u.email AS user_email "
                   + "FROM payments p "
                   + "JOIN orders o ON p.order_id = o.order_id "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE p.transaction_ref = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayment(rs, true);
            }
        }
        return null;
    }
 
    /** Called after VNPay callback: update status, txnRef, gateway JSON, paidAt */
    public void updatePaymentAfterCallback(int paymentId, String status, String txnRef,
                                           String gatewayJson, LocalDateTime paidAt) throws SQLException {
        String sql = "UPDATE payments SET status=?, transaction_ref=?, gateway_response=?, "
                   + "paid_at=?, updated_at=GETDATE() WHERE payment_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, txnRef);
            ps.setString(3, gatewayJson);
            ps.setTimestamp(4, paidAt != null ? Timestamp.valueOf(paidAt) : null);
            ps.setInt(5, paymentId);
            ps.executeUpdate();
        }
    }
 
    /** Admin: all payments, paginated, with optional filters */
    public List<Payment> getAllPayments(String statusFilter, String methodFilter,
                                        int pageSize, int offset) throws SQLException {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, o.status AS order_status, "
          + "u.full_name AS user_full_name, u.email AS user_email "
          + "FROM payments p "
          + "JOIN orders o ON p.order_id = o.order_id "
          + "JOIN users u ON o.user_id = u.user_id WHERE 1=1");
        if (statusFilter != null && !statusFilter.isBlank()) sql.append(" AND p.status=?");
        if (methodFilter != null && !methodFilter.isBlank()) sql.append(" AND p.method=?");
        sql.append(" ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
 
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.isBlank()) ps.setString(idx++, statusFilter);
            if (methodFilter != null && !methodFilter.isBlank()) ps.setString(idx++, methodFilter);
            ps.setInt(idx++, offset);
            ps.setInt(idx,   pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPayment(rs, true));
            }
        }
        return list;
    }
 
    /** Customer: own payment history */
    public List<Payment> getPaymentsByUserId(int userId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, o.status AS order_status, "
                   + "u.full_name AS user_full_name, u.email AS user_email "
                   + "FROM payments p "
                   + "JOIN orders o ON p.order_id = o.order_id "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE o.user_id=? ORDER BY p.created_at DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPayment(rs, true));
            }
        }
        return list;
    }
 
    public int countAllPayments(String statusFilter, String methodFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM payments WHERE 1=1");
        if (statusFilter != null && !statusFilter.isBlank()) sql.append(" AND status=?");
        if (methodFilter != null && !methodFilter.isBlank()) sql.append(" AND method=?");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.isBlank()) ps.setString(idx++, statusFilter);
            if (methodFilter != null && !methodFilter.isBlank()) ps.setString(idx, methodFilter);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════
    // REFUNDS
    // ═══════════════════════════════════════════════════════════════════════
 
    public int createRefund(Refund r) throws SQLException {
        String sql = "INSERT INTO refunds (payment_id, amount, reason, status) VALUES (?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPaymentId());
            ps.setBigDecimal(2, r.getAmount());
            ps.setString(3, r.getReason());
            ps.setString(4, "pending");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("createRefund: no generated key");
    }
 
    public Refund getRefundById(int refundId) throws SQLException {
        String sql = "SELECT r.*, p.order_id, u.full_name AS user_full_name "
                   + "FROM refunds r "
                   + "JOIN payments p ON r.payment_id = p.payment_id "
                   + "JOIN orders o   ON p.order_id   = o.order_id "
                   + "JOIN users u    ON o.user_id    = u.user_id "
                   + "WHERE r.refund_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, refundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRefund(rs);
            }
        }
        return null;
    }
 
    public List<Refund> getAllRefunds(String statusFilter) throws SQLException {
        List<Refund> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, p.order_id, u.full_name AS user_full_name "
          + "FROM refunds r "
          + "JOIN payments p ON r.payment_id = p.payment_id "
          + "JOIN orders o   ON p.order_id   = o.order_id "
          + "JOIN users u    ON o.user_id    = u.user_id WHERE 1=1");
        if (statusFilter != null && !statusFilter.isBlank()) sql.append(" AND r.status=?");
        sql.append(" ORDER BY r.created_at DESC");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (statusFilter != null && !statusFilter.isBlank()) ps.setString(1, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRefund(rs));
            }
        }
        return list;
    }
 
    public void updateRefundStatus(int refundId, String status, Integer processedBy) throws SQLException {
        String sql = "UPDATE refunds SET status=?, processed_by=?, updated_at=GETDATE() WHERE refund_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            setNullableInt(ps, 2, processedBy);
            ps.setInt(3, refundId);
            ps.executeUpdate();
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS / MAPPERS
    // ═══════════════════════════════════════════════════════════════════════
 
    private Order mapOrder(ResultSet rs, boolean withJoins) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setAddressId(rs.getInt("address_id"));
        int pid = rs.getInt("promotion_id"); if (!rs.wasNull()) o.setPromotionId(pid);
        o.setStatus(rs.getString("status"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setShippingFee(rs.getBigDecimal("shipping_fee"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setNote(rs.getString("note"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) o.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");  if (ua != null) o.setUpdatedAt(ua.toLocalDateTime());
        if (withJoins) {
            o.setUserFullName(rs.getString("user_full_name"));
            o.setUserEmail(rs.getString("user_email"));
            o.setRecipientName(rs.getString("recipient_name"));
            o.setAddressText(rs.getString("address"));
        }
        return o;
    }
 
    private Payment mapPayment(ResultSet rs, boolean withJoins) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setMethod(rs.getString("method"));
        p.setStatus(rs.getString("status"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setTransactionRef(rs.getString("transaction_ref"));
        p.setGatewayResponse(rs.getString("gateway_response"));
        Timestamp paid = rs.getTimestamp("paid_at"); if (paid != null) p.setPaidAt(paid.toLocalDateTime());
        Timestamp ca   = rs.getTimestamp("created_at"); if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        if (withJoins) {
            p.setOrderStatus(rs.getString("order_status"));
            p.setUserFullName(rs.getString("user_full_name"));
            p.setUserEmail(rs.getString("user_email"));
        }
        return p;
    }
 
    private Refund mapRefund(ResultSet rs) throws SQLException {
        Refund r = new Refund();
        r.setRefundId(rs.getInt("refund_id"));
        r.setPaymentId(rs.getInt("payment_id"));
        r.setAmount(rs.getBigDecimal("amount"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        int pb = rs.getInt("processed_by"); if (!rs.wasNull()) r.setProcessedBy(pb);
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) r.setCreatedAt(ca.toLocalDateTime());
        r.setOrderId(rs.getInt("order_id"));
        r.setUserFullName(rs.getString("user_full_name"));
        return r;
    }
 
    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v != null) ps.setInt(idx, v); else ps.setNull(idx, Types.INTEGER);
    }
}