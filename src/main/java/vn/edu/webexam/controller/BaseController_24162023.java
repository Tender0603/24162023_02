package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public abstract class BaseController_24162023 extends HttpServlet {
  protected void view(HttpServletRequest req, HttpServletResponse resp, String name)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
  }

  protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
      throws IOException {
    resp.sendRedirect(req.getContextPath() + path);
  }

  protected int positive(String value, int fallback) {
    if (value == null || value.isBlank()) return fallback;
    try {
      int n = Integer.parseInt(value);
      if (n > 0) return n;
    } catch (NumberFormatException ignored) {
    }
    throw new IllegalArgumentException("Tham số số nguyên dương không hợp lệ.");
  }

  protected void unavailable(HttpServletRequest req, HttpServletResponse resp, Exception e)
      throws ServletException, IOException {
    getServletContext().log("Application request failed", e);
    resp.setStatus(503);
    view(req, resp, "error");
  }
}
