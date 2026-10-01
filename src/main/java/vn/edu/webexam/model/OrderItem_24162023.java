package vn.edu.webexam.model;

import java.math.BigDecimal;

public class OrderItem_24162023 {
  private final Integer bookId;
  private final String bookName;
  private final BigDecimal unitPrice, lineTotal;
  private final int quantity;
  public OrderItem_24162023(Integer bookId, String bookName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    this.bookId = bookId; this.bookName = bookName; this.unitPrice = unitPrice;
    this.quantity = quantity; this.lineTotal = lineTotal;
  }
  public Integer getBookId() { return bookId; }
  public String getBookName() { return bookName; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public int getQuantity() { return quantity; }
  public BigDecimal getLineTotal() { return lineTotal; }
}
