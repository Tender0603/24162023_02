package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.*;
import vn.edu.webexam.service.OrderService_24162023;

@WebServlet(urlPatterns={"/orders", "/orders/detail"})
public class OrderController_24162023 extends BaseController_24162023 {
  private final OrderService_24162023 service = new OrderService_24162023();

  protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
    s.setHeader("Cache-Control", "no-store");
    var user = (User_24162023) q.getSession().getAttribute("user");
    try {
      if ("/orders/detail".equals(q.getServletPath())) {
        long id;
        try { id = Long.parseLong(q.getParameter("id")); }
        catch (NumberFormatException e) { s.sendError(400); return; }
        if (id <= 0) { s.sendError(400); return; }
        var order = service.detail(user.getId(), id);
        if (order == null) { s.sendError(404); return; }
        q.setAttribute("order", order);
        view(q, s, "order-detail");
      } else {
        OrderStatus_24162023 status;
        try { status = OrderService_24162023.parseStatus(q.getParameter("status")); }
        catch (IllegalArgumentException e) { s.sendError(400); return; }
        q.setAttribute("statuses", OrderStatus_24162023.values());
        q.setAttribute("selectedStatus", status);
        q.setAttribute("orders", service.history(user.getId(), status));
        view(q, s, "order-history");
      }
    } catch (Exception e) { unavailable(q, s, e); }
  }
}
