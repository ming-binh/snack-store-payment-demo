package dao;

import model.Notification;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng notifications.
 *
 * DDL tham khảo (SQL Server):
 *   CREATE TABLE notifications (
 *     notification_id INT IDENTITY PRIMARY KEY,
 *     user_id         INT NOT NULL,
 *     order_id        INT DEFAULT 0,
 *     payment_id      INT DEFAULT 0,
 *     type            VARCHAR(50)  NOT NULL,
 *     title           NVARCHAR(200) NOT NULL,
 *     message         NVARCHAR(MAX) NOT NULL,
 *     is_read         BIT DEFAULT 0,
 *     created_at      DATETIME DEFAULT GETDATE()
 *   );
 */
public class NotificationDAO {

    // ── Tạo thông báo ─────────────────────────────────────────────────

    public int create(Notification n) throws SQLException {
        String sql = "INSERT INTO notifications "
                   + "(user_id, order_id, payment_id, type, title, message, is_read) "
                   + "VALUES (?,?,?,?,?,?,0)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setInt(2, n.getOrderId());
            ps.setInt(3, n.getPaymentId());
            ps.setString(4, n.getType());
            ps.setString(5, n.getTitle());
            ps.setString(6, n.getMessage());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("create notification: no generated key");
    }

    // ── Lấy thông báo theo user ────────────────────────────────────────

    public List<Notification> getByUserId(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, u.full_name AS user_full_name, u.email AS user_email "
                   + "FROM notifications n "
                   + "JOIN users u ON n.user_id = u.user_id "
                   + "WHERE n.user_id = ? "
                   + "ORDER BY n.created_at DESC";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── Admin: lấy tất cả (phân trang) ────────────────────────────────

    public List<Notification> getAll(String typeFilter, int pageSize, int offset) throws SQLException {
        List<Notification> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT n.*, u.full_name AS user_full_name, u.email AS user_email "
          + "FROM notifications n "
          + "JOIN users u ON n.user_id = u.user_id WHERE 1=1");
        if (typeFilter != null && !typeFilter.isBlank()) sql.append(" AND n.type=?");
        sql.append(" ORDER BY n.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (typeFilter != null && !typeFilter.isBlank()) ps.setString(idx++, typeFilter);
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countAll(String typeFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notifications WHERE 1=1");
        if (typeFilter != null && !typeFilter.isBlank()) sql.append(" AND type=?");
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (typeFilter != null && !typeFilter.isBlank()) ps.setString(1, typeFilter);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── Đánh dấu đã đọc ───────────────────────────────────────────────

    public void markRead(int notificationId, int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read=1 WHERE notification_id=? AND user_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, notificationId); ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read=1 WHERE user_id=? AND is_read=0";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setOrderId(rs.getInt("order_id"));
        n.setPaymentId(rs.getInt("payment_id"));
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) n.setCreatedAt(ca.toLocalDateTime());
        try { n.setUserFullName(rs.getString("user_full_name")); } catch (SQLException ignored) {}
        try { n.setUserEmail(rs.getString("user_email")); } catch (SQLException ignored) {}
        return n;
    }
}
