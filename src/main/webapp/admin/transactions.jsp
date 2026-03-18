<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Payment, java.text.NumberFormat, java.util.Locale, java.math.BigDecimal" %>
<%
    List<Payment> payments = (List<Payment>) request.getAttribute("payments");
    int total       = request.getAttribute("total")      != null ? (Integer)request.getAttribute("total")      : 0;
    int currentPage = request.getAttribute("page")       != null ? (Integer)request.getAttribute("page")       : 1;
    int totalPages  = request.getAttribute("totalPages") != null ? (Integer)request.getAttribute("totalPages") : 1;
    String statusFilter = request.getAttribute("statusFilter") != null ? (String)request.getAttribute("statusFilter") : "";
    String methodFilter = request.getAttribute("methodFilter") != null ? (String)request.getAttribute("methodFilter") : "";
    String error    = (String) request.getAttribute("error");
    String ctx      = request.getContextPath();
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
    if (statusFilter == null) statusFilter = "";
    if (methodFilter == null) methodFilter = "";

    int cSuccess = 0, cPending = 0, cFail = 0, cRefund = 0;
    BigDecimal totalRevenue = BigDecimal.ZERO;
    if (payments != null) {
        for (Payment p : payments) {
            if ("success".equals(p.getStatus()))  { cSuccess++; totalRevenue = totalRevenue.add(p.getAmount()); }
            else if ("pending".equals(p.getStatus())) cPending++;
            else if ("failed".equals(p.getStatus()))  cFail++;
            else if ("refunded".equals(p.getStatus())) cRefund++;
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Quản lý giao dịch — Admin</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/admin/transactions" class="active">Giao dịch</a></li>
    <li><a href="<%= ctx %>/admin/refunds">Hoàn tiền</a></li>
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
      <h1>Quản lý giao dịch</h1>
      <p>Theo dõi và kiểm tra tất cả giao dịch thanh toán (UC-24)</p>
    </div>
    <div style="display:flex;gap:.6rem">
      <button class="btn btn-outline btn-sm">📥 Xuất Excel</button>
      <button class="btn btn-outline btn-sm">🖨 In báo cáo</button>
    </div>
  </div>

<% if (error != null && !error.isEmpty()) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= error %></div>
<% } %>

  <!-- Stats -->
  <div class="stats-grid">
    <div class="stat-card stat-total">
      <div class="stat-label">Tổng giao dịch</div>
      <div class="stat-value"><%= total %></div>
      <div class="stat-sub">Tất cả trạng thái</div>
    </div>
    <div class="stat-card stat-success">
      <div class="stat-label">Thành công</div>
      <div class="stat-value" style="color:var(--success)"><%= cSuccess %></div>
      <div class="stat-sub"><%= nf.format(totalRevenue) %> ₫</div>
    </div>
    <div class="stat-card stat-warn">
      <div class="stat-label">Chờ xử lý</div>
      <div class="stat-value" style="color:var(--warn)"><%= cPending %></div>
      <div class="stat-sub">Trang này</div>
    </div>
    <div class="stat-card stat-danger">
      <div class="stat-label">Thất bại / Hoàn</div>
      <div class="stat-value" style="color:var(--danger)"><%= cFail + cRefund %></div>
      <div class="stat-sub"><%= cFail %> thất bại · <%= cRefund %> hoàn</div>
    </div>
  </div>

  <div class="card">

    <!-- Filter -->
    <form method="get" action="<%= ctx %>/admin/transactions">
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Trạng thái</label>
          <select class="form-control" name="status">
            <option value="">Tất cả</option>
            <option value="pending"  <%= "pending" .equals(statusFilter) ? "selected" : "" %>>⏳ Chờ xử lý</option>
            <option value="success"  <%= "success" .equals(statusFilter) ? "selected" : "" %>>✓ Thành công</option>
            <option value="failed"   <%= "failed"  .equals(statusFilter) ? "selected" : "" %>>✗ Thất bại</option>
            <option value="refunded" <%= "refunded".equals(statusFilter) ? "selected" : "" %>>↩ Đã hoàn</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Phương thức</label>
          <select class="form-control" name="method">
            <option value="">Tất cả</option>
            <option value="vnpay" <%= "vnpay".equals(methodFilter) ? "selected" : "" %>>🏦 VNPay</option>
            <option value="cod"   <%= "cod"  .equals(methodFilter) ? "selected" : "" %>>💵 COD</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Tìm kiếm</label>
          <input class="form-control" type="text" name="search" placeholder="Mã đơn, khách hàng...">
        </div>
        <input type="hidden" name="page" value="1">
        <div style="display:flex;gap:.5rem;margin-top:auto">
          <button type="submit" class="btn btn-primary btn-sm">Lọc</button>
          <a href="<%= ctx %>/admin/transactions" class="btn btn-outline btn-sm">Đặt lại</a>
        </div>
      </div>
    </form>

<% if (payments == null || payments.isEmpty()) { %>
    <div class="empty-state">
      <span class="empty-icon">📊</span>
      <h3>Không có giao dịch nào</h3>
      <p>Thử thay đổi bộ lọc để xem dữ liệu.</p>
    </div>
<% } else { %>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Phương thức</th>
            <th style="text-align:right">Số tiền</th>
            <th>Trạng thái</th>
            <th>Mã GD Gateway</th>
            <th>Ngày tạo</th>
            <th>Ngày thanh toán</th>
          </tr>
        </thead>
        <tbody>
<%
    for (Payment p : payments) {
        String method = p.getMethod()  != null ? p.getMethod()  : "";
        String stat   = p.getStatus()  != null ? p.getStatus()  : "";

        String mBadge;
        if ("vnpay".equals(method))      mBadge = "<span class='badge badge-info'>🏦 VNPay</span>";
        else if ("cod".equals(method))   mBadge = "<span class='badge badge-neutral'>💵 COD</span>";
        else                             mBadge = "<span class='badge badge-neutral'>" + method + "</span>";

        String sBadge;
        if ("success".equals(stat))      sBadge = "<span class='badge badge-success'>✓ Thành công</span>";
        else if ("failed".equals(stat))  sBadge = "<span class='badge badge-danger'>✗ Thất bại</span>";
        else if ("pending".equals(stat)) sBadge = "<span class='badge badge-warning'>⏳ Chờ</span>";
        else if ("refunded".equals(stat))sBadge = "<span class='badge badge-teal'>↩ Hoàn</span>";
        else                             sBadge = "<span class='badge badge-neutral'>" + stat + "</span>";
%>
          <tr>
            <td style="color:var(--text-4);font-size:13px"><%= p.getPaymentId() %></td>
            <td><strong>#<%= p.getOrderId() %></strong></td>
            <td>
              <div style="font-weight:500"><%= p.getUserFullName() != null ? p.getUserFullName() : "—" %></div>
              <div style="font-size:12px;color:var(--text-3)"><%= p.getUserEmail() != null ? p.getUserEmail() : "" %></div>
            </td>
            <td><%= mBadge %></td>
            <td style="text-align:right;font-weight:700;font-variant-numeric:tabular-nums"><%= nf.format(p.getAmount()) %> ₫</td>
            <td><%= sBadge %></td>
            <td><%= p.getTransactionRef() != null && !p.getTransactionRef().isEmpty() ? "<code>" + p.getTransactionRef() + "</code>" : "<span style='color:var(--text-4)'>—</span>" %></td>
            <td style="font-size:13px;color:var(--text-2);white-space:nowrap"><%= p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T"," ").substring(0,16) : "—" %></td>
            <td style="font-size:13px;color:var(--text-2);white-space:nowrap"><%= p.getPaidAt() != null ? p.getPaidAt().toString().replace("T"," ").substring(0,16) : "—" %></td>
          </tr>
<%  } %>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination">
      <% if (currentPage > 1) { %>
        <a href="?page=<%= currentPage-1 %>&status=<%= statusFilter %>&method=<%= methodFilter %>">← Trước</a>
      <% } %>
      <% for (int i = 1; i <= totalPages; i++) {
           if (i == currentPage) { %><span class="active"><%= i %></span>
         <% } else if (i == 1 || i == totalPages || Math.abs(i - currentPage) <= 2) { %>
           <a href="?page=<%= i %>&status=<%= statusFilter %>&method=<%= methodFilter %>"><%= i %></a>
         <% } else if (Math.abs(i - currentPage) == 3) { %><span class="disabled">…</span><% }
         } %>
      <% if (currentPage < totalPages) { %>
        <a href="?page=<%= currentPage+1 %>&status=<%= statusFilter %>&method=<%= methodFilter %>">Sau →</a>
      <% } %>
    </div>
    <div style="text-align:center;font-size:13px;color:var(--text-3);padding-bottom:1rem">
      Trang <%= currentPage %> / <%= totalPages %> — Tổng <%= total %> giao dịch
    </div>
<% } %>

  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
