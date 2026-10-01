package vn.edu.webexam.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

public class OrderDAO_24162023 {
  public Long findIdByKey(Connection c, int userId, String key) throws SQLException {
    try (var p = c.prepareStatement("SELECT order_id FROM orders WHERE user_id=? AND checkout_key=?")) {
      p.setInt(1, userId); p.setString(2, key);
      try (var r = p.executeQuery()) { return r.next() ? r.getLong(1) : null; }
    }
  }

  public long insert(Connection c, int userId, String key, CheckoutForm_24162023 f, BigDecimal total) throws SQLException {
    try (var p = c.prepareStatement("""
        INSERT orders(user_id,receiver_name,receiver_phone,shipping_address,total_amount,status,payment_method,payment_status,checkout_key)
        VALUES(?,?,?,?,?,'NEW','COD','UNPAID',?)
        """, Statement.RETURN_GENERATED_KEYS)) {
      p.setInt(1, userId); p.setNString(2, f.getReceiverName()); p.setString(3, f.getReceiverPhone());
      p.setNString(4, f.getShippingAddress()); p.setBigDecimal(5, total); p.setString(6, key);
      p.executeUpdate();
      try (var r = p.getGeneratedKeys()) {
        if (!r.next()) throw new SQLException("Không lấy được mã đơn hàng.");
        return r.getLong(1);
      }
    }
  }

  public void insertItem(Connection c, long orderId, CartItem_24162023 item) throws SQLException {
    try (var p = c.prepareStatement("""
        INSERT order_items(order_id,book_id,book_name_snapshot,unit_price_snapshot,quantity,line_total)
        VALUES(?,?,?,?,?,?)
        """)) {
      p.setLong(1, orderId); p.setInt(2, item.getBook().getBookid());
      p.setNString(3, item.getBook().getTitle() == null ? "Sách #" + item.getBook().getBookid() : item.getBook().getTitle());
      p.setBigDecimal(4, item.getBook().getPrice()); p.setInt(5, item.getQuantity());
      p.setBigDecimal(6, item.getLineTotal()); p.executeUpdate();
    }
  }

  public void decrement(Connection c, int bookId, int quantity) throws SQLException {
    try (var p = c.prepareStatement("UPDATE books SET quantity=quantity-? WHERE bookid=? AND quantity>=?")) {
      p.setInt(1, quantity); p.setInt(2, bookId); p.setInt(3, quantity);
      if (p.executeUpdate() != 1) throw new IllegalArgumentException("Tồn kho đã thay đổi. Vui lòng kiểm tra giỏ hàng.");
    }
  }

  public Order_24162023 find(int userId, long orderId) throws SQLException {
    try (var c = DBConnection_24162023.getConnection();
         var p = c.prepareStatement("SELECT * FROM orders WHERE order_id=? AND user_id=?")) {
      p.setLong(1, orderId); p.setInt(2, userId);
      try (var r = p.executeQuery()) {
        if (!r.next()) return null;
        var receiver = new CheckoutForm_24162023(r.getNString("receiver_name"),r.getString("receiver_phone"),r.getNString("shipping_address"));
        var total = r.getBigDecimal("total_amount");
        var status = OrderStatus_24162023.valueOf(r.getString("status"));
        var method = r.getString("payment_method"); var payment = r.getString("payment_status");
        var created = r.getTimestamp("created_at").toLocalDateTime();
        // Close the header result before opening the second query (MARS is not required).
        r.close();
        return new Order_24162023(orderId,userId,receiver,total,status,method,payment,created,items(c,orderId));
      }
    }
  }

  private List<OrderItem_24162023> items(Connection c, long orderId) throws SQLException {
    var items = new ArrayList<OrderItem_24162023>();
    try (var p = c.prepareStatement("SELECT * FROM order_items WHERE order_id=? ORDER BY order_item_id")) {
      p.setLong(1, orderId);
      try (var r = p.executeQuery()) {
        while (r.next()) items.add(new OrderItem_24162023((Integer)r.getObject("book_id"),
            r.getNString("book_name_snapshot"),r.getBigDecimal("unit_price_snapshot"),
            r.getInt("quantity"),r.getBigDecimal("line_total")));
      }
    }
    return items;
  }
}
