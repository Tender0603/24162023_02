package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.service.*;

@WebServlet("/cart")
public class CartController_24162023 extends BaseController_24162023 {
  private final CartService_24162023 service = new CartService_24162023();
  protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
    try {
      var user = (User_24162023) q.getSession().getAttribute("user");
      q.setAttribute("cart",service.find(user.getId()));
      q.setAttribute("cartMessage",q.getSession().getAttribute("cartMessage"));
      q.setAttribute("cartError",q.getSession().getAttribute("cartError"));
      q.getSession().removeAttribute("cartMessage"); q.getSession().removeAttribute("cartError");
      view(q,s,"cart");
    } catch (Exception e) { unavailable(q,s,e); }
  }
  protected void doPost(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
    try {
      var user = (User_24162023) q.getSession().getAttribute("user");
      int bookId = PurchaseValidation_24162023.positive(q.getParameter("bookId"));
      String action = q.getParameter("action");
      if (action == null) throw new IllegalArgumentException("Thiếu thao tác giỏ hàng.");
      switch (action) {
        case "add" -> service.add(user.getId(),bookId,PurchaseValidation_24162023.positive(q.getParameter("quantity")));
        case "update" -> service.update(user.getId(),bookId,PurchaseValidation_24162023.positive(q.getParameter("quantity")));
        case "increase" -> service.increment(user.getId(),bookId,1);
        case "decrease" -> service.increment(user.getId(),bookId,-1);
        case "remove" -> service.remove(user.getId(),bookId);
        default -> throw new IllegalArgumentException("Thao tác giỏ hàng không hợp lệ.");
      }
      q.getSession().setAttribute("cartMessage","Đã cập nhật giỏ hàng.");
      redirect(q,s,"/cart");
    } catch (IllegalArgumentException e) {
      q.getSession().setAttribute("cartError",e.getMessage()); redirect(q,s,"/cart");
    } catch (Exception e) { unavailable(q,s,e); }
  }
}
