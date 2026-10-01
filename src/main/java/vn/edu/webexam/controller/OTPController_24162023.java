package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.PendingRegistration_24162023;
import vn.edu.webexam.service.AuthService_24162023;

@WebServlet("/verify-otp")
public class OTPController_24162023 extends BaseController_24162023 {
  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    view(q, s, "verify-otp");
  }

  protected void doPost(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    var session = q.getSession();
    synchronized (session) {
      try {
        var pending = (PendingRegistration_24162023) session.getAttribute("pending");
        if (pending == null)
          throw new IllegalArgumentException("Không có đăng ký chờ kích hoạt. Hãy đăng ký trước.");
        if (!pending.verify(q.getParameter("otp")))
          throw new IllegalArgumentException("OTP không đúng.");
        new AuthService_24162023().activate(pending.getUser());
        session.removeAttribute("pending");
        redirect(q, s, "/login?activated=1");
      } catch (IllegalArgumentException e) {
        q.setAttribute("error", e.getMessage());
        view(q, s, "verify-otp");
      } catch (Exception e) {
        unavailable(q, s, e);
      }
    }
  }
}
