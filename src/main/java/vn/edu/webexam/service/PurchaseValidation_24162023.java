package vn.edu.webexam.service;

import vn.edu.webexam.model.Book_24162023;

public final class PurchaseValidation_24162023 {
  private PurchaseValidation_24162023() {}
  public static int positive(String value) {
    try {
      if (value != null && value.matches("[0-9]+")) {
        int n = Integer.parseInt(value);
        if (n > 0) return n;
      }
    } catch (NumberFormatException ignored) { }
    throw new IllegalArgumentException("Số lượng / mã sách phải là số nguyên dương trong phạm vi hợp lệ.");
  }
  public static void book(Book_24162023 book, long quantity) {
    if (book == null) throw new IllegalArgumentException("Sách không còn tồn tại.");
    if (quantity < 1 || quantity > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Số lượng phải từ 1 đến 2147483647.");
    if (book.getPrice() == null || book.getPrice().signum() < 0)
      throw new IllegalArgumentException("Sách chưa có giá hợp lệ, không thể mua.");
    if (book.getQuantity() == null || book.getQuantity() <= 0)
      throw new IllegalArgumentException("Sách đã hết hàng hoặc chưa có tồn kho hợp lệ.");
    if (quantity > book.getQuantity())
      throw new IllegalArgumentException("Số lượng vượt tồn kho hiện tại (" + book.getQuantity() + ").");
  }
}
