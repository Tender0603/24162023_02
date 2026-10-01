package vn.edu.webexam.service;

import java.sql.SQLException;
import java.util.*;
import vn.edu.webexam.dao.*;
import vn.edu.webexam.model.*;

public class BookService_24162023 {
  public static final int PAGE_SIZE = 3;
  private final BookDAO_24162023 dao = new BookDAO_24162023();

  public Map<Integer, AuthorPage_24162023> grouped(Map<Integer, Integer> pages)
      throws SQLException {
    return dao.authorPages(pages);
  }

  public Book_24162023 find(int id) throws SQLException {
    return dao.find(id);
  }

  public int pages() throws SQLException {
    return Math.max(1, (dao.count() + PAGE_SIZE - 1) / PAGE_SIZE);
  }

  public List<Book_24162023> page(int page) throws SQLException {
    return dao.page(page, PAGE_SIZE);
  }

  public List<Author_24162023> authors() throws SQLException {
    return new AuthorDAO_24162023().findAll();
  }

  public List<Integer> authorIds(int id) throws SQLException {
    return dao.authorIds(id);
  }

  public void save(Book_24162023 b, List<Integer> ids) throws SQLException {
    if (b.getTitle() == null || b.getTitle().isBlank() || b.getTitle().length() > 200)
      throw new IllegalArgumentException("Tiêu đề cần 1–200 ký tự.");
    if (b.getPublisher() != null && b.getPublisher().length() > 100)
      throw new IllegalArgumentException("Publisher tối đa 100 ký tự.");
    if (b.getIsbn() != null && b.getIsbn() < 0 || b.getQuantity() != null && b.getQuantity() < 0)
      throw new IllegalArgumentException("ISBN và quantity không được âm.");
    if (b.getPrice() != null
        && (b.getPrice().signum() < 0
            || b.getPrice().compareTo(new java.math.BigDecimal("9999.99")) > 0
            || b.getPrice().scale() > 2))
      throw new IllegalArgumentException("Giá từ 0 đến 9999.99, tối đa 2 số thập phân.");
    String cover = b.getCoverImage();
    if (cover == null || cover.isBlank()) b.setCoverImage("assets/cover.svg");
    else if (cover.length() > 100
        || !(cover.matches("assets/[A-Za-z0-9_./-]+") && !cover.contains("..")
            || cover.matches("https?://[^\\s\\\"<>]+")))
      throw new IllegalArgumentException(
          "Ảnh: đường dẫn assets/... hoặc URL http/https, tối đa 100 ký tự.");
    Set<Integer> valid = new HashSet<>();
    for (var a : authors()) valid.add(a.getAuthorId());
    if (ids.isEmpty() || !valid.containsAll(ids))
      throw new IllegalArgumentException("Chọn ít nhất một tác giả hợp lệ.");
    dao.save(b, new ArrayList<>(new LinkedHashSet<>(ids)));
  }

  public void delete(int id) throws SQLException {
    dao.delete(id);
  }
}
