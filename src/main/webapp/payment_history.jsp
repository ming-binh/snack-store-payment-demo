<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.Payment, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Payment> payments  = (List<Payment>) request.getAttribute("payments");
    String error        = (String) request.getAttribute("error");
    String refundMsg    = (String) session.getAttribute("refundMsg");
    String refundError  = (String) session.getAttribute("refundError");
    session.removeAttribute("refundMsg");
    session.removeAttribute("refundError");
    String ctx          = request.getContextPath();
    NumberFormat nf     = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Lịch sử thanh toán — Snack Store</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/cart">Giỏ hàng</a></li>
    <li><a href="<%= ctx %>/payment_history" class="active">Lịch sử thanh toán</a></li>
    <li><a href="<%= ctx %>/notifications">Thông báo</a></li>
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
    <span>Lịch sử thanh toán</span>
  </div>

  <div class="page-header">
    <div>
      <h1>Lịch sử thanh toán</h1>
      <p>Tất cả giao dịch của bạn</p>
    </div>
    <a href="<%= ctx %>/cart" class="btn btn-primary">🛒 Mua sắm tiếp</a>
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

  <div class="card">

<% if (payments == null || payments.isEmpty()) { %>
    <div class="card-body empty-state">
      <span class="empty-icon">💳</span>
      <h3>Chưa có giao dịch nào</h3>
      <p>Bạn chưa thực hiện thanh toán nào. Hãy mua sắm và trải nghiệm!</p>
      <a href="<%= ctx %>/cart" class="btn btn-primary" style="margin-top:1.25rem">Mua sắm ngay</a>
    </div>
<% } else { %>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Mã đơn</th>
            <th>Phương thức</th>
            <th>Số tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Mã GD</th>
            <th style="text-align:right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
<%
    int rowIdx = 1;
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
        else if ("pending".equals(stat)) sBadge = "<span class='badge badge-warning'>⏳ Chờ xử lý</span>";
        else if ("refunded".equals(stat))sBadge = "<span class='badge badge-teal'>↩ Đã hoàn tiền</span>";
        else                             sBadge = "<span class='badge badge-neutral'>" + stat + "</span>";
%>
          <tr>
            <td style="color:var(--text-4);font-size:13px"><%= rowIdx++ %></td>
            <td><strong>#<%= p.getOrderId() %></strong></td>
            <td><%= mBadge %></td>
            <td style="font-weight:700;font-variant-numeric:tabular-nums"><%= nf.format(p.getAmount()) %> ₫</td>
            <td><%= sBadge %></td>
            <td style="font-size:13px;color:var(--text-2);white-space:nowrap">
              <%= p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T"," ").substring(0,16) : "—" %>
            </td>
            <td><%= p.getTransactionRef() != null && !p.getTransactionRef().isEmpty() ? "<code>" + p.getTransactionRef().substring(0, Math.min(p.getTransactionRef().length(), 16)) + "…</code>" : "<span style='color:var(--text-4)'>—</span>" %></td>
            <td>
              <div class="table-actions">
<% if (("failed".equals(stat) || "pending".equals(stat)) && "vnpay".equals(method)) { %>
                <form method="post" action="<%= ctx %>/payment_history">
                  <input type="hidden" name="action"  value="retry">
                  <input type="hidden" name="orderId" value="<%= p.getOrderId() %>">
                  <button type="submit" class="btn btn-sm btn-primary">🔄 Thử lại</button>
                </form>
<% } %>
<% if ("pending".equals(stat)) { %>
                <form method="post" action="<%= ctx %>/payment_history"
                      onsubmit="return confirm('Hủy giao dịch này?')">
                  <input type="hidden" name="action"  value="cancel">
                  <input type="hidden" name="orderId" value="<%= p.getOrderId() %>">
                  <button type="submit" class="btn btn-sm btn-danger-outline">✕ Hủy</button>
                </form>
<% } %>
<% if ("success".equals(stat) && "vnpay".equals(method)) { %>
                <button class="btn btn-sm btn-outline"
                        onclick="openRefund(<%= p.getPaymentId() %>, '<%= p.getAmount() %>')">
                  ↩ Hoàn tiền
                </button>
<% } %>
              </div>
            </td>
          </tr>
<% } %>
        </tbody>
      </table>
    </div>
    <div class="card-footer">
      <span>Tổng <strong><%= payments.size() %></strong> giao dịch</span>
      <span style="color:var(--text-3);font-size:12px">Dữ liệu được cập nhật realtime</span>
    </div>
<% } %>
  </div>
</div>

<!-- Modal hoàn tiền -->
<div class="modal-backdrop" id="refModal">
  <div class="modal">
    <div class="modal-header">
      <h2>↩ Yêu cầu hoàn tiền</h2>
      <button class="modal-close" onclick="closeRefund()">✕</button>
    </div>
    <div class="modal-body">
      <div class="alert alert-warning" style="margin-bottom:1rem">
        <span class="alert-icon">⚠</span>
        <div>Yêu cầu sẽ được gửi tới admin để xem xét. Thời gian xử lý 1–3 ngày làm việc.</div>
      </div>
      <form method="post" action="<%= ctx %>/admin/refunds" id="refund-form">
        <input type="hidden" name="action"    value="request">
        <input type="hidden" name="paymentId" id="rfPid">
        <div class="form-group">
          <label class="form-label">Số tiền hoàn (₫)</label>
          <input class="form-control" type="number" name="amount" id="rfAmt" min="1000" required>
          <div class="form-hint">Tối đa bằng số tiền giao dịch gốc</div>
        </div>
        <div class="form-group" style="margin-bottom:0">
          <label class="form-label">Lý do hoàn tiền <span style="color:var(--danger)">*</span></label>
          <textarea class="form-control" name="reason" required
                    placeholder="Mô tả lý do hoàn tiền..."></textarea>
        </div>
      </form>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-outline" onclick="closeRefund()">Hủy</button>
      <button type="submit" form="refund-form" class="btn btn-primary">Gửi yêu cầu</button>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

<script>
function openRefund(pid, amt) {
  document.getElementById('rfPid').value = pid;
  document.getElementById('rfAmt').value = amt;
  document.getElementById('refModal').classList.add('open');
}
function closeRefund() {
  document.getElementById('refModal').classList.remove('open');
}
document.getElementById('refModal').addEventListener('click', function(e) {
  if (e.target === this) closeRefund();
});
document.addEventListener('keydown', function(e) {
  if (e.key === 'Escape') closeRefund();
});
</script>
</body>
</html>
