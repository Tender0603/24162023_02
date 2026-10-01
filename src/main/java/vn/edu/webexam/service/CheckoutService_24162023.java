package vn.edu.webexam.service;

import java.sql.*;
import java.util.*;
import vn.edu.webexam.dao.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

public class CheckoutService_24162023 {
  private final CartDAO_24162023 carts = new CartDAO_24162023();
  private final OrderDAO_24162023 orders;
  public CheckoutService_24162023() { this(new OrderDAO_24162023()); }
  public CheckoutService_24162023(OrderDAO_24162023 orders) { this.orders = orders; }

  public static String validKey(String key) {
    if (key == null || !key.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}"))
      throw new IllegalArgumentException("Phiên thanh toán không hợp lệ. Hãy tải lại trang thanh toán.");
    return UUID.fromString(key).toString();
  }

  public Long existing(int userId, String key) throws SQLException {
    key = validKey(key);
    try (var c = DBConnection_24162023.getConnection()) { return orders.findIdByKey(c,userId,key); }
  }
  public Order_24162023 find(int userId, long id) throws SQLException { return orders.find(userId,id); }

  public long place(int userId, String key, CheckoutForm_24162023 form) throws SQLException {
    key = validKey(key);
    form.validate();
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        carts.lockUser(c,userId);
        Long existing = orders.findIdByKey(c,userId,key);
        if (existing != null) { c.commit(); return existing; }
        var quantities = carts.quantities(c,userId);
        if (quantities.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống, không thể đặt hàng.");
        var items = new ArrayList<CartItem_24162023>();
        // Sorted IDs ensure consistent book lock ordering between different buyers.
        for (var entry : quantities.entrySet()) {
          var book = carts.lockBook(c,entry.getKey());
          PurchaseValidation_24162023.book(book,entry.getValue());
          items.add(new CartItem_24162023(book,entry.getValue()));
        }
        var cart = new Cart_24162023(items);
        long id = orders.insert(c,userId,key,form,cart.getTotal());
        for (var item : items) {
          orders.insertItem(c,id,item);
          orders.decrement(c,item.getBook().getBookid(),item.getQuantity());
        }
        carts.clear(c,userId);
        c.commit();
        return id;
      } catch (SQLException | RuntimeException e) {
        c.rollback(); throw e;
      }
    }
  }
}
