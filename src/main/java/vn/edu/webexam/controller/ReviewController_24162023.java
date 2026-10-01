package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.service.RatingService_24162023;

@WebServlet("/review")
public class ReviewController_24162023 extends BaseController_24162023 {
  protected void doPost(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    int id;
    try {
      id = positive(q.getParameter("bookid"), 0);
      if (id == 0) throw new IllegalArgumentException();
    } catch (IllegalArgumentException e) {
      s.sendError(400);
      return;
    }
    try {
      var user = (User_24162023) q.getSession().getAttribute("user");
      new RatingService_24162023()
          .save(
              user.getId(),
              id,
              positive(q.getParameter("rating"), 0),
              q.getParameter("review_text"));
      redirect(q, s, "/book-detail?id=" + id + "&saved=1");
    } catch (IllegalArgumentException e) {
      q.getSession().setAttribute("reviewError", e.getMessage());
      redirect(q, s, "/book-detail?id=" + id);
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }
}
