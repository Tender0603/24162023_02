package vn.edu.webexam.dao;

import java.sql.*;
import java.util.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

public class BookDAO_24162023 {
  // Aggregate each one-to-many relation before joining so review counts are not multiplied by
  // authors.
  private static final String JOINS =
      """
LEFT JOIN (SELECT ba.bookid, STRING_AGG(CAST(a.author_name AS varchar(max)), ', ') AS authors
  FROM book_author ba JOIN author a ON a.author_id=ba.author_id GROUP BY ba.bookid) au ON au.bookid=b.bookid
LEFT JOIN (SELECT bookid,COUNT(*) AS review_count FROM rating GROUP BY bookid) rc ON rc.bookid=b.bookid
""";
  private static final String SELECT =
      "SELECT b.*,au.authors,COALESCE(rc.review_count,0) AS review_count FROM books b ";

  public Book_24162023 map(ResultSet r) throws SQLException {
    var b = new Book_24162023();
    b.setBookid(r.getInt("bookid"));
    b.setIsbn((Integer) r.getObject("isbn"));
    b.setTitle(r.getString("title"));
    b.setPublisher(r.getString("publisher"));
    b.setPrice(r.getBigDecimal("price"));
    b.setDescription(r.getString("description"));
    var d = r.getDate("publish_date");
    b.setPublishDate(d == null ? null : d.toLocalDate());
    b.setCoverImage(r.getString("cover_image"));
    b.setQuantity((Integer) r.getObject("quantity"));
    b.setOriginalQuantity(b.getQuantity());
    b.setAuthors(r.getString("authors"));
    b.setReviewCount(r.getInt("review_count"));
    return b;
  }

  public Book_24162023 find(int id) throws SQLException {
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement(SELECT + JOINS + " WHERE b.bookid=?")) {
      p.setInt(1, id);
      try (var r = p.executeQuery()) {
        return r.next() ? map(r) : null;
      }
    }
  }

  public Map<Integer, AuthorPage_24162023> authorPages(Map<Integer, Integer> requested)
      throws SQLException {
    var result = new LinkedHashMap<Integer, AuthorPage_24162023>();
    try (var c = DBConnection_24162023.getConnection()) {
      try (var p =
              c.prepareStatement(
                  "SELECT a.author_id,a.author_name,COUNT(ba.bookid) total FROM author a LEFT JOIN"
                      + " book_author ba ON ba.author_id=a.author_id GROUP BY"
                      + " a.author_id,a.author_name ORDER BY a.author_id");
          var r = p.executeQuery()) {
        while (r.next()) {
          int id = r.getInt("author_id"),
              total = r.getInt("total"),
              page = Math.min(requested.getOrDefault(id, 1), Math.max(1, (total + 2) / 3));
          result.put(id, new AuthorPage_24162023(id, r.getString("author_name"), total, page));
        }
      }
      if (result.isEmpty()) return result;
      // Exactly two queries for all authors. Only placeholder count is dynamic; values are always
      // bound.
      String values = String.join(",", Collections.nCopies(result.size(), "(?,?)"));
      String sql =
          """
WITH requested(author_id,page_no) AS (SELECT * FROM (VALUES %s) v(author_id,page_no)),
ranked AS (SELECT bookid,author_id,ROW_NUMBER() OVER(PARTITION BY author_id ORDER BY bookid) rn FROM book_author)
SELECT b.*,au.authors,COALESCE(rc.review_count,0) review_count,x.author_id AS group_author
FROM ranked x JOIN requested q ON q.author_id=x.author_id JOIN books b ON b.bookid=x.bookid
"""
                  .formatted(values)
              + JOINS
              + " WHERE x.rn BETWEEN (q.page_no-1)*3+1 AND q.page_no*3 ORDER BY x.author_id,x.rn";
      try (var p = c.prepareStatement(sql)) {
        int i = 1;
        for (var a : result.values()) {
          p.setInt(i++, a.getAuthorId());
          p.setInt(i++, a.getPage());
        }
        try (var r = p.executeQuery()) {
          while (r.next()) result.get(r.getInt("group_author")).getBooks().add(map(r));
        }
      }
    }
    return result;
  }

  public int count() throws SQLException {
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement("SELECT COUNT(*) FROM books");
        var r = p.executeQuery()) {
      r.next();
      return r.getInt(1);
    }
  }

  public List<Book_24162023> page(int page, int size) throws SQLException {
    var list = new ArrayList<Book_24162023>();
    try (var c = DBConnection_24162023.getConnection();
        var p =
            c.prepareStatement(
                SELECT + JOINS + " ORDER BY b.bookid OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")) {
      p.setInt(1, (page - 1) * size);
      p.setInt(2, size);
      try (var r = p.executeQuery()) {
        while (r.next()) list.add(map(r));
      }
    }
    return list;
  }

  public List<Integer> authorIds(int bookid) throws SQLException {
    var ids = new ArrayList<Integer>();
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement("SELECT author_id FROM book_author WHERE bookid=?")) {
      p.setInt(1, bookid);
      try (var r = p.executeQuery()) {
        while (r.next()) ids.add(r.getInt(1));
      }
    }
    return ids;
  }

  public void save(Book_24162023 b, List<Integer> authorIds) throws SQLException {
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        boolean create = b.getBookid() == 0;
        String sql =
            create
                ? "INSERT"
                      + " books(isbn,title,publisher,price,description,publish_date,cover_image,quantity)"
                      + " VALUES(?,?,?,?,?,?,?,?)"
                : "UPDATE books SET"
                      + " isbn=?,title=?,publisher=?,price=?,description=?,publish_date=?,cover_image=?,quantity=?"
                      + " WHERE bookid=? AND (quantity=? OR (quantity IS NULL AND ? IS NULL))";
        try (var p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
          p.setObject(1, b.getIsbn(), Types.INTEGER);
          p.setString(2, b.getTitle());
          p.setString(3, b.getPublisher());
          p.setBigDecimal(4, b.getPrice());
          p.setString(5, b.getDescription());
          p.setObject(6, b.getPublishDate());
          p.setString(7, b.getCoverImage());
          p.setObject(8, b.getQuantity(), Types.INTEGER);
          if (!create) {
            p.setInt(9, b.getBookid());
            p.setObject(10, b.getOriginalQuantity(), Types.INTEGER);
            p.setObject(11, b.getOriginalQuantity(), Types.INTEGER);
          }
          if (p.executeUpdate() == 0)
            throw new IllegalArgumentException("Sách không còn tồn tại hoặc tồn kho đã thay đổi. Hãy mở lại trang sửa sách.");
          if (create)
            try (var r = p.getGeneratedKeys()) {
              r.next();
              b.setBookid(r.getInt(1));
            }
        }
        try (var p = c.prepareStatement("DELETE FROM book_author WHERE bookid=?")) {
          p.setInt(1, b.getBookid());
          p.executeUpdate();
        }
        try (var p = c.prepareStatement("INSERT book_author(bookid,author_id) VALUES(?,?)")) {
          for (int id : authorIds) {
            p.setInt(1, b.getBookid());
            p.setInt(2, id);
            p.addBatch();
          }
          p.executeBatch();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public void delete(int id) throws SQLException {
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        // Lock parent first: serialize against concurrent review writes.
        try (var p =
            c.prepareStatement("SELECT bookid FROM books WITH (XLOCK,HOLDLOCK) WHERE bookid=?")) {
          p.setInt(1, id);
          try (var r = p.executeQuery()) {
            if (!r.next()) throw new IllegalArgumentException("Sách không tồn tại.");
          }
        }
        for (String sql :
            List.of(
                "DELETE FROM rating WHERE bookid=?",
                "DELETE FROM book_author WHERE bookid=?",
                "DELETE FROM books WHERE bookid=?")) {
          try (var p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
          }
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }
}
