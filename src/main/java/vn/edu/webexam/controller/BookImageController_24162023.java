package vn.edu.webexam.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.Files;
import vn.edu.webexam.service.BookImageService_24162023;

/** Public read-only endpoint: uploaded files are never executed as JSP or other code. */
@WebServlet("/assets/uploads/books/*")
public class BookImageController_24162023 extends HttpServlet {
  private final BookImageService_24162023 images = new BookImageService_24162023();

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      String path = request.getPathInfo();
      var file = images.resolve(path == null ? null : path.substring(1));
      if (!Files.isRegularFile(file)) {
        response.sendError(404);
        return;
      }
      String name = file.getFileName().toString();
      response.setContentType(
          name.endsWith(".png")
              ? "image/png"
              : name.endsWith(".webp") ? "image/webp" : "image/jpeg");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setHeader("Cache-Control", "public, max-age=86400");
      response.setContentLengthLong(Files.size(file));
      Files.copy(file, response.getOutputStream());
    } catch (IllegalArgumentException e) {
      response.sendError(404);
    }
  }
}
