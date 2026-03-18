<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.CartItem, java.math.BigDecimal, java.text.NumberFormat, java.util.Locale" %>
<%
    List<CartItem> cart = (List<CartItem>) request.getAttribute("cart");
    BigDecimal subtotal = (BigDecimal) request.getAttribute("subtotal");
    BigDecimal shipping = (BigDecimal) request.getAttribute("shipping");
    BigDecimal total    = (BigDecimal) request.getAttribute("total");
    String error        = (String)     request.getAttribute("error");
    String ctx          = request.getContextPath();
    NumberFormat nf     = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Thanh toán — Snack Store</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="<%= ctx %>/style.css">
</head>
<body>

<nav class="navbar">
  <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
  <ul class="navbar-nav">
    <li><a href="<%= ctx %>/cart">Giỏ hàng</a></li>
    <li><a href="<%= ctx %>/checkout" class="active">Thanh toán</a></li>
  </ul>
  <div class="navbar-end">
    <div class="navbar-avatar">A</div>
    <span class="navbar-user">Nguyễn Văn A</span>
  </div>
</nav>

<div class="page-wrapper">

  <!-- Progress steps -->
  <div class="steps">
    <div class="step done"><div class="step-num">✓</div><span>Giỏ hàng</span></div>
    <div class="step-line"></div>
    <div class="step active"><div class="step-num">2</div><span>Thanh toán</span></div>
    <div class="step-line"></div>
    <div class="step"><div class="step-num">3</div><span>Xác nhận</span></div>
  </div>

<% if (error != null && !error.isEmpty()) { %>
  <div class="alert alert-danger"><span class="alert-icon">⚠</span><%= error %></div>
<% } %>

  <form method="post" action="<%= ctx %>/checkout" id="checkout-form">
  <div class="layout-checkout">

    <div class="col-stack">

      <!-- Địa chỉ giao hàng -->
      <div class="card">
        <div class="card-header"><span class="card-header-icon">📍</span><h2>Địa chỉ giao hàng</h2></div>
        <div class="card-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Họ và tên</label>
              <input class="form-control" type="text" value="Nguyễn Văn A" readonly>
            </div>
            <div class="form-group">
              <label class="form-label">Số điện thoại</label>
              <input class="form-control" type="text" value="0901 234 567" readonly>
            </div>
          </div>
          <div class="form-group" style="margin-bottom:0">
            <label class="form-label">Địa chỉ nhận hàng</label>
            <input class="form-control" type="text" value="120 Yên Lãng, Đống Đa, Hà Nội" readonly>
          </div>
          <p style="margin-top:.75rem;font-size:13px;color:var(--text-2)">
            <a href="#" style="color:var(--accent);font-weight:500">Thay đổi địa chỉ →</a>
          </p>
        </div>
      </div>

      <!-- Phương thức thanh toán -->
      <div class="card">
        <div class="card-header"><span class="card-header-icon">💳</span><h2>Phương thức thanh toán</h2></div>
        <div class="card-body">
          <div class="payment-options">

            <label class="payment-option" id="opt-vnpay">
              <input type="radio" name="paymentMethod" value="vnpay" onchange="selectPay(this)">
              <span class="payment-icon">🏦</span>
              <span class="payment-label">Thanh toán online</span>
              <span class="payment-desc">VNPay – Internet Banking, ATM, QR Code</span>
              <div class="payment-chips">
                <span class="payment-chip">VNPAY</span>
                <span class="payment-chip">Vietcombank</span>
                <span class="payment-chip">Techcombank</span>
                <span class="payment-chip">+30 ngân hàng</span>
              </div>
            </label>

            <label class="payment-option" id="opt-cod">
              <input type="radio" name="paymentMethod" value="cod" onchange="selectPay(this)">
              <span class="payment-icon">💵</span>
              <span class="payment-label">Thanh toán khi nhận hàng</span>
              <span class="payment-desc">Trả tiền mặt khi nhận hàng (COD). Không mất phí thêm.</span>
            </label>

          </div>
          <p id="method-err" style="color:var(--danger);font-size:13px;margin-top:.6rem;display:none">
            ⚠ Vui lòng chọn phương thức thanh toán.
          </p>
        </div>
      </div>

      <!-- Ghi chú -->
      <div class="card">
        <div class="card-header"><span class="card-header-icon">📝</span><h2>Ghi chú đơn hàng</h2></div>
        <div class="card-body">
          <div class="form-group" style="margin-bottom:0">
            <textarea class="form-control" name="note" placeholder="Ghi chú thêm cho người giao hàng (tùy chọn)..."></textarea>
          </div>
        </div>
      </div>

    </div>

    <!-- Order summary -->
    <div>
      <div class="card" style="position:sticky;top:calc(var(--nav-h) + 16px)">
        <div class="card-header"><span class="card-header-icon">🧾</span><h2>Đơn hàng của bạn</h2></div>
        <div class="card-body">

<% if (cart != null) { for (CartItem item : cart) { %>
          <div class="summary-row">
            <span style="display:flex;align-items:center;gap:6px">
              <span style="font-size:11px;color:var(--text-3);background:var(--bg);border:1px solid var(--border);border-radius:4px;padding:1px 6px;font-weight:600">×<%= item.getQuantity() %></span>
              <%= item.getProductName() %>
            </span>
            <span class="val"><%= nf.format(item.getSubtotal()) %> ₫</span>
          </div>
<% } } %>

          <hr class="divider">
          <div class="summary-row"><span>Tạm tính</span><span class="val"><%= subtotal != null ? nf.format(subtotal) : "0" %> ₫</span></div>
          <div class="summary-row"><span>Phí vận chuyển</span><span class="val"><%= shipping != null ? nf.format(shipping) : "0" %> ₫</span></div>
          <div class="summary-row total">
            <span>Tổng thanh toán</span>
            <span class="val" style="color:var(--accent);font-size:1.1rem"><%= total != null ? nf.format(total) : "0" %> ₫</span>
          </div>
          <hr class="divider" style="margin:1rem 0">

          <button type="submit" class="btn btn-primary btn-block btn-lg" id="submit-btn">
            Đặt hàng ngay →
          </button>
          <a href="<%= ctx %>/cart" class="btn btn-outline btn-block" style="margin-top:.6rem">
            ← Quay lại giỏ hàng
          </a>
          <p style="font-size:12px;color:var(--text-3);text-align:center;margin-top:.85rem;line-height:1.5">
            🔒 Thông tin được mã hóa SSL an toàn.<br>
            Bằng việc đặt hàng, bạn đồng ý với <a href="#" style="color:var(--accent)">điều khoản dịch vụ</a>.
          </p>
        </div>
      </div>
    </div>

  </div>
  </form>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

<script>
function selectPay(radio) {
  document.getElementById('method-err').style.display = 'none';
  document.querySelectorAll('.payment-option').forEach(el => el.classList.remove('selected'));
  radio.closest('.payment-option').classList.add('selected');
}
document.getElementById('checkout-form').addEventListener('submit', function(e) {
  if (!document.querySelector('input[name="paymentMethod"]:checked')) {
    e.preventDefault();
    document.getElementById('method-err').style.display = 'block';
    document.querySelector('.payment-options').scrollIntoView({ behavior: 'smooth', block: 'center' });
    return;
  }
  const btn = document.getElementById('submit-btn');
  btn.textContent = 'Đang xử lý...';
  btn.disabled = true;
});
</script>
</body>
</html>
