# Checklist chụp màn hình — Trần Hoàng Tiến Đạt / 24162023 / Đề 02

## Order History + Filter — bổ sung sau baseline 8e75364

Demo tại `http://localhost:18082/24162023_02`. Đăng nhập user có đơn COD; dùng SSMS chọn đúng `WebExam02_24162023`. Test tự dọn fixture nên tạo đơn demo bằng Cart/COD trước khi chụp.

- [ ] Header **Lịch sử đơn hàng**, `/orders`: ALL có mã/ngày/người nhận/tổng/COD/payment/status và nút chi tiết.
- [ ] Filter **Đơn hàng mới** active và đơn NEW.
- [ ] SSMS SELECT order_id/user_id/status và SQL UPDATE đúng mã đơn sang CONFIRMED. Dùng template trong `scripts/order-status-demo.sql`.
- [ ] Refresh: nhãn **Đã xác nhận**; filter NEW không còn đơn, filter CONFIRMED có đơn.
- [ ] PREPARING / SHIPPING / DELIVERING: UPDATE → refresh → filter tương ứng.
- [ ] DELIVERED: UI **Đã giao**, filter active.
- [ ] CANCELLED: UI **Đơn hàng hủy**; RETURNED: UI **Đơn hàng hoàn**.
- [ ] Detail: thời điểm, status hiện tại, COD/payment, receiver/phone/address, tên/giá snapshot, quantity/line total/tổng.
- [ ] User khác truy cập URL detail trả 404; logout rồi mở history yêu cầu login.
- [ ] Terminal: 89 JUnit PASS, 77 Order History HTTP/DB checks PASS; Cart/COD 54 và regression HTTP 36 PASS.

Đã có ảnh automation local trong `.runtime/order-history-*.png` và `.runtime/order-detail.png`; đây là ảnh fixture, không chứa thanh địa chỉ trình duyệt. Khi nộp bài, chụp toàn màn hình trình duyệt/SSMS thật theo yêu cầu giảng viên. Không chụp session cookie, credentials hay OTP.

## Cart + COD — yêu cầu bổ sung 01/10/2026

Chạy đúng project D:\24162023_02, ví dụ URL `http://localhost:18082/24162023_02`. Chụp toàn màn hình với URL/IDE/SSMS hiện rõ. Không chụp credentials hoặc OTP thật.

- [ ] Book detail sau login: có giá, tồn kho, quantity và nút **Thêm vào giỏ**.
- [ ] Cart: ảnh/tên sách, giá, số lượng, subtotal và total.
- [ ] Add cùng sách cộng dồn một dòng; tăng/giảm và cập nhật số lượng.
- [ ] Lỗi vượt stock (có thể nhập request qua DevTools khi cần chứng minh validation server); dữ liệu giỏ không tự clamp.
- [ ] Xóa dòng và trạng thái giỏ rỗng.
- [ ] Checkout COD: người nhận, điện thoại giữ số 0 đầu, địa chỉ, tổng tiền.
- [ ] Success: mã đơn, **Đơn hàng mới**, COD, **Chưa thanh toán**, receiver và items; refresh vẫn cùng mã đơn.
- [ ] SQL `orders`: user_id, total_amount, status=NEW, payment_method=COD, payment_status=UNPAID.
- [ ] SQL `order_items`: tên/giá snapshot, quantity và line_total đúng đơn.
- [ ] SQL `books.quantity` trước/sau checkout giảm đúng; `cart_items` của user đã được xóa.
- [ ] Đăng nhập user khác không đọc được success của đơn trên.
- [ ] Kết quả build/static audit và JDBC/HTTP tests trong terminal.

Test tự động dọn fixture. Để chụp screenshot, thao tác demo thủ công tạo đơn trong database đã xác nhận. Order History/filter hiện có ở phần bổ sung phía trên.

Chụp **toàn màn hình**, gồm tên file/tab IDE hoặc URL trình duyệt. Mỗi câu chụp lần lượt DAO → Service → Controller → View → kết quả.
Java root: `src/main/java/vn/edu/webexam/`. JSP root: `src/main/webapp/WEB-INF/`.
Tên file Java dưới đây có hậu tố đầy đủ; có thể chụp nhiều ảnh nếu nội dung dài.

## Câu 1
- [ ] Cây project: model, dao, service, controller, filter, util, views, decorators.
- [ ] pom.xml: packaging war, Servlet/JSTL/JDBC/SiteMesh dependencies.
- [ ] database.sql: 5 bảng, PK/FK/IDENTITY; SSMS schema và SELECT seed.
- [ ] util/DBConnection_24162023.java và application.properties (che credentials thật).
- [ ] web.xml, sitemesh3.xml.
- [ ] decorators/user.jsp, admin.jsp, header.jspf, footer.jspf.
- [ ] Trình duyệt Home có menu và footer đúng họ tên/MSSV/đề.
- [ ] Admin đăng nhập có menu Trang quản trị và layout admin.

## Câu 2
- [ ] dao/UserDAO_24162023.java: findByEmail/create/updateLastLogin.
- [ ] service/AuthService_24162023.java, OTPService_24162023.java.
- [ ] model/PendingRegistration_24162023.java: hết hạn và số lần thử.
- [ ] util/PasswordUtil_24162023.java.
- [ ] controller/RegisterController_24162023.java, OTPController_24162023.java.
- [ ] controller/LoginController_24162023.java, LogoutController_24162023.java.
- [ ] filter/AdminFilter_24162023.java, AuthenticationFilter_24162023.java, RequestFilter_24162023.java.
- [ ] views/register.jsp, verify-otp.jsp, login.jsp.
- [ ] Đăng ký → email OTP thật nếu đã cấu hình SMTP; nếu demo, ghi rõ ảnh log OTP là demo.
- [ ] OTP sai, OTP đúng, đăng nhập sai, đăng nhập User vào Home, đăng xuất.
- [ ] User thường truy cập /admin/books nhận 403; khách chuyển login.
- [ ] SQL users: tài khoản chỉ xuất hiện sau OTP; last_login cập nhật. Che hash nếu không cần.

## Câu 3
- [ ] dao/BookDAO_24162023.java: authorPages + JOIN + ROW_NUMBER.
- [ ] service/BookService_24162023.java: PAGE_SIZE = 3.
- [ ] controller/HomeController_24162023.java.
- [ ] views/home.jsp, fragments/book.jspf, fragments/author-link.jspf.
- [ ] Home trang 1: nhóm tác giả, tối đa 3 sách mỗi nhóm, đủ trường.
- [ ] Home trang 2 của tác giả đầu; tác giả khác giữ trang; Previous/Next và số trang.
- [ ] Tiêu đề là liên kết tới /book-detail?id=...

## Câu 4
- [ ] dao/BookDAO_24162023.java: find; dao/RatingDAO_24162023.java: JOIN users + save.
- [ ] service/RatingService_24162023.java.
- [ ] controller/BookDetailController_24162023.java, ReviewController_24162023.java.
- [ ] views/book-detail.jsp.
- [ ] Chi tiết sách #1 với thông tin, số review, tên người review và nội dung.
- [ ] Khách thấy yêu cầu đăng nhập.
- [ ] User thêm review, gửi lại cập nhật một review, refresh không submit lại.

## Câu 6
- [ ] dao/BookDAO_24162023.java: page, save, delete + transaction.
- [ ] service/BookService_24162023.java: validation, save, delete.
- [ ] controller/AdminBookController_24162023.java.
- [ ] filter/AdminFilter_24162023.java.
- [ ] views/admin/books/list.jsp, form.jsp, detail.jsp.
- [ ] List đủ trường + View/Edit/Delete + Add Book.
- [ ] Trang 1, trang 2 và nút phân trang.
- [ ] Tạo sách (chọn tác giả), xem, sửa và kết quả sau sửa.
- [ ] Hộp confirm xóa và danh sách sau khi xóa sách có review/liên kết.

## Build và hồ sơ nộp
- [ ] Terminal mvn clean package: BUILD SUCCESS và số test.
- [ ] File target/24162023_02.war.
- [ ] Naming audit: tất cả class/interface có _24162023.
- [ ] Nếu cần ảnh repository: tự tạo/push repository được giáo viên yêu cầu rồi chụp URL; chưa cấu hình remote trong project này.
- [ ] Chèn screenshot thật theo từng câu vào 24162023.docx.
- [ ] Nộp 24162023.docx và 24162023_02.zip.
