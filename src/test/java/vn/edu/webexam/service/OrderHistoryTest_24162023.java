package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import vn.edu.webexam.model.OrderStatus_24162023;

class OrderHistoryTest_24162023 {
  @Test void omittedStatusMeansAll() { assertNull(OrderService_24162023.parseStatus(null)); }

  @ParameterizedTest @EnumSource(OrderStatus_24162023.class)
  void acceptsEachStatus(OrderStatus_24162023 status) {
    assertSame(status, OrderService_24162023.parseStatus(status.name()));
  }

  @ParameterizedTest @ValueSource(strings={"HACK", "abc", "", "new", " NEW ", "NEW' OR 1=1--", "'; DROP TABLE orders;--"})
  void invalidStatusRejected(String value) {
    assertThrows(IllegalArgumentException.class, () -> OrderService_24162023.parseStatus(value));
  }
}
