package vn.edu.webexam.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

public class RequestFilter_24162023 implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var q = (HttpServletRequest) request;
    var s = (HttpServletResponse) response;
    q.setCharacterEncoding("UTF-8");
    s.setCharacterEncoding("UTF-8");
    s.setHeader("X-Content-Type-Options", "nosniff");
    s.setHeader("X-Frame-Options", "DENY");
    if (q.getRequestURI().startsWith(q.getContextPath() + "/assets/")) {
      chain.doFilter(q, s);
      return;
    }
    s.setHeader("Cache-Control", "no-store");
    var session = q.getSession();
    if (session.getAttribute("csrf") == null)
      session.setAttribute("csrf", UUID.randomUUID().toString());
    if ("POST".equals(q.getMethod())) {
      try {
        if (q.getContentType() != null
            && q.getContentType()
                .toLowerCase(java.util.Locale.ROOT)
                .startsWith("multipart/form-data")
            && ("/admin/books".equals(q.getServletPath()) || "/admin".equals(q.getServletPath()))) {
          // Parse explicitly: Tomcat may otherwise hide the size error and return null parameters.
          q.getParts();
        }
        // getParameter also parses multipart fields using the target servlet's limits.
        if (!session.getAttribute("csrf").equals(q.getParameter("csrf"))) {
          s.sendError(403);
          return;
        }
      } catch (IllegalStateException e) {
        if (q.getContentType() == null
            || !q.getContentType()
                .toLowerCase(java.util.Locale.ROOT)
                .startsWith("multipart/form-data")) throw e;
        s.setStatus(413);
        s.setContentType("text/html; charset=UTF-8");
        s.getWriter()
            .write(
                "<!DOCTYPE html><html lang=\"vi\"><meta charset=\"UTF-8\">"
                    + "<title>Ảnh quá lớn</title><h1>Ảnh vượt giới hạn upload</h1>"
                    + "<p>Mỗi ảnh tối đa 5 MB; toàn bộ biểu mẫu tối đa 6 MB. "
                    + "Hãy quay lại và chọn ảnh nhỏ hơn.</p></html>");
        return;
      }
    }
    chain.doFilter(q, s);
  }
}
