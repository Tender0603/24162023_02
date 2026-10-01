package vn.edu.webexam.model;

import java.math.BigDecimal;
import java.util.List;

public class Cart_24162023 {
  private final List<CartItem_24162023> items;
  public Cart_24162023(List<CartItem_24162023> items) { this.items = List.copyOf(items); }
  public List<CartItem_24162023> getItems() { return items; }
  public boolean isEmpty() { return items.isEmpty(); }
  public boolean isAvailable() { return !isEmpty() && items.stream().allMatch(CartItem_24162023::isAvailable); }
  public BigDecimal getTotal() {
    BigDecimal total = new BigDecimal("0.00");
    for (var item : items) {
      if (item.getLineTotal() == null) return null;
      total = total.add(item.getLineTotal());
    }
    return total;
  }
}
