package vn.edu.webexam.model;

import java.math.BigDecimal;

public class CartItem_24162023 {
  private final Book_24162023 book;
  private final int quantity;
  public CartItem_24162023(Book_24162023 book, int quantity) {
    this.book = book;
    this.quantity = quantity;
  }
  public Book_24162023 getBook() { return book; }
  public int getQuantity() { return quantity; }
  public BigDecimal getLineTotal() {
    return book.getPrice() == null || book.getPrice().signum() < 0 ? null
        : book.getPrice().multiply(BigDecimal.valueOf(quantity));
  }
  public boolean isAvailable() {
    return quantity > 0 && book.getPrice() != null && book.getPrice().signum() >= 0
        && book.getQuantity() != null && book.getQuantity() >= quantity;
  }
}
