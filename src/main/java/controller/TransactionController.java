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
 * UC-24: View Transactions — admin transaction monitoring with filters + pagination.
 */
public class TransactionController extends HttpServlet {
 
    private static final int PAGE_SIZE = 10;
    private final PaymentService svc = new PaymentService();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        String statusFilter = req.getParameter("status");
        String methodFilter = req.getParameter("method");
        String pageStr      = req.getParameter("page");
        int    page         = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
        if (page < 1) page = 1;
 
        try {
            int total     = svc.countAllPayments(statusFilter, methodFilter);
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            List<Payment> payments = svc.getAllPayments(statusFilter, methodFilter, page, PAGE_SIZE);
 
            req.setAttribute("payments",     payments);
            req.setAttribute("total",        total);
            req.setAttribute("page",         page);
            req.setAttribute("totalPages",   totalPages);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("methodFilter", methodFilter);
 
            req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
 
        } catch (SQLException e) {
            log("TransactionController error: " + e.getMessage(), e);
            req.setAttribute("error", "Không thể tải danh sách giao dịch.");
            req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
        }
    }
}
 