package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import vn.edu.webexam.model.*;

class PurchaseTest_24162023 {
  private Book_24162023 book() {
    var b = new Book_24162023(); b.setPrice(new BigDecimal("12.35")); b.setQuantity(5); return b;
  }
  @ParameterizedTest @ValueSource(ints={1,5})
  void quantityBoundariesAreValid(int n) { assertDoesNotThrow(() -> PurchaseValidation_24162023.book(book(),n)); }
  @ParameterizedTest @ValueSource(longs={0,-1,6,2147483648L})
  void invalidQuantityRejected(long n) { assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.book(book(),n)); }
  @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"0","-1","abc","1.5","2147483648"," 1","+1"})
  void malformedQuantityRejected(String n) { assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.positive(n)); }
  @Test void missingBookRejected() { assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.book(null,1)); }
  @Test void nullPriceRejected() {
    var b = book(); b.setPrice(null); assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.book(b,1));
    assertNull(new Cart_24162023(List.of(new CartItem_24162023(b,1))).getTotal());
  }
  @Test void negativePriceRejected() {
    var b = book(); b.setPrice(new BigDecimal("-1")); assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.book(b,1));
  }
  @Test void zeroPriceAllowed() {
    var b = book(); b.setPrice(BigDecimal.ZERO); assertDoesNotThrow(() -> PurchaseValidation_24162023.book(b,1));
  }
  @ParameterizedTest @NullSource @ValueSource(ints={0,-1})
  void unavailableStockRejected(Integer stock) {
    var b = book(); b.setQuantity(stock); assertThrows(IllegalArgumentException.class,() -> PurchaseValidation_24162023.book(b,1));
    assertFalse(new CartItem_24162023(b,1).isAvailable());
  }
  @Test void moneyCalculatedExactlyFromBooks() {
    var cart = new Cart_24162023(List.of(new CartItem_24162023(book(),3),new CartItem_24162023(book(),2)));
    assertEquals(new BigDecimal("61.75"),cart.getTotal()); assertTrue(cart.isAvailable());
  }
  @Test void emptyCartCannotCheckout() { assertFalse(new Cart_24162023(List.of()).isAvailable()); }
  @Test void receiverTrimAndLeadingZeroPreserved() {
    var f = new CheckoutForm_24162023("  Nguyễn An  "," 0901234567 ","  Số 1, TP HCM  ");
    f.validate(); assertEquals("Nguyễn An",f.getReceiverName()); assertEquals("0901234567",f.getReceiverPhone());
  }
  @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"   "})
  void receiverNameRequired(String name) { assertThrows(IllegalArgumentException.class,() -> new CheckoutForm_24162023(name,"0901234567","Address").validate()); }
  @ParameterizedTest @ValueSource(strings={"0123","phone","090 123 4567","1234567890123456"})
  void invalidPhoneRejected(String phone) { assertThrows(IllegalArgumentException.class,() -> new CheckoutForm_24162023("An",phone,"Address").validate()); }
  @Test void receiverLengthAndAddressValidated() {
    assertThrows(IllegalArgumentException.class,() -> new CheckoutForm_24162023("x".repeat(101),"0901234567","Address").validate());
    assertThrows(IllegalArgumentException.class,() -> new CheckoutForm_24162023("An","0901234567"," ").validate());
    assertThrows(IllegalArgumentException.class,() -> new CheckoutForm_24162023("An","0901234567","x".repeat(501)).validate());
  }
  @Test void exactlyEightOrderStatuses() {
    assertEquals(Set.of("NEW","CONFIRMED","PREPARING","SHIPPING","DELIVERING","DELIVERED","CANCELLED","RETURNED"),
        new HashSet<>(Arrays.stream(OrderStatus_24162023.values()).map(Enum::name).toList()));
    assertEquals("Đơn hàng mới",OrderStatus_24162023.NEW.getLabel());
  }
  @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"bad","1-1-1-1-1","00000000-0000-0000-0000-000000000000extra"})
  void malformedCheckoutKeyRejected(String key) { assertThrows(IllegalArgumentException.class,() -> CheckoutService_24162023.validKey(key)); }
}
