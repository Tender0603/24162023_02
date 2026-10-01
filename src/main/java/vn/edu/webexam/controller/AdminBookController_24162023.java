package vn.edu.webexam.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import vn.edu.webexam.model.Book_24162023;
import vn.edu.webexam.service.BookImageService_24162023;
import vn.edu.webexam.service.BookService_24162023;

@WebServlet(urlPatterns = {"/admin", "/admin/books"})
@MultipartConfig(
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 6 * 1024 * 1024,
    fileSizeThreshold = 1024 * 1024)
public class AdminBookController_24162023 extends BaseController_24162023 {
  private final BookService_24162023 service = new BookService_24162023();
  private final BookImageService_24162023 images = new BookImageService_24162023();

  protected void doGet(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    try {
      String action = q.getParameter("action");
      if ("new".equals(action)) {
        q.setAttribute("book", new Book_24162023());
        form(q, s);
        return;
      }
      if ("edit".equals(action) || "view".equals(action)) {
        int id = positive(q.getParameter("id"), 0);
        var b = service.find(id);
        if (b == null) {
          s.sendError(404);
          return;
        }
        q.setAttribute("book", b);
        if ("edit".equals(action)) {
          q.setAttribute("selectedAuthors", service.authorIds(id));
          form(q, s);
        } else view(q, s, "admin/books/detail");
        return;
      }
      int pages = service.pages(), page = Math.min(positive(q.getParameter("page"), 1), pages);
      q.setAttribute("books", service.page(page));
      q.setAttribute("page", page);
      q.setAttribute("pages", pages);
      q.setAttribute("message", q.getSession().getAttribute("adminMessage"));
      q.getSession().removeAttribute("adminMessage");
      view(q, s, "admin/books/list");
    } catch (IllegalArgumentException e) {
      s.sendError(400);
    } catch (Exception e) {
      unavailable(q, s, e);
    }
  }

  protected void doPost(HttpServletRequest q, HttpServletResponse s)
      throws ServletException, IOException {
    var b = new Book_24162023();
    var ids = new ArrayList<Integer>();
    String uploaded = null;
    String previousCover = null;
    boolean saved = false;
    try {
      String action = q.getParameter("action");
      if ("delete".equals(action)) {
        service.delete(positive(q.getParameter("id"), 0));
        q.getSession()
            .setAttribute("adminMessage", "Đã xóa sách và các liên kết/review liên quan.");
        redirect(q, s, "/admin/books");
        return;
      }
      if (!"save".equals(action)) {
        s.sendError(400);
        return;
      }
      b.setTitle(q.getParameter("title"));
      b.setPublisher(q.getParameter("publisher"));
      b.setDescription(q.getParameter("description"));
      b.setBookid(positive(q.getParameter("id"), 0));
      if (b.getBookid() > 0) {
        var existing = service.find(b.getBookid());
        if (existing == null) {
          s.sendError(404);
          return;
        }
        previousCover = existing.getCoverImage();
        b.setOriginalQuantity(q.getParameter("originalQuantity") == null
            ? existing.getQuantity() : optionalInt(q.getParameter("originalQuantity")));
      }
      // Existing image comes from the database, never a hidden browser field.
      b.setCoverImage(previousCover);
      b.setIsbn(optionalInt(q.getParameter("isbn")));
      b.setQuantity(optionalInt(q.getParameter("quantity")));
      String price = q.getParameter("price"), date = q.getParameter("publish_date");
      b.setPrice(price == null || price.isBlank() ? null : new BigDecimal(price));
      b.setPublishDate(date == null || date.isBlank() ? null : LocalDate.parse(date));
      String[] authors = q.getParameterValues("authorIds");
      if (authors != null) for (String id : authors) ids.add(positive(id, 0));
      if (q.getContentType() != null
          && q.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
        Part cover = q.getPart("cover_file");
        if (cover != null
            && cover.getSubmittedFileName() != null
            && !cover.getSubmittedFileName().isBlank()) {
          try (var input = cover.getInputStream()) {
            uploaded = images.store(input, cover.getSubmittedFileName(), cover.getSize());
          }
          b.setCoverImage(uploaded);
        }
      }
      service.save(b, ids);
      saved = true;
      q.getSession().setAttribute("adminMessage", "Đã lưu sách.");
      redirect(q, s, "/admin/books?action=view&id=" + b.getBookid());
    } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
      b.setCoverImage(previousCover);
      q.setAttribute(
          "error",
          e instanceof NumberFormatException || e instanceof java.time.format.DateTimeParseException
              ? "Số hoặc ngày không hợp lệ."
              : e.getMessage());
      q.setAttribute("book", b);
      q.setAttribute("selectedAuthors", ids);
      if ("delete".equals(q.getParameter("action"))) {
        q.getSession().setAttribute("adminMessage", e.getMessage());
        redirect(q, s, "/admin/books");
      } else
        try {
          form(q, s);
        } catch (java.sql.SQLException ex) {
          unavailable(q, s, ex);
        }
    } catch (Exception e) {
      unavailable(q, s, e);
    } finally {
      // Do not leave a new file behind when validation or the DB transaction fails.
      if (!saved && uploaded != null) {
        try {
          images.discard(uploaded);
        } catch (IOException e) {
          getServletContext().log("Could not clean up unsuccessful book upload", e);
        }
      }
    }
  }

  private Integer optionalInt(String value) {
    return value == null || value.isBlank() ? null : Integer.valueOf(value);
  }

  private void form(HttpServletRequest q, HttpServletResponse s)
      throws java.sql.SQLException, ServletException, IOException {
    q.setAttribute("authors", service.authors());
    view(q, s, "admin/books/form");
  }
}
