package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import vn.edu.webexam.dao.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

/** Explicit opt-in only: mvn -Pcart-cod-it test. Uses and cleans only uniquely created fixtures. */
class CartCodDatabaseIT_24162023 {
  private int a,b,book,second;
  private final CartService_24162023 carts = new CartService_24162023();
  private final CheckoutService_24162023 checkout = new CheckoutService_24162023();
  private final CheckoutForm_24162023 form = new CheckoutForm_24162023("Nguyễn An","0901234567","Số 1, TP HCM");
  private String key() { return UUID.randomUUID().toString(); }
  @BeforeEach void fixtures() throws Exception {
    assertEquals("YES",System.getenv("CART_COD_DB_CONFIRMED"),"Explicitly confirm the project DB before integration tests");
    try (var c = DBConnection_24162023.getConnection()) { assertEquals("WebExam02_24162023",c.getCatalog()); }
    a = createUser(); b = createUser(); book = createBook(); second = createBook();
  }
  private int createUser() throws SQLException {
    return insert("INSERT users(email,passwd,is_admin) VALUES(?,REPLICATE('x',32),0)","it-"+key()+"@t.dev");
  }
  private int createBook() throws SQLException {
    return insert("INSERT books(title,price,quantity) VALUES(?,12.35,5)","Cart COD fixture " + key());
  }
  private int insert(String sql, String value) throws SQLException {
    try (var c=DBConnection_24162023.getConnection(); var p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1,value); p.executeUpdate(); try(var r=p.getGeneratedKeys()) { r.next(); return r.getInt(1); }
    }
  }
  private void execute(String sql, int... values) throws SQLException {
    try(var c=DBConnection_24162023.getConnection(); var p=c.prepareStatement(sql)) {
      for(int i=0;i<values.length;i++) p.setInt(i+1,values[i]); p.executeUpdate();
    }
  }
  private int number(String sql, int id) throws SQLException {
    try(var c=DBConnection_24162023.getConnection(); var p=c.prepareStatement(sql)) {
      p.setInt(1,id); try(var r=p.executeQuery()) { r.next(); return r.getInt(1); }
    }
  }
  private int stock() throws SQLException { return number("SELECT quantity FROM books WHERE bookid=?",book); }
  private int countOrders(int user) throws SQLException { return number("SELECT COUNT(*) FROM orders WHERE user_id=?",user); }
  @AfterEach void cleanup() throws Exception {
    if(a==0 && b==0) return;
    execute("DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id IN (?,?))",a,b);
    execute("DELETE FROM orders WHERE user_id IN (?,?)",a,b);
    execute("DELETE FROM cart_items WHERE user_id IN (?,?)",a,b);
    execute("DELETE FROM books WHERE bookid IN (?,?)",book,second);
    execute("DELETE FROM users WHERE id IN (?,?)",a,b);
  }
  @Test void addSameBookUpdatesOneLineAndServerTotals() throws Exception {
    carts.add(a,book,2); carts.add(a,book,3);
    var cart=carts.find(a); assertEquals(1,cart.getItems().size()); assertEquals(5,cart.getItems().getFirst().getQuantity());
    assertEquals(new BigDecimal("61.75"),cart.getTotal());
    assertThrows(IllegalArgumentException.class,()->carts.add(a,book,1));
    assertEquals(5,carts.find(a).getItems().getFirst().getQuantity());
  }
  @Test void updateStepBoundariesAndRemove() throws Exception {
    carts.add(a,book,1); assertThrows(IllegalArgumentException.class,()->carts.increment(a,book,-1));
    carts.increment(a,book,1); assertEquals(2,carts.find(a).getItems().getFirst().getQuantity());
    carts.increment(a,book,-1); carts.update(a,book,5);
    assertThrows(IllegalArgumentException.class,()->carts.increment(a,book,1));
    for(int n:new int[]{0,-1,6}) assertThrows(IllegalArgumentException.class,()->carts.update(a,book,n));
    carts.remove(a,book); assertTrue(carts.find(a).isEmpty());
  }
  @Test void ownershipForUpdateRemoveAndCheckout() throws Exception {
    carts.add(b,book,2);
    assertThrows(IllegalArgumentException.class,()->carts.update(a,book,1)); carts.remove(a,book);
    assertThrows(IllegalArgumentException.class,()->checkout.place(a,key(),form));
    assertEquals(2,carts.find(b).getItems().getFirst().getQuantity()); assertEquals(0,countOrders(a));
  }
  @Test void rejectsUnavailableProducts() throws Exception {
    assertThrows(IllegalArgumentException.class,()->carts.add(a,Integer.MAX_VALUE,1));
    execute("UPDATE books SET price=NULL WHERE bookid=?",book);
    assertThrows(IllegalArgumentException.class,()->carts.add(a,book,1));
    execute("UPDATE books SET price=12.35,quantity=NULL WHERE bookid=?",book);
    assertThrows(IllegalArgumentException.class,()->carts.add(a,book,1));
    execute("UPDATE books SET quantity=0 WHERE bookid=?",book);
    assertThrows(IllegalArgumentException.class,()->carts.add(a,book,1));
    assertTrue(carts.find(a).isEmpty());
  }
  @Test void validCodSnapshotsStockCartAndDuplicateReplay() throws Exception {
    carts.add(a,book,2); execute("UPDATE books SET price=15.25 WHERE bookid=?",book);
    String key=key(); long id=checkout.place(a,key,form); var order=checkout.find(a,id);
    assertEquals(OrderStatus_24162023.NEW,order.getStatus()); assertEquals("COD",order.getPaymentMethod());
    assertEquals("UNPAID",order.getPaymentStatus()); assertEquals(new BigDecimal("30.50"),order.getTotalAmount());
    assertEquals(new BigDecimal("15.25"),order.getItems().getFirst().getUnitPrice());
    assertEquals("0901234567",order.getReceiver().getReceiverPhone());
    assertEquals(3,stock()); assertTrue(carts.find(a).isEmpty()); assertNull(checkout.find(b,id));
    carts.add(a,second,1);
    assertEquals(id,checkout.place(a,key,form)); assertEquals(1,countOrders(a));
    assertFalse(carts.find(a).isEmpty()); assertEquals(3,stock());
  }
  @Test void insufficientStockLeavesEverythingUntouched() throws Exception {
    carts.add(a,book,2); carts.add(a,second,2); execute("UPDATE books SET quantity=1 WHERE bookid=?",second);
    assertThrows(IllegalArgumentException.class,()->checkout.place(a,key(),form));
    assertEquals(0,countOrders(a)); assertEquals(5,stock()); assertEquals(2,carts.find(a).getItems().size());
  }
  @Test void failureAfterOrderInsertRollsBack() throws Exception {
    carts.add(a,book,2);
    var broken=new CheckoutService_24162023(new OrderDAO_24162023() {
      @Override public long insert(Connection c,int u,String k,CheckoutForm_24162023 f,BigDecimal t) throws SQLException {
        super.insert(c,u,k,f,t); throw new SQLException("Injected after header insert");
      }
    });
    assertThrows(SQLException.class,()->broken.place(a,key(),form));
    assertEquals(0,countOrders(a)); assertEquals(5,stock()); assertFalse(carts.find(a).isEmpty());
  }
  @Test void failureAfterStockWriteRollsBackHeaderItemsStockAndKeepsCart() throws Exception {
    carts.add(a,book,2);
    var broken=new CheckoutService_24162023(new OrderDAO_24162023() {
      @Override public void decrement(Connection c,int id,int q) throws SQLException {
        super.decrement(c,id,q); throw new SQLException("Injected after actual stock write");
      }
    });
    assertThrows(SQLException.class,()->broken.place(a,key(),form));
    assertEquals(0,countOrders(a)); assertEquals(5,stock()); assertEquals(2,carts.find(a).getItems().getFirst().getQuantity());
  }
  @Test void physicalDeleteKeepsOrderSnapshotAndRemovesCartReference() throws Exception {
    carts.add(a,book,1); long id=checkout.place(a,key(),form); carts.add(b,book,1);
    new BookDAO_24162023().delete(book);
    var order=checkout.find(a,id); assertNull(order.getItems().getFirst().getBookId());
    assertTrue(order.getItems().getFirst().getBookName().startsWith("Cart COD fixture"));
    assertEquals(new BigDecimal("12.35"),order.getTotalAmount()); assertTrue(carts.find(b).isEmpty());
  }
  @Test void concurrentAddsCannotLoseQuantityOrDuplicateLine() throws Exception {
    try(var executor=Executors.newFixedThreadPool(2)) {
      var start=new CountDownLatch(1);
      Callable<Void> add=()->{start.await();carts.add(a,book,2);return null;};
      var one=executor.submit(add); var two=executor.submit(add); start.countDown();
      one.get(20,TimeUnit.SECONDS); two.get(20,TimeUnit.SECONDS);
    }
    assertEquals(1,carts.find(a).getItems().size()); assertEquals(4,carts.find(a).getItems().getFirst().getQuantity());
  }
  @Test void concurrentDuplicateCheckoutCreatesOnlyOneOrder() throws Exception {
    carts.add(a,book,2); String key=key();
    try(var executor=Executors.newFixedThreadPool(2)) {
      var start=new CountDownLatch(1);
      Callable<Long> place=()->{start.await();return checkout.place(a,key,form);};
      var one=executor.submit(place); var two=executor.submit(place); start.countDown();
      assertEquals(one.get(20,TimeUnit.SECONDS),two.get(20,TimeUnit.SECONDS));
    }
    assertEquals(1,countOrders(a)); assertEquals(3,stock());
  }
  @Test void competingBuyersCannotOversell() throws Exception {
    carts.add(a,book,4); carts.add(b,book,4);
    try(var executor=Executors.newFixedThreadPool(2)) {
      var start=new CountDownLatch(1);
      var results=new ArrayList<Future<Boolean>>();
      for(int user:new int[]{a,b}) results.add(executor.submit(()->{
        start.await(); try { checkout.place(user,key(),form); return true; }
        catch(IllegalArgumentException expected) { return false; }
      }));
      start.countDown(); int successes=0;
      for(var result:results) if(result.get(20,TimeUnit.SECONDS)) successes++;
      assertEquals(1,successes);
    }
    assertEquals(1,stock()); assertEquals(1,countOrders(a)+countOrders(b));
  }
  @Test void schemaConstraintsRejectInvalidWrites() throws Exception {
    assertThrows(SQLException.class,()->execute("INSERT cart_items(user_id,book_id,quantity) VALUES(?,?,0)",a,book));
    carts.add(a,book,1); long id=checkout.place(a,key(),form);
    assertThrows(SQLException.class,()->execute("UPDATE orders SET status='INVALID' WHERE user_id=?",a));
    assertThrows(SQLException.class,()->execute("UPDATE orders SET payment_method='CARD' WHERE user_id=?",a));
    execute("UPDATE orders SET status='CONFIRMED' WHERE user_id=?",a);
    assertEquals(OrderStatus_24162023.CONFIRMED,checkout.find(a,id).getStatus());
  }
  @Test void adminStaleStockCannotUndoCheckout() throws Exception {
    var dao=new BookDAO_24162023(); var stale=dao.find(book);
    carts.add(a,book,2); checkout.place(a,key(),form);
    assertThrows(IllegalArgumentException.class,()->dao.save(stale,List.of()));
    assertEquals(3,stock());
  }
}
