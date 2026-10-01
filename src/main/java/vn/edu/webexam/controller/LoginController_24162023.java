package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.service.AuthService_24162023;

@WebServlet("/login")
public class LoginController_24162023 extends BaseController_24162023 {
  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    view(q, s, "login");
  }

  protected void doPost(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    try {
      var user =
          new AuthService_24162023().login(q.getParameter("email"), q.getParameter("password"));
      if (user == null) {
        q.setAttribute("error", "Email hoặc mật khẩu không đúng.");
        view(q, s, "login");
        return;
      }
      q.getSession().invalidate();
      var session = q.getSession(true);
      session.setAttribute("user", user);
      redirect(q, s, user.isAdmin() ? "/admin/books" : "/home");
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }
}
