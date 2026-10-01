package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vn.edu.webexam.dao.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

class OrderHistoryDatabaseIT_24162023 {
  private int a, b, book;
  private long first, second, foreign;
  private final OrderService_24162023 service = new OrderService_24162023();
  private long insert(String sql, Object... values) throws SQLException {
    try (var c = DBConnection_24162023.getConnection(); var p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      for (int i=0;i<values.length;i++) p.setObject(i+1,values[i]);
      p.executeUpdate(); try (var r=p.getGeneratedKeys()) { r.next(); return r.getLong(1); }
    }
  }
  private void execute(String sql, Object... values) throws SQLException {
    try (var c=DBConnection_24162023.getConnection(); var p=c.prepareStatement(sql)) {
      for(int i=0;i<values.length;i++) p.setObject(i+1,values[i]);
      p.executeUpdate();
    }
  }
  private long order(int user) throws SQLException {
    try(var c=DBConnection_24162023.getConnection()) {
      return new OrderDAO_24162023().insert(c,user,UUID.randomUUID().toString(),
          new CheckoutForm_24162023("History receiver","0901234567","History address"),new BigDecimal("24.70"));
    }
  }
  @BeforeEach void fixtures() throws Exception {
    assertEquals("YES",System.getenv("CART_COD_DB_CONFIRMED"));
    try(var c=DBConnection_24162023.getConnection()) { assertEquals("WebExam02_24162023",c.getCatalog()); }
    a=(int)insert("INSERT users(email,passwd,is_admin) VALUES(?,REPLICATE('x',32),0)",UUID.randomUUID()+"@t.dev");
    b=(int)insert("INSERT users(email,passwd,is_admin) VALUES(?,REPLICATE('x',32),0)",UUID.randomUUID()+"@t.dev");
    book=(int)insert("INSERT books(title,price,quantity) VALUES('Original snapshot',12.35,10)");
    first=order(a); second=order(a); foreign=order(b);
    execute("UPDATE orders SET created_at='2026-01-01' WHERE user_id IN (?,?)",a,b);
    execute("INSERT order_items(order_id,book_id,book_name_snapshot,unit_price_snapshot,quantity,line_total) VALUES(?,?,'Original snapshot',12.35,2,24.70)",first,book);
  }
  @AfterEach void cleanup() throws Exception {
    if(a==0 && b==0) return;
    execute("DELETE order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id IN (?,?))",a,b);
    execute("DELETE orders WHERE user_id IN (?,?)",a,b);
    execute("DELETE books WHERE bookid=?",book);
    execute("DELETE users WHERE id IN (?,?)",a,b);
  }
  @Test void allOnlyContainsOwnOrdersAndBreaksTimestampTiesById() throws Exception {
    assertEquals(List.of(second,first), service.history(a,null).stream().map(Order_24162023::getOrderId).toList());
  }
  @Test void createdTimeTakesPriorityOverId() throws Exception {
    execute("UPDATE orders SET created_at='2026-02-01' WHERE order_id=?",first);
    assertEquals(first,service.history(a,null).getFirst().getOrderId());
  }
  @ParameterizedTest @EnumSource(OrderStatus_24162023.class)
  void eachFilterUsesCurrentDatabaseStatusAndOwner(OrderStatus_24162023 status) throws Exception {
    execute("UPDATE orders SET status=? WHERE order_id IN (?,?)",status.name(),first,foreign);
    var found=service.history(a,status);
    assertEquals(status==OrderStatus_24162023.NEW ? 2 : 1,found.size());
    assertTrue(found.stream().allMatch(o->o.getUserId()==a && o.getStatus()==status));
    assertTrue(found.stream().anyMatch(o->o.getOrderId()==first));
  }
  @Test void directSqlChangesAreVisibleWithoutNewServiceOrSession() throws Exception {
    for(var status:OrderStatus_24162023.values()) {
      execute("UPDATE orders SET status=? WHERE order_id=?",status.name(),first);
      assertEquals(status,service.detail(a,first).getStatus());
      assertTrue(service.history(a,status).stream().anyMatch(o->o.getOrderId()==first));
      if(status!=OrderStatus_24162023.NEW)
        assertFalse(service.history(a,OrderStatus_24162023.NEW).stream().anyMatch(o->o.getOrderId()==first));
    }
  }
  @Test void allIncludesDifferentStatuses() throws Exception {
    execute("UPDATE orders SET status='DELIVERED' WHERE order_id=?",second);
    assertEquals(Set.of(OrderStatus_24162023.NEW,OrderStatus_24162023.DELIVERED),
        new HashSet<>(service.history(a,null).stream().map(Order_24162023::getStatus).toList()));
  }
  @Test void foreignAndMissingDetailsReturnNull() throws Exception {
    assertNull(service.detail(a,foreign)); assertNull(service.detail(a,Long.MAX_VALUE));
  }
  @Test void detailRetainsItemsNamesPricesAndTotalsAfterBookChangesAndDeletion() throws Exception {
    execute("UPDATE books SET title='Changed',price=999 WHERE bookid=?",book);
    assertSnapshot();
    new BookDAO_24162023().delete(book);
    assertSnapshot();
    assertNull(service.detail(a,first).getItems().getFirst().getBookId());
    assertEquals(2,service.history(a,null).size());
  }
  private void assertSnapshot() throws Exception {
    var order=service.detail(a,first); assertEquals(new BigDecimal("24.70"),order.getTotalAmount());
    assertEquals(1,order.getItems().size()); var item=order.getItems().getFirst();
    assertEquals("Original snapshot",item.getBookName()); assertEquals(new BigDecimal("12.35"),item.getUnitPrice());
    assertEquals(2,item.getQuantity()); assertEquals(new BigDecimal("24.70"),item.getLineTotal());
  }
}
