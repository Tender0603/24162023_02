package vn.edu.webexam.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthenticationFilter_24162023 implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var q = (HttpServletRequest) request;
    var s = (HttpServletResponse) response;
    if (q.getSession(false) == null || q.getSession(false).getAttribute("user") == null) {
      s.sendRedirect(q.getContextPath() + "/login");
      return;
    }
    chain.doFilter(q, s);
  }
}
