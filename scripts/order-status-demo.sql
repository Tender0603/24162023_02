-- Select WebExam02_24162023 in SSMS. This file only SELECTs when run as a whole.
-- Log in as the order owner, open /orders, then use an ID from the query below.
-- Copy ONE UPDATE, replace <ORDER_ID>, remove its leading -- and execute it.
-- Refresh /orders and /orders/detail?id=... after each UPDATE.
-- Filter the matching status; the order must disappear from its previous filter.
SELECT order_id, user_id, status, created_at
FROM orders
ORDER BY order_id DESC;

-- UPDATE orders SET status = 'NEW' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'CONFIRMED' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'PREPARING' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'SHIPPING' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'DELIVERING' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'DELIVERED' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'CANCELLED' WHERE order_id = <ORDER_ID>;
-- UPDATE orders SET status = 'RETURNED' WHERE order_id = <ORDER_ID>;
