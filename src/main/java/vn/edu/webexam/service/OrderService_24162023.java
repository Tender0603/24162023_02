package vn.edu.webexam.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webexam.dao.OrderDAO_24162023;
import vn.edu.webexam.model.*;

/** Read-only history; every call reads the database for the authenticated user. */
public class OrderService_24162023 {
  private final OrderDAO_24162023 orders = new OrderDAO_24162023();

  public static OrderStatus_24162023 parseStatus(String value) {
    if (value == null) return null;
    try { return OrderStatus_24162023.valueOf(value); }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ.");
    }
  }

  public List<Order_24162023> history(int authenticatedUserId, OrderStatus_24162023 status) throws SQLException {
    return orders.findByUserIdAndStatus(authenticatedUserId, status);
  }

  public Order_24162023 detail(int authenticatedUserId, long orderId) throws SQLException {
    return orders.find(authenticatedUserId, orderId);
  }
}
