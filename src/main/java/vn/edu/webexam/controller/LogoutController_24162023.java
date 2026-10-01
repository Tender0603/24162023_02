package vn.edu.webexam.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutController_24162023 extends BaseController_24162023 {
  protected void doPost(HttpServletRequest q, HttpServletResponse s) throws IOException {
    if (q.getSession(false) != null) q.getSession(false).invalidate();
    redirect(q, s, "/login");
  }
}
