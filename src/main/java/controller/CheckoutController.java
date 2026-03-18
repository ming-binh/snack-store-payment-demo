package controller;
 
import model.CartItem;
import service.PaymentService;
import util.VnpayUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
 
public class CheckoutController extends HttpServlet {
 
    private final PaymentService paymentService = new PaymentService();
 
    /** UC-19: Initiate Payment — show checkout page with order summary */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        HttpSession session = req.getSession();
 
        // Simulate logged-in user (replace with real auth session attribute)
        Integer userId    = (Integer) session.getAttribute("userId");
        Integer addressId = (Integer) session.getAttribute("addressId");
        if (userId == null)    { userId = 1; session.setAttribute("userId", 1); }
        if (addressId == null) { addressId = 1; session.setAttribute("addressId", 1); }
 
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
 
        BigDecimal subtotal = cart.stream()
            .map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = new BigDecimal("15000");
        BigDecimal total    = subtotal.add(shipping);
 
        req.setAttribute("cart",     cart);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("shipping", shipping);
        req.setAttribute("total",    total);
 
        req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
    }
 
    /** UC-20: Select Payment Method — handle form POST */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        HttpSession session  = req.getSession();
        String  method       = req.getParameter("paymentMethod");
        String  note         = req.getParameter("note");
        int     userId       = (int) session.getAttribute("userId");
        int     addressId    = (int) session.getAttribute("addressId");
 
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
 
        BigDecimal subtotal = cart.stream()
            .map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = new BigDecimal("15000");
        BigDecimal total    = subtotal.add(shipping);
 
        try {
            // UC-19: Create order first
            model.Order order = new model.Order();
            order.setUserId(userId);
            order.setAddressId(addressId);
            order.setStatus("pending");
            order.setSubtotal(subtotal);
            order.setShippingFee(shipping);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setTotalAmount(total);
            order.setNote(note);
            int orderId = paymentService.getDao().createOrder(order);
 
            session.setAttribute("currentOrderId", orderId);
 
            if ("vnpay".equals(method)) {
                // UC-21: Online Banking via VNPay
                String ipAddr  = VnpayUtil.getClientIp(req);
                String payUrl  = paymentService.initiateVnpay(orderId, ipAddr);
                resp.sendRedirect(payUrl);
 
            } else if ("cod".equals(method)) {
                // UC-22: Cash on Delivery
                paymentService.processCod(orderId);
                session.removeAttribute("cart");
                resp.sendRedirect(req.getContextPath()
                    + "/payment_result.jsp?status=cod&orderId=" + orderId);
 
            } else {
                req.setAttribute("error", "Vui lòng chọn phương thức thanh toán.");
                doGet(req, resp);
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
            log("CheckoutController error: " + e.getMessage(), e);
            req.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            doGet(req, resp);
        }
    }
}