<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Footer fragment – include vào mọi trang: <%@ include file="/WEB-INF/fragments/footer.jsp" %> --%>

<footer class="site-footer">
  <div class="footer-main">
    <div class="footer-brand">
      <span class="brand-name">🍿 Snack Store</span>
      <p>Cửa hàng đồ ăn vặt trực tuyến uy tín hàng đầu. Giao hàng nhanh, đổi trả dễ dàng, thanh toán an toàn.</p>
      
      <div class="footer-social">
        <a href="#" title="Facebook">f</a>
        <a href="#" title="Instagram">in</a>
        <a href="#" title="YouTube">▶</a>
        <a href="#" title="TikTok">♪</a>
      </div>
    </div>

    <div class="footer-col">
      <h3>Mua sắm</h3>
      <ul>
        <li><a href="#">Đồ ăn vặt</a></li>
        <li><a href="#">Bánh kẹo</a></li>
        <li><a href="#">Nước uống</a></li>
        <li><a href="#">Combo tiết kiệm</a></li>
        <li><a href="#">Hàng mới về</a></li>
      </ul>
    </div>

    <div class="footer-col">
      <h3>Hỗ trợ</h3>
      <ul>
        <li><a href="#">Chính sách đổi trả</a></li>
        <li><a href="#">Hướng dẫn thanh toán</a></li>
        <li><a href="${pageContext.request.contextPath}/notifications">Thông báo của tôi</a></li>
        <li><a href="#">Theo dõi đơn hàng</a></li>
        <li><a href="#">Câu hỏi thường gặp</a></li>
        <li><a href="#">Liên hệ</a></li>
      </ul>
    </div>

    <div class="footer-col">
      <h3>Công ty</h3>
      <ul>
        <li><a href="#">Giới thiệu</a></li>
        <li><a href="#">Tuyển dụng</a></li>
        <li><a href="#">Đối tác</a></li>
        <li><a href="#">Blog</a></li>
        <li><a href="#">Báo chí</a></li>
      </ul>
    </div>
  </div>

  <div class="footer-bottom">
    <div>© 2025 Snack Store. Được bảo hộ.</div>

    <div class="footer-payment-icons">
      <span style="font-size:12px;color:var(--text-3);margin-right:4px">
        Thanh toán:
      </span>
      <span class="footer-payment-icon">VNPAY</span>
      <span class="footer-payment-icon">VCB</span>
      <span class="footer-payment-icon">TCB</span>
      <span class="footer-payment-icon">COD</span>
    </div>

    <div class="footer-bottom-links">
      <a href="#">Điều khoản</a>
      <a href="#">Bảo mật</a>
      <a href="#">Cookie</a>
    </div>
  </div>
</footer>