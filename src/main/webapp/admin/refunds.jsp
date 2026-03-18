<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Refund, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Refund> refunds  = (List<Refund>) request.getAttribute("refunds");
    String statusFilter   = (String) request.getAttribute("statusFilter");
    String error          = (String) request.getAttribute("error");
    String refundMsg      = (String) request.getAttribute("refundMsg");
    String refundError    = (String) request.getAttribute("refundError");
    String ctx            = request.getContextPath();
    NumberFormat nf       = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
    if (statusFilter == null) statusFilter = "";

    int cPending = 0, cApproved = 0, cRejected = 0;
    if (refunds != null) {
        for (Refund r : refunds) {
            if ("pending".equals(r.getStatus()))   cPending++;
            else if ("approved".equals(r.getStatus())) cApproved++;
            else if ("rejected".equals(r.getStatus())) cRejected++;
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Quản lý hoàn tiền — Admin</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/admin/transactions">Giao dịch</a></li>
    <li><a href="<%= ctx %>/admin/refunds" class="active">Hoàn tiền</a></li>
    <li><a href="<%= ctx %>/admin/notifications">Thông báo</a></li>
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
      <h1>Quản lý hoàn tiền</h1>
      <p>Xem xét và xử lý yêu cầu hoàn tiền từ khách hàng (UC-25)</p>
    </div>
  </div>

<% if (error != null && !error.isEmpty()) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= error %></div>
<% } %>
<% if (refundMsg != null && !refundMsg.isEmpty()) { %>
  <div class="alert alert-success"><span class="alert-icon">✓</span><%= refundMsg %></div>
<% } %>
<% if (refundError != null && !refundError.isEmpty()) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= refundError %></div>
<% } %>

  <!-- Stats -->
  <div class="stats-grid">
    <div class="stat-card stat-total">
      <div class="stat-label">Tổng yêu cầu</div>
      <div class="stat-value"><%= refunds != null ? refunds.size() : 0 %></div>
      <div class="stat-sub">Tất cả trạng thái</div>
    </div>
    <div class="stat-card stat-warn">
      <div class="stat-label">Chờ duyệt</div>
      <div class="stat-value" style="color:var(--warn)"><%= cPending %></div>
      <div class="stat-sub">Cần xử lý</div>
    </div>
    <div class="stat-card stat-success">
      <div class="stat-label">Đã duyệt</div>
      <div class="stat-value" style="color:var(--success)"><%= cApproved %></div>
      <div class="stat-sub">Hoàn tiền thành công</div>
    </div>
    <div class="stat-card stat-danger">
      <div class="stat-label">Từ chối</div>
      <div class="stat-value" style="color:var(--danger)"><%= cRejected %></div>
      <div class="stat-sub">Không được duyệt</div>
    </div>
  </div>

  <div class="card">

    <!-- Filter -->
    <form method="get" action="<%= ctx %>/admin/refunds">
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Trạng thái</label>
          <select class="form-control" name="status">
            <option value="">Tất cả</option>
            <option value="pending"   <%= "pending"  .equals(statusFilter) ? "selected" : "" %>>⏳ Chờ duyệt</option>
            <option value="approved"  <%= "approved" .equals(statusFilter) ? "selected" : "" %>>✓ Đã duyệt</option>
            <option value="rejected"  <%= "rejected" .equals(statusFilter) ? "selected" : "" %>>✗ Từ chối</option>
            <option value="completed" <%= "completed".equals(statusFilter) ? "selected" : "" %>>✅ Hoàn tất</option>
          </select>
        </div>
        <div style="display:flex;gap:.5rem;margin-top:auto">
          <button type="submit" class="btn btn-primary btn-sm">Lọc</button>
          <a href="<%= ctx %>/admin/refunds" class="btn btn-outline btn-sm">Đặt lại</a>
        </div>
      </div>
    </form>

<% if (refunds == null || refunds.isEmpty()) { %>
    <div class="empty-state">
      <span class="empty-icon">↩</span>
      <h3>Không có yêu cầu hoàn tiền</h3>
      <p>Hiện tại không có yêu cầu nào cần xử lý.</p>
    </div>
<% } else { %>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã GD</th>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th style="text-align:right">Số tiền hoàn</th>
            <th>Lý do</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th style="text-align:right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
<%
    for (Refund r : refunds) {
        String stat = r.getStatus() != null ? r.getStatus() : "";

        String sBadge;
        if ("pending".equals(stat))        sBadge = "<span class='badge badge-warning'>⏳ Chờ duyệt</span>";
        else if ("approved".equals(stat))  sBadge = "<span class='badge badge-success'>✓ Đã duyệt</span>";
        else if ("rejected".equals(stat))  sBadge = "<span class='badge badge-danger'>✗ Từ chối</span>";
        else if ("completed".equals(stat)) sBadge = "<span class='badge badge-teal'>✅ Hoàn tất</span>";
        else                               sBadge = "<span class='badge badge-neutral'>" + stat + "</span>";
%>
          <tr>
            <td style="color:var(--text-4);font-size:13px"><%= r.getRefundId() %></td>
            <td style="font-size:13px">#<%= r.getPaymentId() %></td>
            <td><strong>#<%= r.getOrderId() %></strong></td>
            <td style="font-weight:500"><%= r.getUserFullName() != null ? r.getUserFullName() : "—" %></td>
            <td style="text-align:right;font-weight:700;color:var(--danger);font-variant-numeric:tabular-nums">
              <%= nf.format(r.getAmount()) %> ₫
            </td>
            <td>
              <div style="max-width:200px;overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;font-size:13px;color:var(--text-2)">
                <%= r.getReason() != null ? r.getReason() : "—" %>
              </div>
            </td>
            <td><%= sBadge %></td>
            <td style="font-size:13px;color:var(--text-2);white-space:nowrap">
              <%= r.getCreatedAt() != null ? r.getCreatedAt().toString().replace("T"," ").substring(0,16) : "—" %>
            </td>
            <td>
              <div class="table-actions">
<% if ("pending".equals(stat)) { %>
                <form method="post" action="<%= ctx %>/admin/refunds"
                      onsubmit="return confirm('Duyệt hoàn tiền #<%= r.getRefundId() %>?')">
                  <input type="hidden" name="action"   value="approve">
                  <input type="hidden" name="refundId" value="<%= r.getRefundId() %>">
                  <button type="submit" class="btn btn-sm btn-success">✓ Duyệt</button>
                </form>
                <form method="post" action="<%= ctx %>/admin/refunds"
                      onsubmit="return confirm('Từ chối hoàn tiền #<%= r.getRefundId() %>?')">
                  <input type="hidden" name="action"   value="reject">
                  <input type="hidden" name="refundId" value="<%= r.getRefundId() %>">
                  <button type="submit" class="btn btn-sm btn-danger">✗ Từ chối</button>
                </form>
<% } else { %>
                <span style="font-size:13px;color:var(--text-4)">Đã xử lý</span>
<% } %>
              </div>
            </td>
          </tr>
<% } %>
        </tbody>
      </table>
    </div>
    <div class="card-footer">
      <span>Tổng <strong><%= refunds.size() %></strong> yêu cầu</span>
      <% if (cPending > 0) { %>
        <span style="color:var(--warn);font-weight:600;font-size:13px">⚠ Có <%= cPending %> yêu cầu đang chờ xử lý</span>
      <% } %>
    </div>
<% } %>

  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
