package vn.edu.webexam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order_24162023 {
  private final long orderId;
  private final int userId;
  private final CheckoutForm_24162023 receiver;
  private final BigDecimal totalAmount;
  private final OrderStatus_24162023 status;
  private final String paymentMethod, paymentStatus;
  private final LocalDateTime createdAt;
  private final List<OrderItem_24162023> items;
  public Order_24162023(long id, int userId, CheckoutForm_24162023 receiver, BigDecimal total,
      OrderStatus_24162023 status, String method, String paymentStatus, LocalDateTime createdAt,
      List<OrderItem_24162023> items) {
    this.orderId = id; this.userId = userId; this.receiver = receiver; this.totalAmount = total;
    this.status = status; this.paymentMethod = method; this.paymentStatus = paymentStatus;
    this.createdAt = createdAt; this.items = List.copyOf(items);
  }
  public long getOrderId() { return orderId; }
  public int getUserId() { return userId; }
  public CheckoutForm_24162023 getReceiver() { return receiver; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public OrderStatus_24162023 getStatus() { return status; }
  public String getPaymentMethod() { return paymentMethod; }
  public String getPaymentStatus() { return paymentStatus; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public List<OrderItem_24162023> getItems() { return items; }
}
