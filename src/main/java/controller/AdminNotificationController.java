package controller;

import model.Notification;
import service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * UC-Notify (Admin): Xem toàn bộ lịch sử thông báo đã gửi.
 * GET /admin/notifications?type=&page=
 */
public class AdminNotificationController extends HttpServlet {

    private static final int PAGE_SIZE = 20;
    private final NotificationService svc = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String typeFilter = req.getParameter("type");
        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        if (page < 1) page = 1;

        try {
            int total      = svc.countAll(typeFilter);
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            List<Notification> list = svc.getAll(typeFilter, page, PAGE_SIZE);

            req.setAttribute("notifications", list);
            req.setAttribute("total",         total);
            req.setAttribute("page",          page);
            req.setAttribute("totalPages",    Math.max(totalPages, 1));
            req.setAttribute("typeFilter",    typeFilter != null ? typeFilter : "");
            req.getRequestDispatcher("/admin/notifications.jsp").forward(req, resp);
        } catch (SQLException e) {
            log("AdminNotificationController error: " + e.getMessage(), e);
            req.setAttribute("error", "Không thể tải danh sách thông báo.");
            req.getRequestDispatcher("/admin/notifications.jsp").forward(req, resp);
        }
    }
}
