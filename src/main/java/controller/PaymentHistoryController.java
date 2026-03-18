package controller;
 
import model.Payment;
import service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
 
/**
 * UC-27: View Payment History – customer's own transactions.
 * UC-26: Retry failed payment from this page.
 * UC-28: Cancel pending payment from this page.
 */
public class PaymentHistoryController extends HttpServlet {
 
    private final PaymentService svc = new PaymentService();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) { userId = 1; } // demo fallback
 
        try {
            List<Payment> payments = svc.getPaymentHistory(userId);
            req.setAttribute("payments", payments);
            req.getRequestDispatcher("/payment_history.jsp").forward(req, resp);
        } catch (SQLException e) {
            log("PaymentHistoryController error: " + e.getMessage(), e);
            req.setAttribute("error", "Không thể tải lịch sử thanh toán.");
            req.getRequestDispatcher("/payment_history.jsp").forward(req, resp);
        }
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        String action  = req.getParameter("action");
        int    orderId = Integer.parseInt(req.getParameter("orderId"));
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) userId = 1;
 
        try {
            if ("retry".equals(action)) {
                // UC-26: retry VNPay
                String ip = util.VnpayUtil.getClientIp(req);
                String url = svc.retryVnpay(orderId, userId, ip);
                if (url != null) {
                    resp.sendRedirect(url);
                    return;
                }
                req.setAttribute("error", "Không thể thực hiện lại thanh toán.");
 
            } else if ("cancel".equals(action)) {
                // UC-28: cancel pending payment
                svc.cancelPendingPayment(orderId);
            }
        } catch (SQLException e) {
            log("PaymentHistoryController POST error: " + e.getMessage(), e);
        }
        resp.sendRedirect(req.getContextPath() + "/payment_history");
    }
}
 