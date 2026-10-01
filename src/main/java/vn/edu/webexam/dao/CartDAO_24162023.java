package vn.edu.webexam.dao;

import java.sql.*;
import java.util.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

public class CartDAO_24162023 {
  // All cart writes and checkout lock the same existing account row until commit/rollback.
  public void lockUser(Connection c, int userId) throws SQLException {
    if (userId <= 0) throw new IllegalArgumentException("Người dùng không hợp lệ.");
    try (var p = c.prepareStatement("SELECT id FROM users WITH (UPDLOCK,HOLDLOCK,ROWLOCK) WHERE id=?")) {
      p.setQueryTimeout(15);
      p.setInt(1, userId);
      try (var r = p.executeQuery()) {
        if (!r.next()) throw new IllegalArgumentException("Người dùng không còn tồn tại.");
      }
    }
  }

  public Cart_24162023 find(int userId) throws SQLException {
    var items = new ArrayList<CartItem_24162023>();
    try (var c = DBConnection_24162023.getConnection();
         var p = c.prepareStatement("""
           SELECT b.bookid,b.title,b.price,b.quantity,b.cover_image,ci.quantity AS cart_quantity
           FROM cart_items ci JOIN books b ON b.bookid=ci.book_id
           WHERE ci.user_id=? ORDER BY ci.book_id
           """)) {
      p.setInt(1, userId);
      try (var r = p.executeQuery()) {
        while (r.next()) items.add(new CartItem_24162023(mapBook(r), r.getInt("cart_quantity")));
      }
    }
    return new Cart_24162023(items);
  }

  public Map<Integer,Integer> quantities(Connection c, int userId) throws SQLException {
    var result = new TreeMap<Integer,Integer>();
    try (var p = c.prepareStatement("SELECT book_id,quantity FROM cart_items WHERE user_id=? ORDER BY book_id")) {
      p.setInt(1, userId);
      try (var r = p.executeQuery()) {
        while (r.next()) result.put(r.getInt(1), r.getInt(2));
      }
    }
    return result;
  }

  public Book_24162023 lockBook(Connection c, int bookId) throws SQLException {
    try (var p = c.prepareStatement("SELECT bookid,title,price,quantity,cover_image FROM books WITH (UPDLOCK,HOLDLOCK) WHERE bookid=?")) {
      p.setInt(1, bookId);
      try (var r = p.executeQuery()) { return r.next() ? mapBook(r) : null; }
    }
  }

  private Book_24162023 mapBook(ResultSet r) throws SQLException {
    var book = new Book_24162023();
    book.setBookid(r.getInt("bookid")); book.setTitle(r.getString("title"));
    book.setPrice(r.getBigDecimal("price")); book.setQuantity((Integer) r.getObject("quantity"));
    book.setCoverImage(r.getString("cover_image"));
    return book;
  }

  public void set(Connection c, int userId, int bookId, int quantity, boolean exists) throws SQLException {
    String sql = exists ? "UPDATE cart_items SET quantity=?,updated_at=SYSUTCDATETIME() WHERE user_id=? AND book_id=?"
        : "INSERT cart_items(quantity,user_id,book_id) VALUES(?,?,?)";
    try (var p = c.prepareStatement(sql)) {
      p.setInt(1, quantity); p.setInt(2, userId); p.setInt(3, bookId);
      if (p.executeUpdate() != 1) throw new IllegalArgumentException("Giỏ hàng đã thay đổi. Vui lòng tải lại.");
    }
  }

  public void remove(Connection c, int userId, int bookId) throws SQLException {
    try (var p = c.prepareStatement("DELETE FROM cart_items WHERE user_id=? AND book_id=?")) {
      p.setInt(1, userId); p.setInt(2, bookId); p.executeUpdate();
    }
  }

  public void clear(Connection c, int userId) throws SQLException {
    try (var p = c.prepareStatement("DELETE FROM cart_items WHERE user_id=?")) {
      p.setInt(1, userId); p.executeUpdate();
    }
  }
}
