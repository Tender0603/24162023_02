-- Run only against the verified database for D:\24162023_02.
-- sqlcmd -S .\SQLEXPRESS -E -C -b -f 65001 -d WebExam02_24162023 -i scripts/cart_cod_upgrade.sql
SET NOCOUNT ON;
SET XACT_ABORT ON;
IF DB_NAME() <> 'WebExam02_24162023'
 THROW 50001, 'Wrong database. Select the verified project database explicitly.', 1;
IF OBJECT_ID('dbo.users','U') IS NULL OR OBJECT_ID('dbo.books','U') IS NULL
 THROW 50002, 'Base users/books schema is missing.', 1;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @lock int;
 EXEC @lock = sys.sp_getapplock @Resource='cart-cod-schema-24162023', @LockMode='Exclusive', @LockOwner='Transaction', @LockTimeout=10000;
 IF @lock < 0 THROW 50003, 'Cannot lock schema upgrade.', 1;
 IF OBJECT_ID('dbo.cart_items','U') IS NULL
 CREATE TABLE dbo.cart_items (
   user_id int NOT NULL REFERENCES dbo.users(id),
   book_id int NOT NULL REFERENCES dbo.books(bookid) ON DELETE CASCADE,
   quantity int NOT NULL CHECK (quantity > 0),
   created_at datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
   updated_at datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
   CONSTRAINT PK_cart_items PRIMARY KEY (user_id,book_id)
 );
 IF OBJECT_ID('dbo.orders','U') IS NULL
 CREATE TABLE dbo.orders (
   order_id bigint IDENTITY PRIMARY KEY,
   user_id int NOT NULL REFERENCES dbo.users(id),
   receiver_name nvarchar(100) NOT NULL,
   receiver_phone varchar(20) NOT NULL,
   shipping_address nvarchar(500) NOT NULL,
   total_amount decimal(19,2) NOT NULL CHECK (total_amount >= 0),
   status varchar(20) NOT NULL DEFAULT 'NEW'
     CHECK (status IN ('NEW','CONFIRMED','PREPARING','SHIPPING','DELIVERING','DELIVERED','CANCELLED','RETURNED')),
   payment_method varchar(10) NOT NULL DEFAULT 'COD' CHECK (payment_method='COD'),
   payment_status varchar(10) NOT NULL DEFAULT 'UNPAID' CHECK (payment_status IN ('UNPAID','PAID','REFUNDED')),
   checkout_key uniqueidentifier NOT NULL,
   created_at datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
   CONSTRAINT UQ_orders_user_checkout UNIQUE (user_id,checkout_key)
 );
 IF OBJECT_ID('dbo.order_items','U') IS NULL
 CREATE TABLE dbo.order_items (
   order_item_id bigint IDENTITY PRIMARY KEY,
   order_id bigint NOT NULL REFERENCES dbo.orders(order_id),
   book_id int NULL REFERENCES dbo.books(bookid) ON DELETE SET NULL,
   book_name_snapshot nvarchar(200) NOT NULL,
   unit_price_snapshot decimal(19,2) NOT NULL CHECK (unit_price_snapshot >= 0),
   quantity int NOT NULL CHECK (quantity > 0),
   line_total decimal(19,2) NOT NULL CHECK (line_total >= 0),
   CONSTRAINT CK_order_items_total CHECK (line_total = unit_price_snapshot * quantity)
 );
 IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.cart_items') AND name='IX_cart_items_book')
 CREATE INDEX IX_cart_items_book ON dbo.cart_items(book_id);
 IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.orders') AND name='IX_orders_user_status')
 CREATE INDEX IX_orders_user_status ON dbo.orders(user_id,status,created_at DESC);
 IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.order_items') AND name='IX_order_items_order')
 CREATE INDEX IX_order_items_order ON dbo.order_items(order_id);
 IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.order_items') AND name='IX_order_items_book')
 CREATE INDEX IX_order_items_book ON dbo.order_items(book_id);
 COMMIT;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT > 0 ROLLBACK;
 THROW;
END CATCH;
