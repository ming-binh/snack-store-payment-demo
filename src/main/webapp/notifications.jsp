<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Notification" %>
<%
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    int unreadCount = request.getAttribute("unreadCount") != null ? (Integer) request.getAttribute("unreadCount") : 0;
    String error    = (String) request.getAttribute("error");
    String ctx      = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Thông báo — Snack Store</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
<style>
.notif-item {
  display: flex; gap: 1rem; padding: 1.1rem 1.5rem;
  border-bottom: 1px solid var(--border); align-items: flex-start;
  transition: background .1s; cursor: default; position: relative;
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: #FDFCFB; }
.notif-item.unread { background: #FFFBF7; }
.notif-item.unread::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0;
  width: 3px; background: var(--accent); border-radius: 0 2px 2px 0;
}
.notif-icon {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.15rem; flex-shrink: 0; margin-top: 1px;
}
.notif-icon.success  { background: var(--success-bg); }
.notif-icon.danger   { background: var(--danger-bg); }
.notif-icon.info     { background: var(--info-bg); }
.notif-icon.warning  { background: var(--warn-bg); }
.notif-icon.neutral  { background: var(--bg); border: 1px solid var(--border); }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-weight: 600; font-size: 14px; margin-bottom: 3px; color: var(--text); }
.notif-item.unread .notif-title { color: var(--text); }
.notif-msg   { font-size: 13px; color: var(--text-2); line-height: 1.55; }
.notif-meta  { display: flex; align-items: center; gap: .6rem; margin-top: 5px; flex-wrap: wrap; }
.notif-time  { font-size: 12px; color: var(--text-3); }
.notif-actions { display: flex; gap: .4rem; align-items: flex-start; flex-shrink: 0; }

.filter-tabs { display: flex; gap: .3rem; padding: 1rem 1.5rem; border-bottom: 1px solid var(--border); flex-wrap: wrap; }
.filter-tab {
  padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 500;
  color: var(--text-2); text-decoration: none; transition: .12s;
  border: 1.5px solid transparent;
}
.filter-tab:hover { background: var(--bg); color: var(--text); }
.filter-tab.active { background: var(--accent-bg); color: var(--accent); border-color: var(--accent-bg2); font-weight: 600; }
</style>
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/cart">Giỏ hàng</a></li>
    <li><a href="<%= ctx %>/payment_history">Lịch sử</a></li>
    <li><a href="<%= ctx %>/notifications" class="active">
      Thông báo<% if (unreadCount > 0) { %> <span class="navbar-badge"><%= unreadCount %></span><% } %>
    </a></li>
  </ul>
  <div class="navbar-end">
    <div class="navbar-avatar">A</div>
    <span class="navbar-user">Nguyễn Văn A</span>
  </div>
</nav>

<div class="page-wrapper">
  <div class="breadcrumb">
    <a href="<%= ctx %>/">Trang chủ</a>
    <span class="breadcrumb-sep">›</span>
    <span>Thông báo</span>
  </div>

  <div class="page-header">
    <div>
      <h1>Thông báo</h1>
      <p>Cập nhật về đơn hàng và thanh toán của bạn</p>
    </div>
    <% if (notifications != null && !notifications.isEmpty() && unreadCount > 0) { %>
    <form method="post" action="<%= ctx %>/notifications">
      <input type="hidden" name="action" value="readAll">
      <button type="submit" class="btn btn-outline btn-sm">✓ Đánh dấu tất cả đã đọc</button>
    </form>
    <% } %>
  </div>

<% if (error != null) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= error %></div>
<% } %>

  <!-- Stats nhanh -->
  <% if (notifications != null && !notifications.isEmpty()) { %>
  <div class="stats-grid" style="grid-template-columns:repeat(3,1fr);margin-bottom:1.5rem">
    <div class="stat-card stat-total">
      <div class="stat-label">Tổng thông báo</div>
      <div class="stat-value"><%= notifications.size() %></div>
      <div class="stat-sub">Tất cả</div>
    </div>
    <div class="stat-card stat-warn">
      <div class="stat-label">Chưa đọc</div>
      <div class="stat-value" style="color:var(--accent)"><%= unreadCount %></div>
      <div class="stat-sub">Cần xem</div>
    </div>
    <div class="stat-card stat-success">
      <div class="stat-label">Đã đọc</div>
      <div class="stat-value" style="color:var(--success)"><%= notifications.size() - unreadCount %></div>
      <div class="stat-sub">Đã xem</div>
    </div>
  </div>
  <% } %>

  <div class="card">

    <!-- Filter tabs -->
    <div class="filter-tabs">
      <a href="<%= ctx %>/notifications" class="filter-tab active">🔔 Tất cả</a>
      <a href="#" class="filter-tab">✅ Thanh toán</a>
      <a href="#" class="filter-tab">↩ Hoàn tiền</a>
      <a href="#" class="filter-tab">📦 Đơn hàng</a>
    </div>

<% if (notifications == null || notifications.isEmpty()) { %>
    <div class="empty-state">
      <span class="empty-icon">🔔</span>
      <h3>Chưa có thông báo nào</h3>
      <p>Các thông báo về đơn hàng và thanh toán sẽ xuất hiện ở đây.</p>
    </div>
<% } else {
    for (Notification n : notifications) {
        String iconClass;
        switch (n.getType() != null ? n.getType() : "") {
            case "payment_success": case "refund_approved": case "order_confirmed": iconClass = "success"; break;
            case "payment_failed":  case "refund_rejected": case "order_cancelled": iconClass = "danger"; break;
            default: iconClass = "neutral";
        }
        String timeStr = n.getCreatedAt() != null
            ? n.getCreatedAt().toString().replace("T"," ").substring(0,16)
            : "—";
%>
    <div class="notif-item <%= n.isRead() ? "" : "unread" %>">
      <div class="notif-icon <%= iconClass %>"><%= n.getIcon() %></div>
      <div class="notif-body">
        <div class="notif-title"><%= n.getTitle() %></div>
        <div class="notif-msg"><%= n.getMessage() %></div>
        <div class="notif-meta">
          <span class="notif-time">🕐 <%= timeStr %></span>
          <% if (!n.isRead()) { %>
            <span class="badge badge-warning" style="font-size:10px;padding:1px 7px">Mới</span>
          <% } %>
          <% if (n.getOrderId() > 0) { %>
            <a href="<%= ctx %>/payment_history" class="badge badge-neutral" style="font-size:11px;text-decoration:none">Đơn #<%= n.getOrderId() %></a>
          <% } %>
        </div>
      </div>
      <div class="notif-actions">
        <% if (!n.isRead()) { %>
        <form method="post" action="<%= ctx %>/notifications" style="display:inline">
          <input type="hidden" name="action" value="read">
          <input type="hidden" name="id" value="<%= n.getNotificationId() %>">
          <button type="submit" class="btn btn-ghost btn-sm btn-icon" title="Đánh dấu đã đọc" style="font-size:14px">✓</button>
        </form>
        <% } %>
      </div>
    </div>
<%  } } %>

  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
