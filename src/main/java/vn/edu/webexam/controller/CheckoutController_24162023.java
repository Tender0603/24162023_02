package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import vn.edu.webexam.model.*;
import vn.edu.webexam.service.*;

@WebServlet(urlPatterns={"/checkout","/checkout/success"})
public class CheckoutController_24162023 extends BaseController_24162023 {
  private final CheckoutService_24162023 service = new CheckoutService_24162023();
  private final CartService_24162023 carts = new CartService_24162023();
  protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
    try {
      var user = (User_24162023) q.getSession().getAttribute("user");
      if ("/checkout/success".equals(q.getServletPath())) {
        long id;
        try { id = Long.parseLong(q.getParameter("id")); }
        catch (RuntimeException e) { s.sendError(400); return; }
        var order = service.find(user.getId(),id);
        if (order == null) { s.sendError(404); return; }
        q.setAttribute("order",order); view(q,s,"checkout-success"); return;
      }
      q.setAttribute("form",new CheckoutForm_24162023(user.getFullname(),"",""));
      render(q,s,user.getId());
    } catch (Exception e) { unavailable(q,s,e); }
  }
  protected void doPost(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
    if (!"/checkout".equals(q.getServletPath())) { s.sendError(405); return; }
    var user = (User_24162023) q.getSession().getAttribute("user");
    var form = new CheckoutForm_24162023(q.getParameter("receiverName"),q.getParameter("receiverPhone"),q.getParameter("shippingAddress"));
    try {
      String key = CheckoutService_24162023.validKey(q.getParameter("checkoutKey"));
      Long existing = service.existing(user.getId(),key);
      if (existing == null) {
        if (!key.equals(q.getSession().getAttribute("checkoutKey")))
          throw new IllegalArgumentException("Phiên thanh toán đã thay đổi. Hãy dùng biểu mẫu hiện tại.");
        existing = service.place(user.getId(),key,form);
      }
      synchronized (q.getSession()) {
        if (key.equals(q.getSession().getAttribute("checkoutKey"))) q.getSession().removeAttribute("checkoutKey");
      }
      redirect(q,s,"/checkout/success?id=" + existing);
    } catch (IllegalArgumentException e) {
      q.setAttribute("error",e.getMessage()); q.setAttribute("form",form); s.setStatus(400);
      try { render(q,s,user.getId()); } catch (Exception ex) { unavailable(q,s,ex); }
    } catch (Exception e) { unavailable(q,s,e); }
  }
  private void render(HttpServletRequest q, HttpServletResponse s, int userId) throws Exception {
    var cart = carts.find(userId);
    if (cart.isEmpty()) {
      q.getSession().setAttribute("cartError","Giỏ hàng trống, không thể thanh toán.");
      redirect(q,s,"/cart"); return;
    }
    synchronized (q.getSession()) {
      if (q.getSession().getAttribute("checkoutKey") == null)
        q.getSession().setAttribute("checkoutKey",UUID.randomUUID().toString());
      q.setAttribute("checkoutKey",q.getSession().getAttribute("checkoutKey"));
    }
    q.setAttribute("cart",cart); view(q,s,"checkout");
  }
}
