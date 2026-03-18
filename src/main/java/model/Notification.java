package model;

import java.time.LocalDateTime;

/**
 * Notification — thông báo gửi tới khách hàng.
 *
 * type:    payment_success | payment_failed | refund_approved |
 *          refund_rejected | order_confirmed | order_cancelled | system
 * channel: in_app (luôn có) — email / sms là UI-only trong demo này
 */
public class Notification {

    private int    notificationId;
    private int    userId;
    private int    orderId;        // 0 nếu không liên quan đơn hàng
    private int    paymentId;      // 0 nếu không liên quan payment
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    // join fields
    private String userFullName;
    private String userEmail;

    public Notification() {}

    public Notification(int userId, String type, String title, String message) {
        this.userId  = userId;
        this.type    = type;
        this.title   = title;
        this.message = message;
        this.isRead  = false;
    }

    // Getters & setters
    public int    getNotificationId()              { return notificationId; }
    public void   setNotificationId(int v)         { this.notificationId = v; }
    public int    getUserId()                      { return userId; }
    public void   setUserId(int v)                 { this.userId = v; }
    public int    getOrderId()                     { return orderId; }
    public void   setOrderId(int v)                { this.orderId = v; }
    public int    getPaymentId()                   { return paymentId; }
    public void   setPaymentId(int v)              { this.paymentId = v; }
    public String getType()                        { return type; }
    public void   setType(String v)                { this.type = v; }
    public String getTitle()                       { return title; }
    public void   setTitle(String v)               { this.title = v; }
    public String getMessage()                     { return message; }
    public void   setMessage(String v)             { this.message = v; }
    public boolean isRead()                        { return isRead; }
    public void   setRead(boolean v)               { this.isRead = v; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void   setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
    public String getUserFullName()                { return userFullName; }
    public void   setUserFullName(String v)        { this.userFullName = v; }
    public String getUserEmail()                   { return userEmail; }
    public void   setUserEmail(String v)           { this.userEmail = v; }

    /** Trả về icon emoji theo loại thông báo */
    public String getIcon() {
        if (type == null) return "🔔";
        return switch (type) {
            case "payment_success"  -> "✅";
            case "payment_failed"   -> "❌";
            case "refund_approved"  -> "↩️";
            case "refund_rejected"  -> "🚫";
            case "order_confirmed"  -> "📦";
            case "order_cancelled"  -> "⚠️";
            default                 -> "🔔";
        };
    }

    /** CSS class badge theo loại */
    public String getBadgeClass() {
        if (type == null) return "badge-neutral";
        return switch (type) {
            case "payment_success", "refund_approved", "order_confirmed" -> "badge-success";
            case "payment_failed",  "refund_rejected", "order_cancelled" -> "badge-danger";
            default -> "badge-neutral";
        };
    }
}
