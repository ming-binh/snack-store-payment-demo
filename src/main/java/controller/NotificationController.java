package controller;

import model.Notification;
import service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * UC-Notify: Khách hàng xem và quản lý thông báo của mình.
 * GET  /notifications          — danh sách thông báo
 * POST /notifications?action=read&id=X     — đánh dấu 1 thông báo đã đọc
 * POST /notifications?action=readAll       — đánh dấu tất cả đã đọc
 */
public class NotificationController extends HttpServlet {

    private final NotificationService svc = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) { userId = 1; session.setAttribute("userId", 1); }

        try {
            List<Notification> notifications = svc.getForUser(userId);
            int unread = (int) notifications.stream().filter(n -> !n.isRead()).count();

            req.setAttribute("notifications", notifications);
            req.setAttribute("unreadCount",   unread);
            req.getRequestDispatcher("/notifications.jsp").forward(req, resp);
        } catch (Exception e) {
            log("NotificationController GET error: " + e.getMessage(), e);
            req.setAttribute("error", "Không thể tải thông báo.");
            req.getRequestDispatcher("/notifications.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) userId = 1;

        String action = req.getParameter("action");
        try {
            if ("read".equals(action)) {
                int nid = Integer.parseInt(req.getParameter("id"));
                svc.markRead(nid, userId);
            } else if ("readAll".equals(action)) {
                svc.markAllRead(userId);
            }
        } catch (SQLException | NumberFormatException e) {
            log("NotificationController POST error: " + e.getMessage(), e);
        }
        resp.sendRedirect(req.getContextPath() + "/notifications");
    }
}
