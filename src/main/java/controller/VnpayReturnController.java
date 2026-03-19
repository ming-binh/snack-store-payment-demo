package controller;
 /**
 * Author: HE190438 Thân Bình Minh
 * Created: 2026-03-19
 */
import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.PaymentService;
import service.PaymentService.PaymentResult;
 
/**
 * UC-23: Confirm Payment Result
 * VNPay redirects customer here after payment attempt.
 */
public class VnpayReturnController extends HttpServlet {
 
    private final PaymentService paymentService = new PaymentService();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        try {
            PaymentResult result = paymentService.handleVnpayReturn(req.getParameterMap());
 
            req.setAttribute("paymentResult", result);
            req.setAttribute("success",  result.success);
            req.setAttribute("message",  result.message);
            req.setAttribute("payment",  result.payment);
 
            if (result.success) {
                // Clear cart on success
                req.getSession().removeAttribute("cart");
                req.setAttribute("orderId",
                    result.payment != null ? result.payment.getOrderId() : 0);
            }
 
            req.getRequestDispatcher("/payment_result.jsp").forward(req, resp);
 
        } catch (SQLException e) {
            log("VnpayReturnController error: " + e.getMessage(), e);
            req.setAttribute("success", false);
            req.setAttribute("message", "Lỗi hệ thống khi xử lý kết quả thanh toán.");
            req.getRequestDispatcher("/payment_result.jsp").forward(req, resp);
        }
    }
}