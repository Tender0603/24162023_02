package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.service.*;

@WebServlet("/register")
public class RegisterController_24162023 extends BaseController_24162023 {
  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    view(q, s, "register");
  }

  protected void doPost(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    try {
      Long sent = (Long) q.getSession().getAttribute("otpSentAt");
      if (sent != null && System.currentTimeMillis() - sent < 60000)
        throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi gửi OTP mới.");
      var user =
          new AuthService_24162023()
              .prepareRegistration(
                  q.getParameter("email"),
                  q.getParameter("fullname"),
                  q.getParameter("phone"),
                  q.getParameter("password"));
      var pending = new OTPService_24162023().send(user);
      q.getSession().setAttribute("pending", pending);
      q.getSession().setAttribute("otpSentAt", System.currentTimeMillis());
      redirect(q, s, "/verify-otp");
    } catch (IllegalArgumentException e) {
      q.setAttribute("error", e.getMessage());
      view(q, s, "register");
    } catch (jakarta.mail.MessagingException e) {
      getServletContext().log("OTP delivery failed", e);
      q.setAttribute(
          "error", "Chưa gửi được OTP. Kiểm tra cấu hình SMTP hoặc bật chế độ demo theo README.");
      view(q, s, "register");
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }
}
