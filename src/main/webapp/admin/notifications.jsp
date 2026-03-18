<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Notification" %>
<%
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    int total       = request.getAttribute("total")      != null ? (Integer)request.getAttribute("total")      : 0;
    int currentPage = request.getAttribute("page")       != null ? (Integer)request.getAttribute("page")       : 1;
    int totalPages  = request.getAttribute("totalPages") != null ? (Integer)request.getAttribute("totalPages") : 1;
    String typeFilter = (String) request.getAttribute("typeFilter");
    String error      = (String) request.getAttribute("error");
    String ctx        = request.getContextPath();
    if (typeFilter == null) typeFilter = "";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Lịch sử thông báo — Admin</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/admin/transactions">Giao dịch</a></li>
    <li><a href="<%= ctx %>/admin/refunds">Hoàn tiền</a></li>
    <li><a href="<%= ctx %>/admin/notifications" class="active">Thông báo</a></li>
  </ul>
  <div class="navbar-end">
    <span class="navbar-badge">Admin</span>
    <div class="navbar-divider"></div>
    <div class="navbar-avatar" style="background:#EEF4FD;color:var(--info)">👤</div>
  </div>
</nav>

<div class="page-wrapper-wide">
  <div class="page-header">
    <div>
      <h1>Lịch sử thông báo</h1>
      <p>Tất cả thông báo đã gửi tới khách hàng</p>
    </div>
  </div>

<% if (error != null && !error.isEmpty()) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= error %></div>
<% } %>

  <!-- Stats -->
  <%
    int cPaySuccess = 0, cPayFail = 0, cRefund = 0, cOrder = 0;
    if (notifications != null) {
        for (Notification n : notifications) {
            if (n.getType() == null) continue;
            if (n.getType().startsWith("payment_success")) cPaySuccess++;
            else if (n.getType().startsWith("payment_failed")) cPayFail++;
            else if (n.getType().startsWith("refund_")) cRefund++;
            else if (n.getType().startsWith("order_")) cOrder++;
        }
    }
  %>
  <div class="stats-grid" style="grid-template-columns:repeat(4,1fr)">
    <div class="stat-card stat-total">
      <div class="stat-label">Tổng đã gửi</div>
      <div class="stat-value"><%= total %></div>
      <div class="stat-sub">Tất cả loại</div>
    </div>
    <div class="stat-card stat-success">
      <div class="stat-label">Thanh toán thành công</div>
      <div class="stat-value" style="color:var(--success)"><%= cPaySuccess %></div>
      <div class="stat-sub">Trang này</div>
    </div>
    <div class="stat-card stat-danger">
      <div class="stat-label">Thanh toán thất bại</div>
      <div class="stat-value" style="color:var(--danger)"><%= cPayFail %></div>
      <div class="stat-sub">Trang này</div>
    </div>
    <div class="stat-card stat-info">
      <div class="stat-label">Hoàn tiền + Đơn hàng</div>
      <div class="stat-value" style="color:var(--info)"><%= cRefund + cOrder %></div>
      <div class="stat-sub">Trang này</div>
    </div>
  </div>

  <div class="card">

    <!-- Filter -->
    <form method="get" action="<%= ctx %>/admin/notifications">
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Loại thông báo</label>
          <select class="form-control" name="type">
            <option value="">Tất cả</option>
            <option value="payment_success" <%= "payment_success".equals(typeFilter) ? "selected" : "" %>>✅ Thanh toán thành công</option>
            <option value="payment_failed"  <%= "payment_failed" .equals(typeFilter) ? "selected" : "" %>>❌ Thanh toán thất bại</option>
            <option value="refund_approved" <%= "refund_approved".equals(typeFilter) ? "selected" : "" %>>↩ Hoàn tiền được duyệt</option>
            <option value="refund_rejected" <%= "refund_rejected".equals(typeFilter) ? "selected" : "" %>>🚫 Hoàn tiền bị từ chối</option>
            <option value="order_confirmed" <%= "order_confirmed".equals(typeFilter) ? "selected" : "" %>>📦 Đặt hàng thành công</option>
            <option value="order_cancelled" <%= "order_cancelled".equals(typeFilter) ? "selected" : "" %>>⚠ Đơn hàng đã hủy</option>
          </select>
        </div>
        <input type="hidden" name="page" value="1">
        <div style="display:flex;gap:.5rem;margin-top:auto">
          <button type="submit" class="btn btn-primary btn-sm">Lọc</button>
          <a href="<%= ctx %>/admin/notifications" class="btn btn-outline btn-sm">Đặt lại</a>
        </div>
      </div>
    </form>

<% if (notifications == null || notifications.isEmpty()) { %>
    <div class="empty-state">
      <span class="empty-icon">🔔</span>
      <h3>Chưa có thông báo nào</h3>
      <p>Thông báo sẽ được tạo tự động khi có sự kiện thanh toán.</p>
    </div>
<% } else { %>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Khách hàng</th>
            <th>Loại</th>
            <th>Tiêu đề</th>
            <th>Nội dung</th>
            <th>Mã đơn</th>
            <th>Trạng thái</th>
            <th>Thời gian</th>
          </tr>
        </thead>
        <tbody>
<%
    for (Notification n : notifications) {
        String typeBadge;
        switch (n.getType() != null ? n.getType() : "") {
            case "payment_success": typeBadge = "<span class='badge badge-success'>✅ Thanh toán TC</span>"; break;
            case "payment_failed":  typeBadge = "<span class='badge badge-danger'>❌ Thanh toán TBai</span>"; break;
            case "refund_approved": typeBadge = "<span class='badge badge-teal'>↩ Hoàn tiền duyệt</span>"; break;
            case "refund_rejected": typeBadge = "<span class='badge badge-danger'>🚫 Hoàn tiền từ chối</span>"; break;
            case "order_confirmed": typeBadge = "<span class='badge badge-info'>📦 Đặt hàng TC</span>"; break;
            case "order_cancelled": typeBadge = "<span class='badge badge-warning'>⚠ Đơn đã hủy</span>"; break;
            default: typeBadge = "<span class='badge badge-neutral'>" + n.getType() + "</span>";
        }
        String timeStr = n.getCreatedAt() != null
            ? n.getCreatedAt().toString().replace("T"," ").substring(0,16) : "—";
%>
          <tr>
            <td style="color:var(--text-4);font-size:13px"><%= n.getNotificationId() %></td>
            <td>
              <div style="font-weight:500;font-size:14px"><%= n.getUserFullName() != null ? n.getUserFullName() : "User #" + n.getUserId() %></div>
              <div style="font-size:12px;color:var(--text-3)"><%= n.getUserEmail() != null ? n.getUserEmail() : "" %></div>
            </td>
            <td><%= typeBadge %></td>
            <td style="font-size:13px;font-weight:600"><%= n.getTitle() %></td>
            <td style="font-size:12px;color:var(--text-2);max-width:260px">
              <div style="overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">
                <%= n.getMessage() %>
              </div>
            </td>
            <td>
              <% if (n.getOrderId() > 0) { %>
                <strong>#<%= n.getOrderId() %></strong>
              <% } else { %><span style="color:var(--text-4)">—</span><% } %>
            </td>
            <td>
              <% if (n.isRead()) { %>
                <span class="badge badge-neutral">✓ Đã đọc</span>
              <% } else { %>
                <span class="badge badge-warning">● Chưa đọc</span>
              <% } %>
            </td>
            <td style="font-size:13px;color:var(--text-2);white-space:nowrap"><%= timeStr %></td>
          </tr>
<%  } %>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination">
      <% if (currentPage > 1) { %>
        <a href="?page=<%= currentPage-1 %>&type=<%= typeFilter %>">← Trước</a>
      <% } %>
      <% for (int i = 1; i <= totalPages; i++) {
           if (i == currentPage) { %><span class="active"><%= i %></span>
         <% } else { %><a href="?page=<%= i %>&type=<%= typeFilter %>"><%= i %></a><% }
         } %>
      <% if (currentPage < totalPages) { %>
        <a href="?page=<%= currentPage+1 %>&type=<%= typeFilter %>">Sau →</a>
      <% } %>
    </div>
    <div style="text-align:center;font-size:13px;color:var(--text-3);padding-bottom:1rem">
      Trang <%= currentPage %> / <%= totalPages %> — Tổng <%= total %> thông báo
    </div>
<% } %>

  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
