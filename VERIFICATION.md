# Kiểm chứng bài Web Exam 02

## Cart + COD — 01/10/2026, project D:\24162023_02

### Phạm vi và database
- Giữ nguyên Servlet/JSP/JSTL/SiteMesh/JDBC/SQL Server, target Java 21; các lệnh build lần này đặt JAVA_HOME về Temurin 21.
- Chủ project đã xác nhận quyền dùng `WebExam02_24162023` trên `DESKTOP-L3A5HVK\SQLEXPRESS` cho project này. Không truy cập repository khác.
- Đọc connection/metadata thành công bằng SQLCMD Windows Authentication; JDBC thực tế dùng localhost:1433, integratedSecurity, native DLL 12.8.1.x64.
- `scripts/cart_cod_upgrade.sql` đã thực thi thành công và chạy lại thành công. Đã đọc metadata PK/FK/CHECK/UNIQUE. Ba bảng mới: cart_items, orders, order_items.
- Xóa Book: cart FK CASCADE; order item FK SET NULL, không xóa snapshot/đơn. Năm bảng gốc giữ nguyên cấu trúc.
- Không có UI Order History/filter. Schema/model hỗ trợ đủ 8 status; checkout tạo NEW/COD/UNPAID.

### Kiểm thử đã thực chạy
| Kiểm tra | Kết quả |
|---|---|
| `mvn -B clean test` (13:34:50 UTC+7) | BUILD SUCCESS; 45 tests, 0 failures, 0 errors, 0 skipped |
| `mvn -B package` | BUILD SUCCESS; 45 tests, WAR target/24162023_02.war |
| Profile `cart-cod-it` (13:30:02 UTC+7) | BUILD SUCCESS; 59 tests tổng cộng = 45 + 14 JDBC integration; 0 failures/errors/skipped |
| `python scripts/audit.py` | PASS; 45 Java chính, 4 Java test; 0 vi phạm naming/layer/framework |
| `scripts/cart-cod-smoke.py`, Tomcat riêng 18082 | 54 HTTP/DB checks PASS |
| `scripts/http-smoke.py`, cùng Tomcat, OTP_MODE=demo | 36 HTTP checks hồi quy PASS |

- 6 test cũ giữ nguyên và PASS. 39 test mới chạy mặc định kiểm tra số lượng/overflow, giá/stock null, BigDecimal, người nhận, checkout key và status.
- 14 JDBC test dùng fixture riêng: cộng giỏ/PK, update/+/-/remove, ownership, sách không hợp lệ, giá đọc lại/snapshot, COD/NEW/UNPAID, trừ tồn/xóa giỏ, stock thiếu, rollback sau insert header và sau actual stock write, xóa Book giữ history, add đồng thời, checkout cùng key đồng thời, hai người mua tranh stock, constraints và admin stale stock.
- 54 HTTP/DB checks dùng cookie/session thật và JSP render trên Tomcat: auth/CSRF, đầu vào giả user/price/total/payment, các biên quantity, stock đổi, form người nhận, PRG, idempotency, refresh, ownership success, snapshot và đọc status đã đổi trong DB.
- 36 hồi quy gồm Home/author pagination, Admin CRUD/default cover, review ownership/XSS, login/logout/OTP demo. Không gửi email SMTP thực.
- Các ca kiểm thử lỗi phát hiện trong quá trình làm đã được sửa nguyên nhân: khóa app ban đầu chưa có transaction hoạt động với JDBC; đổi sang khóa dòng user hiện hữu. JSP dùng `cart.empty` xung đột từ khóa EL; đổi sang `empty cart.items`. Các suite liên quan đã chạy lại và PASS.
- Windows Authentication trong sandbox ban đầu không lấy được credentials; chạy lại với quyền thực thi đã được duyệt thì JDBC/HTTP integration hoạt động. Không đổi password hoặc tạo SQL login để vượt lỗi.

### Dữ liệu và giới hạn
- Fixture của JDBC/Cart-COD HTTP đã được dọn theo ID vừa tạo. Tài khoản OTP fixture của hồi quy cũ cũng đã dọn bằng cleanup script riêng của lần chạy.
- Sau cleanup: 5 users, 10 books, 3 author, 11 book_author, 6 rating; cart_items/orders/order_items đều 0. Không coi số lượng trong biên bản 24/09 bên dưới là snapshot hiện tại.
- Unit tests không thay thế JDBC/HTTP evidence; lần này có cả hai mức. Chưa kiểm tra trực quan bằng trình duyệt đồ họa hoặc chụp screenshot; checklist dành cho demo thủ công.
- Test rollback dùng DAO gây SQLException sau khi đã ghi SQL thực; không dùng DB giả. Không kiểm thử sự cố mất điện/mất mạng đúng thời điểm commit.
- Build output/log/runtime không thuộc source nộp Git; target/ và .runtime/ đã bị ignore. Không thêm credentials/OTP thật vào source. Chưa thực hiện Git.

**Các mục 24/09 bên dưới là biên bản lịch sử, không phải kết quả mới của project hiện tại.**

## Cập nhật upload ảnh — 24/09/2026, 14:43 trở đi
- `mvn clean package`: **BUILD SUCCESS**, **6 tests, 0 failures/errors/skipped**; 32 Java class chính, 2 class test.
- Đã deploy WAR mới lên Tomcat demo 18080, giữ nguyên cấu hình SMTP đang có.
- **32 kiểm tra HTTP upload thành công**: multipart Create PNG; Edit JPG/JPEG/WEBP; giữ ảnh khi không chọn file; preview Edit; Home/Detail/Admin View; byte/MIME ảnh; UUID; từ chối giả ảnh/sai đuôi/file rỗng; quá 5 MiB trả 413; cleanup khi validation fail; CSRF/role/path traversal.
- Đã kiểm tra riêng Create multipart không chọn ảnh dùng cover.svg, sau đó Delete bình thường.
- Đã restart/redeploy WAR thật: đường dẫn trong DB và file ảnh vẫn tồn tại, HTTP ảnh trả 200; sau đó dọn fixture.
- **36 kiểm tra HTTP hồi quy thành công**: CRUD, pagination, review, login/logout, phân quyền và OTP demo trên instance kiểm thử 18081. SMTP thật không gửi trong phép thử này.
- Đã dọn tài khoản OTP/sách/ảnh test, dừng instance 18081; instance demo 18080 vẫn chạy.
- Static audit: 0 lỗi suffix, 0 JDBC trong JSP, 0 SQL/JDBC trong controller. Không đổi schema/database.sql/DAO hay business logic đăng nhập/OTP/review.
- File upload lưu mặc định ở .runtime/uploads/books, ngoài .runtime/webapps; UUID path lưu trong books.cover_image varchar(100).
- Hai JUnit upload mới kiểm tra tên an toàn/không trùng, nội dung ảnh, sai định dạng, dung lượng và chống path traversal.

Phần dưới là biên bản kiểm chứng ban đầu trước khi bổ sung upload.

Ngày kiểm chứng: **24/09/2026**. Trạng thái dưới đây dựa trên lệnh và HTTP đã chạy thật, không chỉ đọc source.

## Môi trường và build
- java -version: Temurin **21.0.12.1**.
- mvn -version: **3.9.16**, JVM Oracle **26.0.2.1**.
- Maven compiler release **21**; packaging **war**.
- Tomcat runtime **10.1.57**, chạy Java 21; Servlet **Jakarta 6.0**.
- SiteMesh **3.2.1**: log khởi tạo thành công; response có user-layout/admin-layout tương ứng.
- Build từng phase 1–5 thành công. Build cuối: **mvn -B clean package — BUILD SUCCESS** lúc 13:21 ngày 24/09/2026.
- Compile: **30 Java source chính**, **1 test source**.
- JUnit: **4 tests, 0 failures, 0 errors, 0 skipped**.
- WAR: `target/24162023_02.war`.
- Report test thực: `target/surefire-reports/vn.edu.webexam.service.SecurityTest_24162023.txt`.

## Database runtime
- Microsoft SQL Server **2022 Express 16.0.1000.6**, instance **SQLEXPRESS**, TCP **1433**.
- `database.sql` thực thi thành công qua sqlcmd Windows Authentication.
- JDBC Windows Authentication thực tế qua Microsoft DLL 12.8.1.x64.
- Đã đối chiếu sys.tables/sys.columns: đúng **5 bảng, 26 cột**, kiểu, nullability, identity.
- PK thực tế: books(bookid), users(id), author(author_id), book_author(bookid,author_id), rating(userid,bookid).
- Đúng **4 FK**; không có ID giả trong bảng liên kết.
- Sau khi dọn fixture kiểm thử: **10 books, 3 users, 3 author, 11 book_author, 4 rating**.
- Tài khoản OTP tạm và sách/review/liên kết tạm đã được dọn. Identity có thể nhảy số sau test, đúng hành vi SQL Server.

## Kiểm thử HTTP thực tế — 35 kiểm tra thành công
Script: `scripts/http-smoke.py`; bản kết quả local: `.runtime/http-results.json`.

1. Home HTTP 200, decorator User.
2. Footer đúng Trần Hoàng Tiến Đạt, MSSV 24162023, Mã đề 02.
3. Nhóm tác giả trang đầu có 3/3/2 sách.
4. Khách không thấy menu Admin.
5. Phân trang độc lập: page_1=2, page_2=2 trả 2/1/2 sách.
6. Link phân trang giữ trang tác giả khác.
7. Page sai định dạng trả 400.
8. Khách truy cập Admin được chuyển login.
9. Sách không tồn tại trả 404.
10. Chi tiết sách có tên người review từ JOIN users.
11. Input SQL injection không đăng nhập được.
12. POST thiếu CSRF bị từ chối 403.
13. User đăng nhập được chuyển Home.
14. User vào /admin/books trả 403.
15. User vào /admin trả 403.
16. Admin đăng nhập, đúng decorator Admin, có menu quản trị.
17. Danh sách Admin hiển thị 3 sách/trang.
18. Trang Admin thứ 2 hoạt động.
19. JSP Add Book biên dịch/render.
20. Create thành công, redirect chi tiết.
21. Edit đọc lại đúng hai tác giả đã chọn.
22. Update lưu tiêu đề mới.
23. Review lấy user Session dù browser gửi userid khác; HTML được escape.
24. Review lại cập nhật một dòng, số review vẫn là 1.
25. Điểm review ngoài 1–5 bị từ chối.
26. Xóa sách có review và book_author thành công, chi tiết sau xóa trả 404.
27. Đăng ký chuyển OTP.
28. Không đăng nhập được khi chưa kích hoạt.
29. OTP sai bị từ chối.
30. OTP đúng kích hoạt tài khoản.
31. OTP vừa dùng không dùng lại được trước khi login.
32. Tài khoản kích hoạt đăng nhập thành công.
33. Logout xóa thông tin người dùng khỏi header.
34. Sau logout, trang Admin yêu cầu login.
35. OTP không còn dùng lại được sau logout.

## Audit toàn bộ yêu cầu
| Hạng mục | Trạng thái / bằng chứng |
|---|---|
| Maven Web, WAR, Servlet, JDBC, JSP, SQL Server | Build + runtime đã kiểm chứng |
| 3 tầng MVC → Service → DAO | Đọc source và static audit; SQL nằm tại DAO |
| SiteMesh User/Admin | Response runtime và log SiteMesh |
| Header 4 mục, Admin theo role | JSP + HTTP guest/admin |
| Footer đúng họ tên, MSSV, đề | HTTP assertion tiếng Việt chính xác |
| Register, OTP verification | Runtime demo; pending Session, chỉ tạo user sau OTP |
| Email OTP qua SMTP | Đã triển khai và compile; **chưa verify gửi email thật** |
| OTP expiry/attempt limit | 2 JUnit tests hết hạn / giới hạn 5 lần |
| Login/Session/Logout/Admin authorization | HTTP thực tế |
| Home grouped author, 3 sách/trang | SQL + HTTP thực tế |
| cover_image/title/isbn/authors/publisher/date/quantity/review count | JSP fragment + response chi tiết/home |
| Previous/Next/page numbers | JSP và HTTP chuyển trang |
| Title link detail | JSP href /book-detail?id và HTTP detail |
| Detail/reviews/user names/add review | JOIN + HTTP thực tế |
| userid lấy Session | HTTP giả userid không ảnh hưởng chủ review |
| Composite rating duplicate | Runtime update cùng review, count=1 |
| Admin Create/Read/Update/Delete | HTTP thực tế, DB ghi thật |
| Admin pagination | HTTP trang 1/2 |
| Delete confirm + FK transaction | Form confirm có trong JSP; transaction đã thử sách có review |
| database.sql/seed đúng schema | SQL metadata và count thực tế |
| Mọi Java class/interface có _24162023 | 30 main + 1 test, **0 vi phạm** |
| Không Spring/JPA/Hibernate, không trộn javax.servlet | Static search 0 kết quả |
| Không scriptlet/JDBC trong JSP, không SQL/JDBC trong controller | Static audit 0 vi phạm |
| PreparedStatement, POST writes, redirect sau thành công | Đọc source và HTTP |
| HTML escape, CSRF, validate | HTTP XSS/CSRF/rating/page + service validation |
| README/checklist screenshot | Đã tạo |
| 24162023.docx | Cố ý chưa tạo vì chưa có screenshot thật |

## Giới hạn xác minh
- **Chưa có SMTP account/app password**, chưa xác nhận thư tới mailbox ngoài. OTP demo runtime đã được xác minh; không coi đó là SMTP thật.
- Không có automation trình duyệt đồ họa trong lần kiểm tra này: JSP đã compile/render qua HTTP, chưa chụp screenshot hoặc kiểm tra trực quan hộp confirm.
- Database mặc định trong application.properties dành cho SQL Authentication và để trống user/password; demo dùng Windows Authentication qua script.
- Không tạo repository remote hoặc Word. Người dùng chụp ảnh và tạo Word theo checklist.
- Không phát hiện lỗi compile/runtime trong phạm vi 35 HTTP checks và 4 JUnit tests cuối cùng.
