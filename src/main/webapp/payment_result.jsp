<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Payment, java.text.NumberFormat, java.util.Locale" %>
<%
    Boolean success  = (Boolean)  request.getAttribute("success");
    String  message  = (String)   request.getAttribute("message");
    Payment payment  = (Payment)  request.getAttribute("payment");
    String  status   = request.getParameter("status");
    String  orderId  = request.getParameter("orderId");
    String  ctx      = request.getContextPath();
    NumberFormat nf  = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
    boolean isCod     = "cod".equals(status);
    boolean isSuccess = Boolean.TRUE.equals(success);
    boolean isFail    = !isCod && !isSuccess;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Kết quả thanh toán — Snack Store</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/payment_history">Lịch sử thanh toán</a></li>
    <li><a href="<%= ctx %>/notifications">Thông báo</a></li>
    <li><a href="<%= ctx %>/cart">Giỏ hàng</a></li>
  </ul>
  <div class="navbar-end">
    <div class="navbar-avatar">A</div>
    <span class="navbar-user">Nguyễn Văn A</span>
  </div>
</nav>

<div class="page-wrapper">
  <div class="result-center">

<% if (isCod) { %>
    <div class="result-icon">📦</div>
    <h1 style="color:var(--info)">Đặt hàng thành công!</h1>
    <p class="lead">Đơn hàng <strong>#<%= orderId != null ? orderId : "?" %></strong> đã được xác nhận.<br>Bạn sẽ thanh toán <strong>tiền mặt khi nhận hàng</strong>.</p>

    <div class="card result-box">
      <div class="card-header"><span class="card-header-icon">📦</span><h2>Chi tiết đơn hàng</h2></div>
      <div class="card-body">
        <table class="detail-table">
          <tr><td>Mã đơn hàng</td><td><strong>#<%= orderId != null ? orderId : "?" %></strong></td></tr>
          <tr><td>Phương thức</td><td><span class="badge badge-neutral">💵 COD – Tiền mặt</span></td></tr>
          <tr><td>Trạng thái</td><td><span class="badge badge-info">🚚 Chờ giao hàng</span></td></tr>
          <tr><td>Dự kiến giao</td><td>2–3 ngày làm việc</td></tr>
        </table>
      </div>
    </div>

    <div class="result-actions">
      <a href="<%= ctx %>/payment_history" class="btn btn-primary">Xem lịch sử đơn hàng</a>
      <a href="<%= ctx %>/cart" class="btn btn-outline">Tiếp tục mua sắm</a>
    </div>

<% } else if (isSuccess) { %>
    <div class="result-icon">✅</div>
    <h1 style="color:var(--success)">Thanh toán thành công!</h1>
    <p class="lead"><%= message != null ? message : "Giao dịch đã được xử lý thành công." %></p>

  <% if (payment != null) { %>
    <div class="card result-box">
      <div class="card-header"><span class="card-header-icon">🧾</span><h2>Chi tiết giao dịch</h2></div>
      <div class="card-body">
        <table class="detail-table">
          <tr><td>Mã đơn hàng</td><td><strong>#<%= payment.getOrderId() %></strong></td></tr>
          <tr><td>Mã giao dịch</td><td><code><%= payment.getTransactionRef() != null ? payment.getTransactionRef() : "—" %></code></td></tr>
          <tr><td>Số tiền</td><td><strong style="color:var(--success);font-size:1.05rem"><%= nf.format(payment.getAmount()) %> ₫</strong></td></tr>
          <tr><td>Phương thức</td><td><span class="badge badge-info">🏦 VNPay</span></td></tr>
          <tr><td>Trạng thái</td><td><span class="badge badge-success">✓ Thành công</span></td></tr>
          <% if (payment.getPaidAt() != null) { %>
          <tr><td>Thời gian</td><td><%= payment.getPaidAt().toString().replace("T"," ").substring(0,19) %></td></tr>
          <% } %>
        </table>
      </div>
    </div>
  <% } %>

    <div class="result-actions">
      <a href="<%= ctx %>/payment_history" class="btn btn-primary">Xem lịch sử thanh toán</a>
      <a href="<%= ctx %>/cart" class="btn btn-outline">Tiếp tục mua sắm</a>
    </div>

    <!-- Gợi ý sau khi mua -->
    <div class="card result-box" style="margin-top:1rem">
      <div class="card-body" style="display:flex;gap:1.5rem;flex-wrap:wrap;text-align:left">
        <div style="flex:1;min-width:140px">
          <div style="font-size:1.4rem;margin-bottom:.4rem">🎁</div>
          <div style="font-weight:600;font-size:14px;margin-bottom:3px">Chương trình thành viên</div>
          <div style="font-size:13px;color:var(--text-2)">Tích điểm mỗi đơn hàng để nhận ưu đãi</div>
        </div>
        <div style="flex:1;min-width:140px">
          <div style="font-size:1.4rem;margin-bottom:.4rem">📱</div>
          <div style="font-weight:600;font-size:14px;margin-bottom:3px">Tải ứng dụng</div>
          <div style="font-size:13px;color:var(--text-2)">Theo dõi đơn hàng dễ dàng trên mobile</div>
        </div>
        <div style="flex:1;min-width:140px">
          <div style="font-size:1.4rem;margin-bottom:.4rem">🔔</div>
          <div style="font-weight:600;font-size:14px;margin-bottom:3px">Nhận thông báo</div>
          <div style="font-size:13px;color:var(--text-2)">Cập nhật trạng thái giao hàng realtime</div>
        </div>
      </div>
    </div>

<% } else { %>
    <div class="result-icon">❌</div>
    <h1 style="color:var(--danger)">Thanh toán thất bại</h1>
    <p class="lead"><%= message != null && !message.isEmpty() ? message : "Giao dịch không thể hoàn tất. Vui lòng thử lại." %></p>

  <% if (payment != null) { %>
    <div class="card result-box">
      <div class="card-header"><span class="card-header-icon">⚠</span><h2>Thông tin giao dịch</h2></div>
      <div class="card-body">
        <table class="detail-table">
          <tr><td>Mã đơn hàng</td><td><strong>#<%= payment.getOrderId() %></strong></td></tr>
          <tr><td>Số tiền</td><td><%= nf.format(payment.getAmount()) %> ₫</td></tr>
          <tr><td>Phương thức</td><td><span class="badge badge-info">VNPay</span></td></tr>
          <tr><td>Trạng thái</td><td><span class="badge badge-danger">✗ Thất bại</span></td></tr>
        </table>
      </div>
    </div>
  <% } %>

    <!-- Retry card -->
    <div class="card result-box" style="border-top:3px solid var(--warn)">
      <div class="card-header"><span class="card-header-icon">🔄</span><h2>Thử lại thanh toán</h2></div>
      <div class="card-body" style="display:flex;flex-direction:column;gap:.75rem">
  <% if (payment != null) { %>
        <form method="post" action="<%= ctx %>/payment_history">
          <input type="hidden" name="action"  value="retry">
          <input type="hidden" name="orderId" value="<%= payment.getOrderId() %>">
          <button type="submit" class="btn btn-primary btn-block">🔄 Thử lại với VNPay</button>
        </form>
        <form method="post" action="<%= ctx %>/checkout">
          <input type="hidden" name="orderId"       value="<%= payment.getOrderId() %>">
          <input type="hidden" name="paymentMethod" value="cod">
          <button type="submit" class="btn btn-outline btn-block">💵 Đổi sang thanh toán COD</button>
        </form>
        <form method="post" action="<%= ctx %>/payment_history"
              onsubmit="return confirm('Bạn có chắc muốn hủy giao dịch này?')">
          <input type="hidden" name="action"  value="cancel">
          <input type="hidden" name="orderId" value="<%= payment.getOrderId() %>">
          <button type="submit" class="btn btn-danger-outline btn-block">✕ Hủy giao dịch</button>
        </form>
  <% } %>
        <a href="<%= ctx %>/cart" class="btn btn-ghost btn-block">← Quay lại giỏ hàng</a>
      </div>
    </div>

<% } %>
  </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
