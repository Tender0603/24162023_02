package vn.edu.webexam.service;

import java.sql.*;
import vn.edu.webexam.dao.CartDAO_24162023;
import vn.edu.webexam.model.Cart_24162023;
import vn.edu.webexam.util.DBConnection_24162023;

public class CartService_24162023 {
  private final CartDAO_24162023 dao = new CartDAO_24162023();
  public Cart_24162023 find(int userId) throws SQLException { return dao.find(userId); }
  public void add(int userId, int bookId, int quantity) throws SQLException { change(userId, bookId, quantity, "add"); }
  public void update(int userId, int bookId, int quantity) throws SQLException { change(userId, bookId, quantity, "update"); }
  public void increment(int userId, int bookId, int delta) throws SQLException {
    if (delta != 1 && delta != -1) throw new IllegalArgumentException("Thao tác không hợp lệ.");
    change(userId, bookId, delta, "step");
  }
  public void remove(int userId, int bookId) throws SQLException { change(userId, bookId, 0, "remove"); }

  private void change(int userId, int bookId, int input, String action) throws SQLException {
    if (bookId <= 0) throw new IllegalArgumentException("Mã sách không hợp lệ.");
    if ((action.equals("add") || action.equals("update")) && input <= 0)
      throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1.");
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        dao.lockUser(c, userId);
        if (action.equals("remove")) dao.remove(c, userId, bookId);
        else {
          var book = dao.lockBook(c, bookId);
          Integer previous = dao.quantities(c, userId).get(bookId);
          if (!action.equals("add") && previous == null)
            throw new IllegalArgumentException("Sách không có trong giỏ hàng của bạn.");
          long quantity = action.equals("update") ? input : (long) (previous == null ? 0 : previous) + input;
          PurchaseValidation_24162023.book(book, quantity);
          dao.set(c, userId, bookId, (int) quantity, previous != null);
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback(); throw e;
      }
    }
  }
}
