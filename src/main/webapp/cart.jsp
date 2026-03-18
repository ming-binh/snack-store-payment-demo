<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.CartItem, java.math.BigDecimal, java.text.NumberFormat, java.util.Locale" %>
<%
    List<CartItem> cart = (List<CartItem>) request.getAttribute("cart");
    BigDecimal subtotal = (BigDecimal) request.getAttribute("subtotal");
    BigDecimal shipping = (BigDecimal) request.getAttribute("shipping");
    BigDecimal total    = (BigDecimal) request.getAttribute("total");
    String ctx          = request.getContextPath();
    NumberFormat nf     = NumberFormat.getInstance(new Locale("vi","VN"));
    nf.setMaximumFractionDigits(0);
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>Giỏ hàng — Snack Store</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="stylesheet" href="<%= ctx %>/style.css">
    </head>
    <body>

        <nav class="navbar">
            <a class="navbar-brand" href="<%= ctx %>/">🍿 Snack Store</a>
            <ul class="navbar-nav">
                <li><a href="<%= ctx %>/cart" class="active">Giỏ hàng</a></li>
                <li><a href="<%= ctx %>/payment_history">Lịch sử</a></li>
                <li><a href="<%= ctx %>/notifications">🔔 Thông báo</a></li>
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
                <span>Giỏ hàng</span>
            </div>

            <div class="page-header">
                <div>
                    <h1>Giỏ hàng của bạn</h1>
                    <p>Kiểm tra lại sản phẩm trước khi đặt hàng</p>
                </div>
            </div>

            <% if (cart == null || cart.isEmpty()) { %>
            <div class="card">
                <div class="card-body empty-state">
                    <span class="empty-icon">🛒</span>
                    <h3>Giỏ hàng đang trống</h3>
                    <p>Thêm sản phẩm vào giỏ để bắt đầu mua sắm nhé!</p>
                    <a href="<%= ctx %>/" class="btn btn-primary" style="margin-top:1.25rem">Khám phá sản phẩm →</a>
                </div>
            </div>

            <% } else { %>
            <div class="layout-checkout">

                <!-- Danh sách sản phẩm -->
                <div class="card">
                    <div class="card-header">
                        <span class="card-header-icon"></span>
                        <h2>Sản phẩm (<%= cart.size() %> mặt hàng)</h2>
                    </div>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th colspan="2">Sản phẩm</th>
                                    <th style="text-align:center">Số lượng</th>
                                    <th style="text-align:right">Đơn giá</th>
                                    <th style="text-align:right">Thành tiền</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (CartItem item : cart) { %>
                                <tr>
                                    <td style="width:68px">
                                        <div class="item-thumb">
                                            <% if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) { %>
                                            <img src="<%= item.getThumbnailUrl() %>" alt="">
                                            <% } else { %>🍬<% } %>
                                        </div>
                                    </td>
                                    <td>
                                        <div style="font-weight:600"><%= item.getProductName() %></div>
                                        <div style="font-size:12px;color:var(--text-3);margin-top:2px">Mã: #<%= item.getProductId() %></div>
                                    </td>
                                    <td style="text-align:center">
                                        <form method="post" action="<%= ctx %>/cart" style="display:inline">
                                            <input type="hidden" name="action" value="update">
                                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
                                            <input class="qty-input" type="number" name="quantity"
                                                   value="<%= item.getQuantity() %>" min="1" max="99"
                                                   onchange="this.form.submit()">
                                        </form>
                                    </td>
                                    <td style="text-align:right;color:var(--text-2)"><%= nf.format(item.getUnitPrice()) %> ₫</td>
                                    <td style="text-align:right;font-weight:700"><%= nf.format(item.getSubtotal()) %> ₫</td>
                                    <td>
                                        <form method="post" action="<%= ctx %>/cart">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
                                            <button class="btn btn-sm btn-danger-outline btn-icon" type="submit" title="Xóa">✕</button>
                                        </form>
                                    </td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                    <div class="card-footer">
                        <span><%= cart.size() %> sản phẩm trong giỏ</span>
                        <form method="post" action="<%= ctx %>/cart">
                            <input type="hidden" name="action" value="clear">
                            <button class="btn btn-sm btn-ghost" type="submit">Xóa tất cả</button>
                        </form>
                    </div>
                </div>

                <!-- Order summary -->
                <div class="col-stack" style="position:sticky;top:calc(var(--nav-h) + 16px)">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-header-icon"></span>
                            <h2>Tóm tắt đơn hàng</h2>
                        </div>
                        <div class="card-body">
                            <div class="summary-row"><span>Tạm tính</span><span class="val"><%= nf.format(subtotal) %> ₫</span></div>
                            <div class="summary-row"><span>Phí vận chuyển</span><span class="val"><%= nf.format(shipping) %> ₫</span></div>
                            <div class="summary-row"><span>Giảm giá</span><span class="val" style="color:var(--success)">—</span></div>
                            <div class="summary-row total">
                                <span>Tổng cộng</span>
                                <span class="val" style="color:var(--accent);font-size:1.15rem"><%= nf.format(total) %> ₫</span>
                            </div>

                            <div style="margin-top:1.25rem;display:flex;flex-direction:column;gap:.6rem">
                                <a href="<%= ctx %>/checkout" class="btn btn-primary btn-block btn-lg">
                                    Tiến hành thanh toán →
                                </a>
                                <a href="<%= ctx %>/" class="btn btn-outline btn-block">
                                    ← Tiếp tục mua sắm
                                </a>
                            </div>

                            <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid var(--border)">
                                <div style="font-size:12px;color:var(--text-3);margin-bottom:.6rem;font-weight:700;text-transform:uppercase;letter-spacing:.04em">Mã giảm giá</div>
                                <div style="display:flex;gap:.5rem">
                                    <input class="form-control" type="text" placeholder="Nhập mã..." style="font-size:13px;padding:8px 12px">
                                    <button class="btn btn-secondary btn-sm">Áp dụng</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-body">
                            <div class="trust-list">
                                <div class="trust-item"><span class="icon">🔒</span> Thanh toán bảo mật SSL 256-bit</div>
                                <div class="trust-item"><span class="icon">🔄</span> Đổi trả miễn phí trong 7 ngày</div>
                                <div class="trust-item"><span class="icon">🚚</span> Giao hàng nhanh 2–3 ngày</div>
                                <div class="trust-item"><span class="icon">🎁</span> Quà tặng khi đơn từ 300.000 ₫</div>
                            </div>
                        </div>
                    </div>

                    <!-- Gợi ý sản phẩm -->
                    <div class="card">
                        <div class="card-header"><span class="card-header-icon">✨</span><h2>Có thể bạn thích</h2></div>
                        <div class="card-body" style="display:flex;flex-direction:column;gap:.75rem">
                            <div style="display:flex;align-items:center;gap:.75rem">
                                <div class="item-thumb" style="background:#FEF3E8;font-size:1.6rem">🍫</div>
                                <div style="flex:1"><div style="font-size:13px;font-weight:600">Socola đen 72%</div><div style="font-size:12px;color:var(--text-2)">45.000 ₫</div></div>
                                <button class="btn btn-sm btn-outline">+</button>
                            </div>
                            <div style="display:flex;align-items:center;gap:.75rem">
                                <div class="item-thumb" style="background:#EDFBF3;font-size:1.6rem">🧃</div>
                                <div style="flex:1"><div style="font-size:13px;font-weight:600">Nước ép xoài tươi</div><div style="font-size:12px;color:var(--text-2)">28.000 ₫</div></div>
                                <button class="btn btn-sm btn-outline">+</button>
                            </div>
                            <div style="display:flex;align-items:center;gap:.75rem">
                                <div class="item-thumb" style="background:#FEF0F0;font-size:1.6rem">🍭</div>
                                <div style="flex:1"><div style="font-size:13px;font-weight:600">Kẹo dừa Bến Tre</div><div style="font-size:12px;color:var(--text-2)">32.000 ₫</div></div>
                                <button class="btn btn-sm btn-outline">+</button>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <% } %>
        </div>

        <%@ include file="/WEB-INF/fragments/footer.jsp" %>
    </body>
</html>
