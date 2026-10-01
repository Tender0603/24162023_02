# Web Exam 02 — 24162023

**Họ tên:** Trần Hoàng Tiến Đạt<br>
**MSSV:** 24162023 — **Mã đề:** 02

## Yêu cầu môi trường
- JDK 21+, Maven 3.9+, Tomcat **10.1.x** (Jakarta Servlet 6.0).
- SQL Server 2017+ (dùng STRING_AGG), khuyến nghị SQL Server 2022.
- Servlet + JDBC + JSP/JSTL + SiteMesh 3.2.1, đóng gói WAR. Không Spring/JPA/Hibernate.
- Python 3 chỉ cần cho script demo và HTTP smoke test, không cần để chạy WAR.
- Đã phát hiện trên máy: Java CLI 21.0.12.1; Maven 3.9.16 dùng JDK 26.0.2.1; pom đặt release=21; Tomcat 10.1.57; SQL Server Express 2022.

## Database
Chạy bằng SSMS hoặc PowerShell:
```powershell
sqlcmd -S '.\SQLEXPRESS' -E -C -b -f 65001 -i database.sql
```
Database: **WebExam02_24162023**. Script tạo 5 bảng đúng đề, 3 tác giả, 10 sách, 11 liên kết, 3 user, 4 review.
Script không xóa database cũ. Chỉ chạy để khởi tạo lần đầu; database trên máy hiện tại đã được khởi tạo.

Chỉ books.bookid, users.id, author.author_id là IDENTITY. book_author và rating giữ composite PK theo sơ đồ.
Năm bảng gốc giữ nguyên cấu trúc. Yêu cầu bổ sung ngày 01/10/2026 dùng script upgrade riêng để thêm Cart/COD; xem phần bên dưới. Không chạy lại database.sql trên database đã có dữ liệu.
Các cột varchar/text giữ nguyên nên seed sách/tác giả/review dùng chữ không dấu để độc lập code page SQL Server; fullname là nvarchar nên có tiếng Việt.
phone là int: mất số 0 đầu, tối đa 2147483647. price decimal(6,2): tối đa 9999.99.

## JDBC
Cấu hình tập trung: `src/main/resources/application.properties`; biến môi trường cùng tên được ưu tiên.
SQL Authentication:
```powershell
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=WebExam02_24162023;encrypt=true;trustServerCertificate=true;loginTimeout=5'
$env:DB_USER='<SQL login của bạn>'
$env:DB_PASSWORD='<mật khẩu của bạn>'
```
Bật TCP/IP, kiểm tra port bằng SQL Server Configuration Manager. Không commit mật khẩu.
trustServerCertificate=true chỉ tiện cho SQL Server local dùng chứng chỉ tự ký.

Windows Authentication (đã dùng kiểm thử trên máy này):
```powershell
mvn dependency:get '-Dartifact=com.microsoft.sqlserver:mssql-jdbc_auth:12.8.1.x64:dll'
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=WebExam02_24162023;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=5'
```
Thêm thư mục chứa DLL vào `-Djava.library.path` khi khởi động Tomcat (script demo đã làm).
Tài khoản Windows chạy Tomcat phải được phép vào database. Không cần SQL password cho chế độ này.

## SMTP và OTP
Mặc định OTP_MODE=smtp. Cấu hình trong môi trường của tiến trình Tomcat:
```powershell
$env:OTP_MODE='smtp'
$env:SMTP_HOST='<máy chủ SMTP>'
$env:SMTP_PORT='587'
$env:SMTP_USER='<tài khoản SMTP>'
$env:SMTP_PASSWORD='<app password hoặc SMTP password>'
$env:SMTP_FROM='<email gửi>'
$env:SMTP_STARTTLS='true'
```
Không có credentials thật trong source. Chưa cấu hình SMTP sẽ báo chưa gửi được OTP và không tạo tài khoản.
Để demo local: truyền `-OtpMode demo` cho scripts/start-demo.ps1; đọc mã ở log Tomcat. Mặc định script dùng SMTP; không bật demo trên hệ thống thực.
OTP ngẫu nhiên 6 số, hết hạn sau 5 phút, tối đa 5 lần thử; chờ 60 giây trước khi gửi lại.
Thông tin đăng ký chờ nằm trong Session; tài khoản chỉ INSERT sau OTP đúng. Mất Session hoặc restart Tomcat thì đăng ký lại.
Demo không bỏ qua OTP và không hiển thị OTP trên trang web. Gửi email thật vẫn cần kiểm thử với SMTP của bạn.

## Build và deploy
```powershell
mvn clean package
```
WAR: `target/24162023_02.war`. Copy vào `%CATALINA_BASE%/webapps`, chạy Tomcat 10.1.
Cấu hình env phải được đặt trước khi Tomcat chạy. Với Tomcat service, cấu hình môi trường/service tương ứng.

Demo trên máy hiện tại (Tomcat riêng trong .runtime, cổng 18080, Windows Authentication):
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-demo.ps1
```
Có thể truyền `-TomcatHome`, `-JavaHome`, `-Port`. Script chỉ phục vụ demo local, bind 127.0.0.1.
Script restart đúng tiến trình của project và tạo lại bản WAR giải nén trong .runtime; không sửa cài đặt Tomcat gốc.
Dừng demo: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop-demo.ps1`.

## URL
Prefix demo: **http://localhost:18080/24162023_02**. Deploy thường: **http://localhost:8080/24162023_02**.
- `/home`, `/products`: nhóm tác giả, mỗi nhóm 3 sách/trang.
- `/home?page_1=2&page_2=1`: trang độc lập theo author_id.
- `/book-detail?id=1`: chi tiết và review.
- `/register`, `/verify-otp`, `/login`; logout qua nút POST.
- `/admin/books`, `/admin/books?page=2`.
- `/admin/books?action=new`, `?action=edit&id=1`, `?action=view&id=1`.

## Tài khoản demo
| Vai trò | Email | Password |
|---|---|---|
| Admin | admin@example.com | Admin@123 |
| User | user1@example.com | User@123 |
| User | user2@example.com | User@123 |

Hash PBKDF2-HMAC-SHA256, 210000 vòng, output 192 bit Base64 đúng 32 ký tự của passwd.
Vì schema không có cột salt, dùng email chuẩn hóa với tiền tố ứng dụng làm salt theo tài khoản.
Đây là giải pháp trong ràng buộc đề; không đổi email sau khi tạo. Không lưu plaintext.

## Kiến trúc và mapping đề
Java nằm tại `src/main/java/vn/edu/webexam`.
- Presentation: controller + filter + JSP trong `src/main/webapp/WEB-INF/views`.
- Business: service.
- Data Access: dao; JDBC connection tại util.
- Controller → Service → DAO → SQL Server; JSP chỉ EL/JSTL.
- Tất cả class tự viết, kể cả test, có hậu tố _24162023.

| Câu | File chính | Chức năng |
|---|---|---|
| 1 | pom.xml, database.sql, WEB-INF/web.xml, sitemesh3.xml, decorators/ | 3 tầng, 5 bảng, User/Admin SiteMesh, menu/footer |
| 2 | UserDAO, AuthService, OTPService, Login/Register/OTP/LogoutController, filter/ | Đăng ký OTP, session, phân quyền |
| 3 | BookDAO.authorPages, BookService, HomeController, home.jsp | 2 query cho tất cả tác giả, 3 sách/nhóm/trang |
| 4 | BookDAO.find, RatingDAO, RatingService, BookDetail/ReviewController, book-detail.jsp | JOIN user/review, cập nhật khi đã review |
| 6 | BookDAO, BookService, AdminBookController, admin/books/ | CRUD, chọn nhiều tác giả, phân trang, xóa transaction |

Trong bảng trên tên Java rút gọn; tên file thực có hậu tố `_24162023.java`.
Admin được bảo vệ bằng Filter ở server. POST có CSRF token, dữ liệu hiển thị escape bằng c:out.
Xóa book xóa rating và book_author trong cùng transaction; không xóa tác giả hay user.
SiteMesh mapping dùng tên `user.jsp`/`admin.jsp` do phiên bản 3.2.1 tự thêm prefix decorators.

## Kiểm thử và nộp bài
- `mvn clean test` / `mvn package`: 45 JUnit tests (6 regression + 39 kiểm tra Cart/COD). Profile `cart-cod-it` chạy thêm 14 test SQL Server, cần xác nhận database và cấu hình JDBC thực.
- Với demo đang chạy: `python -u scripts/http-smoke.py` kiểm thử HTTP thật; tạo/xóa sách tạm, tạo một tài khoản OTP tạm.
- Sau khi chạy lại smoke test, dọn đúng tài khoản tạm bằng `sqlcmd -S '.\SQLEXPRESS' -E -C -b -i .runtime/cleanup-smoke.sql`. Lần kiểm thử bàn giao đã dọn xong.
- Static audit: `python scripts/audit.py`. Tạo lại ZIP sau khi sửa source: `python scripts/package-submission.py`.
- Kết quả kiểm chứng chi tiết: `VERIFICATION.md`.
- `SCREENSHOT-CHECKLIST.md`: file và màn hình cần chụp toàn màn hình.
- ZIP: `24162023_02.zip` (source/config/docs/scripts, không chứa logs/credentials/runtime).
- Chưa tạo `24162023.docx`; chỉ tạo sau khi bạn chụp screenshot thật.
- Tài liệu tương thích: [SiteMesh chính thức](https://github.com/sitemesh/sitemesh3), [Microsoft JDBC properties](https://learn.microsoft.com/sql/connect/jdbc/setting-the-connection-properties), [Maven compiler release](https://maven.apache.org/plugins/maven-compiler-plugin/).

## Upload ảnh bìa sách
- Admin Add/Edit dùng multipart/form-data và trường chọn file. Hỗ trợ JPG/JPEG/PNG/WEBP, tối đa **5 MiB** (5 × 1024 × 1024 byte). Toàn bộ request tối đa 6 MiB.
- Kiểm tra đuôi và giải mã nội dung ảnh thật; không tin MIME do browser gửi. Giới hạn ảnh 8000 px mỗi chiều và tổng 16 triệu điểm ảnh.
- Tên file UUID; database chỉ lưu `assets/uploads/books/<UUID>.<extension>` (dưới 100 ký tự), giữ nguyên schema.
- Create không chọn ảnh: `assets/cover.svg`. Edit không chọn ảnh: giữ đường dẫn đang lưu trong database, không tin đường dẫn gửi từ browser.
- Mặc định lưu file vật lý tại **${catalina.base}/uploads/books**, ngoài thư mục webapps/WAR. Trên project này: **D:\24162023_02\.runtime\uploads\books**. Restart/redeploy bằng start-demo.ps1 không xóa thư mục này.
- Có thể đặt biến môi trường `BOOK_UPLOAD_DIR=C:/path/to/book-covers` trước khi chạy Tomcat để chọn thư mục bền vững khác. Tài khoản chạy Tomcat cần quyền đọc/ghi. Khi chuyển máy, sao lưu cả database và thư mục ảnh; không xóa .runtime nếu còn dùng vị trí mặc định của demo.
- Servlet `BookImageController_24162023` phục vụ URL `/assets/uploads/books/*` với MIME ảnh và nosniff; không thực thi file upload.
- File mới được xóa nếu lưu sách thất bại. Ảnh cũ sau khi thay ảnh/xóa sách được giữ lại để tránh xóa nhầm file có thể được tham chiếu; có thể dọn thủ công các file không còn được dùng khi cần.
- Bộ đọc WEBP: [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys), chỉ là thư viện ảnh, không thay đổi stack web.

Test thủ công:
1. Login Admin → Add Book → nhập thông tin, chọn tác giả và PNG/JPG/WEBP → Lưu.
2. Kiểm tra ảnh ở Admin View, Book Detail và Home (sách mới nằm cuối nhóm tác giả).
3. Edit → không chọn file → Lưu: ảnh giữ nguyên. Edit lần nữa → chọn ảnh khác → Lưu: ảnh mới xuất hiện.
4. Thử file văn bản đổi tên .png hoặc ảnh >5 MiB: bị từ chối, sách cũ không bị đổi ảnh.
5. Restart demo và mở lại sách: ảnh còn nguyên.

Kiểm thử tự động upload (Python cần Pillow để tạo ảnh mẫu):
```powershell
python -u scripts/upload-smoke.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-demo.ps1
python -u scripts/upload-smoke.py --verify-redeploy
```
Bước cuối xác nhận ảnh tồn tại sau redeploy rồi dọn sách/ảnh fixture. Chỉ dùng script này trên demo local với thư mục ảnh mặc định.
Regression script nhận `EXAM_BASE_URL` và `EXAM_OTP_LOG` nếu cần kiểm thử trên Tomcat riêng có OTP_MODE=demo, không thay đổi SMTP của instance chính.

## Cart + COD — 01/10/2026

Project root: **D:\24162023_02**. Giữ Java 21, Servlet 6, JSP/JSTL, SiteMesh, JDBC và SQL Server. Chưa triển khai Order History/filter UI.

### Database upgrade

Database đã được chủ project xác nhận: `WebExam02_24162023` trên `.\SQLEXPRESS` (TCP localhost:1433). JDBC đọc `application.properties`, ưu tiên biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`. Không ghi mật khẩu vào source.

Với database đã có 5 bảng gốc, **chỉ chạy upgrade**, không chạy lại database.sql:

```powershell
Set-Location D:\24162023_02
sqlcmd -S '.\SQLEXPRESS' -E -C -b -f 65001 -d WebExam02_24162023 -i scripts/cart_cod_upgrade.sql
```

Với database mới hoàn toàn: khởi tạo bằng database.sql trước, sau đó chạy upgrade. Upgrade kiểm tra đúng database, chạy trong transaction, tạo bảng/index còn thiếu và không xóa dữ liệu. Không dùng script này để tự sửa một schema khác cấu trúc đã mô tả.

- `cart_items`: PK `(user_id,book_id)`, quantity > 0, FK user/book. Xóa Book sẽ bỏ dòng cart của sách đó.
- `orders`: FK user; tổng decimal(19,2); receiver phone dạng chuỗi; unique `(user_id,checkout_key)`; COD/UNPAID/NEW lúc tạo.
- `order_items`: snapshot tên, giá, số lượng, thành tiền; xóa Book đặt `book_id=NULL`, giữ nguyên chi tiết và tổng đơn. Không cascade xóa lịch sử đơn.
- Status hỗ trợ `NEW`, `CONFIRMED`, `PREPARING`, `SHIPPING`, `DELIVERING`, `DELIVERED`, `CANCELLED`, `RETURNED`. Model/trang kết quả đọc status từ DB; giao diện lịch sử và filter làm ở task sau.

### Chạy và demo

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
mvn -B clean test
mvn -B package
python scripts/audit.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-demo.ps1 -Port 18082
```

Mở `http://localhost:18082/24162023_02/home`. Launcher tạo Tomcat riêng dưới `.runtime` trong project này và mặc định dùng Windows Authentication nếu chưa đặt DB_URL. DLL native JDBC cần có như hướng dẫn JDBC ở trên. Muốn kiểm thử đăng ký OTP local, dùng thêm `-OtpMode demo`; mặc định là SMTP.

1. Đăng nhập User → mở chi tiết sách → chọn số lượng → **Thêm vào giỏ**.
2. `/cart`: thêm cùng sách cộng dồn; cập nhật, +/− hoặc xóa bằng POST. Số lượng 0/âm/sai định dạng/overflow/vượt stock bị từ chối, không tự clamp. Giỏ gắn user trong DB và tồn tại sau logout/login.
3. `/checkout`: nhập người nhận (1–100 ký tự), điện thoại (9–15 chữ số, tùy chọn dấu +, giữ số 0 đầu), địa chỉ (1–500 ký tự). Chỉ có COD.
4. **Đặt hàng COD** → redirect `/checkout/success?id=...`: mã đơn, người nhận, snapshot sách, tổng tiền, NEW và chưa thanh toán. Refresh không tạo thêm đơn; user khác không đọc được đơn này.
5. Đối chiếu `orders`, `order_items`, `books.quantity`, `cart_items` trong SQL Server. Test tự động dọn fixture nên không để sẵn đơn demo; thao tác demo thủ công tạo đơn thật trong database này.

Giá và tồn kho luôn được đọc từ DB khi thêm/sửa/checkout. Giá null/âm và stock null/không dương không mua được. Giá 0 hợp lệ. Tổng bằng tổng giá × số lượng, dùng BigDecimal; không có phí vận chuyển bổ sung. Giá hiển thị theo đơn vị dữ liệu `books.price` hiện tại.

Checkout dùng **một connection/transaction**: khóa dòng user bằng UPDLOCK/HOLDLOCK, đọc giỏ, khóa sách theo thứ tự ID, kiểm tra lại, insert order/items, trừ tồn có điều kiện, xóa đúng giỏ, commit. Lỗi rollback toàn bộ. Cart write cũng khóa cùng dòng user; PK chống duplicate line. Không tự retry transaction có kết quả chưa rõ. Checkout key do server cấp và unique trong DB cho phép gửi lại cùng key để lấy lại đơn đã tạo, kể cả khi giỏ mới đã có thêm hàng.

Admin edit có `originalQuantity`: nếu tồn kho thay đổi từ lúc mở form, lưu bị từ chối và yêu cầu mở lại form. Admin vẫn có thể chủ động điều chỉnh tồn kho bằng form mới. Xóa sách vẫn xóa review/liên kết tác giả như cũ; snapshot đơn được giữ.

### Kiểm thử SQL Server và HTTP

Các lệnh sau **ghi fixture tạm vào database đã xác nhận**, rồi dọn đúng fixture. Không dùng trên database chưa xác minh. Profile integration không chạy mặc định và không dùng skipped tests để giả PASS.

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=WebExam02_24162023;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=5'
$env:CART_COD_DB_CONFIRMED='YES'
$authPath=Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc_auth\12.8.1.x64'
mvn -B -Pcart-cod-it "-DargLine=-Djava.library.path=$authPath" test
$env:EXAM_BASE_URL='http://127.0.0.1:18082/24162023_02'
python -u scripts/cart-cod-smoke.py
```

Kết quả đã thực chạy: **45 unit/regression + 14 JDBC integration = 59 JUnit tests**; **54 HTTP/DB checks Cart/COD**; **36 HTTP checks hồi quy**. Xem `VERIFICATION.md` để phân biệt kết quả hiện tại và biên bản cũ. Các file build/runtime nằm trong `target/`, `.runtime/` và đã được `.gitignore` loại trừ. Chưa init/commit/push Git.
