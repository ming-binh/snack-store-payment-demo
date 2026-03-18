package controller;
 
import model.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
public class CartController extends HttpServlet {
 
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            // ── Demo items – in a real app load from DB cart table ────────
            cart.add(new CartItem(1, "Bắp rang bơ vị phô mai", new BigDecimal("35000"), 2, null));
            cart.add(new CartItem(2, "Khoai tây chiên vị BBQ",  new BigDecimal("28000"), 1, null));
            cart.add(new CartItem(3, "Snack mực cay vừa",       new BigDecimal("22000"), 3, null));
            session.setAttribute("cart", cart);
        }
 
        BigDecimal subtotal = cart.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = new BigDecimal("15000");
        BigDecimal total    = subtotal.add(shipping);
 
        req.setAttribute("cart",     cart);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("shipping", shipping);
        req.setAttribute("total",    total);
        req.getRequestDispatcher("/cart.jsp").forward(req, resp);
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();
 
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
 
        if ("remove".equals(action)) {
            int pid = Integer.parseInt(req.getParameter("productId"));
            cart.removeIf(i -> i.getProductId() == pid);
            session.setAttribute("cart", cart);
        } else if ("update".equals(action)) {
            int pid = Integer.parseInt(req.getParameter("productId"));
            int qty = Integer.parseInt(req.getParameter("quantity"));
            for (CartItem i : cart) {
                if (i.getProductId() == pid) { i.setQuantity(qty); break; }
            }
            session.setAttribute("cart", cart);
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
 