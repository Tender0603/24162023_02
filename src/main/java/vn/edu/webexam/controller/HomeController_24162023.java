package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import vn.edu.webexam.service.BookService_24162023;

@WebServlet(urlPatterns = {"/home", "/products", ""})
public class HomeController_24162023 extends BaseController_24162023 {
  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    try {
      Map<Integer, Integer> pages = new HashMap<>();
      for (String name : q.getParameterMap().keySet())
        if (name.startsWith("page_"))
          pages.put(positive(name.substring(5), 1), positive(q.getParameter(name), 1));
      q.setAttribute("groups", new BookService_24162023().grouped(pages).values());
      view(q, s, "home");
    } catch (IllegalArgumentException e) {
      s.setStatus(400);
      q.setAttribute("error", e.getMessage());
      view(q, s, "error");
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }
}
