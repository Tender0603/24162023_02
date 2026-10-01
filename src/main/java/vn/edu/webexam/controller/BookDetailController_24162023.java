package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.service.*;

@WebServlet("/book-detail")
public class BookDetailController_24162023 extends BaseController_24162023 {
  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    try {
      int id = positive(q.getParameter("id"), 0);
      var book = new BookService_24162023().find(id);
      if (book == null) {
        s.sendError(404);
        return;
      }
      var reviews = new RatingService_24162023().forBook(id);
      q.setAttribute("book", book);
      q.setAttribute("reviews", reviews);
      var user = (User_24162023) q.getSession().getAttribute("user");
      if (user != null)
        for (var r : reviews) if (r.getUserid() == user.getId()) q.setAttribute("myReview", r);
      q.setAttribute("error", q.getSession().getAttribute("reviewError"));
      q.getSession().removeAttribute("reviewError");
      view(q, s, "book-detail");
    } catch (IllegalArgumentException e) {
      s.sendError(400);
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }
}
