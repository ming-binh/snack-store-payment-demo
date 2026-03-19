package controller;
/**
 * Author: HE190438 Thân Bình Minh
 * Created: 2026-03-19
 */
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Refund;
import service.PaymentService;

/**
 * UC-25: Refund Payment — admin reviews and processes refund requests.
 */
public class RefundController extends HttpServlet {

    private final PaymentService svc = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String statusFilter = req.getParameter("status");
        HttpSession session = req.getSession();

        String refundMsg   = (String) session.getAttribute("refundMsg");
        String refundError = (String) session.getAttribute("refundError");
        session.removeAttribute("refundMsg");
        session.removeAttribute("refundError");

        try {
            List<Refund> refunds = svc.getAllRefunds(statusFilter);
            req.setAttribute("refunds",      refunds);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("refundMsg",    refundMsg);
            req.setAttribute("refundError",  refundError);
            req.getRequestDispatcher("/admin/refunds.jsp").forward(req, resp);
        } catch (SQLException e) {
            log("RefundController GET error: " + e.getMessage(), e);
            req.setAttribute("error", "Khong the tai danh sach hoan tien.");
            req.getRequestDispatcher("/admin/refunds.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        HttpSession session = req.getSession();
        Integer adminId = (Integer) session.getAttribute("userId");
        if (adminId == null) adminId = 1;

        try {
            if ("request".equals(action)) {
                int        paymentId = Integer.parseInt(req.getParameter("paymentId"));
                BigDecimal amount    = new BigDecimal(req.getParameter("amount"));
                String     reason    = req.getParameter("reason");
                svc.requestRefund(paymentId, amount, reason);
                session.setAttribute("refundMsg", "Yeu cau hoan tien da duoc gui thanh cong.");

            } else if ("approve".equals(action)) {
                int refundId = Integer.parseInt(req.getParameter("refundId"));
                svc.approveRefund(refundId, adminId);

            } else if ("reject".equals(action)) {
                int refundId = Integer.parseInt(req.getParameter("refundId"));
                svc.rejectRefund(refundId, adminId);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("refundError", e.getMessage());
        } catch (SQLException e) {
            log("RefundController POST error: " + e.getMessage(), e);
            session.setAttribute("refundError", "Loi he thong, vui long thu lai.");
        }

        if ("request".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/payment_history");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/refunds");
        }
    }
}
