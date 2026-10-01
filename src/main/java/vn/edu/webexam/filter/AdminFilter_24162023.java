package vn.edu.webexam.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.edu.webexam.model.User_24162023;

public class AdminFilter_24162023 implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var q = (HttpServletRequest) request;
    var s = (HttpServletResponse) response;
    var session = q.getSession(false);
    var user = session == null ? null : (User_24162023) session.getAttribute("user");
    if (user == null) {
      s.sendRedirect(q.getContextPath() + "/login");
      return;
    }
    if (!user.isAdmin()) {
      s.sendError(403);
      return;
    }
    chain.doFilter(q, s);
  }
}
