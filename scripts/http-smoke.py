"""End-to-end tests against the isolated demo. Uses temporary users/books; see cleanup below."""
import http.cookiejar
import html
import json
import os
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

BASE = os.environ.get("EXAM_BASE_URL", "http://127.0.0.1:18080/24162023_02")
ROOT = Path(__file__).resolve().parent.parent
results = []

def check(name, condition):
    assert condition, name
    results.append(name)
    print("OK:", name, flush=True)

def client():
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))

def request(opener, path, data=None):
    body = urllib.parse.urlencode(data, doseq=True).encode() if data is not None else None
    try:
        response = opener.open(BASE + path, body, timeout=20)
    except urllib.error.HTTPError as error:
        response = error
    return response.status, response.read().decode("utf-8"), response.geturl()

def token(page):
    return re.search(r'name="csrf" value="([^"]+)"', page).group(1)

def login(opener, email, password):
    _, page, _ = request(opener, "/login")
    return request(opener, "/login", dict(csrf=token(page), email=email, password=password))

guest = client()
status, page, _ = request(guest, "/home")
check("Home HTTP 200 and User SiteMesh decorator", status == 200 and 'class="user-layout"' in page)
check("Footer student, MSSV, exam", "Trần Hoàng Tiến Đạt" in page and "MSSV: 24162023" in page and "Mã đề: 02" in page)
check("Home groups have 3/3/2 books", [s.count('class="card"') for s in re.findall(r'<section.*?</section>',page,re.S)] == [3,3,2])
check("Admin menu hidden for guest", ">Trang quản trị</a>" not in page)
status, page, _ = request(guest, "/home?page_1=2&page_2=2")
check("Independent author pagination 2/1/2 books", status == 200 and [s.count('class="card"') for s in re.findall(r'<section.*?</section>',page,re.S)] == [2,1,2])
check("Pagination retains other author page", "page_2=2" in page)
check("Bad pagination rejected", request(guest, "/home?page_1=oops")[0] == 400)
check("Anonymous admin redirected to login", request(guest, "/admin/books")[2].endswith("/login"))
check("Unknown book 404", request(guest, "/book-detail?id=2147483647")[0] == 404)
check("Book detail reviews with user name", "Nguyễn Văn An" in request(guest, "/book-detail?id=1")[1])
check("SQL injection login rejected", "không đúng" in login(guest,"' OR 1=1 --","bad")[1])
check("CSRF missing rejected", request(guest,"/login",dict(email="admin@example.com",password="Admin@123"))[0] == 403)

user = client()
check("User login -> home", login(user,"user1@example.com","User@123")[2].endswith("/home"))
check("User direct admin access 403", request(user,"/admin/books")[0] == 403)
check("User direct /admin access 403", request(user,"/admin")[0] == 403)

admin = client()
status, page, url = login(admin,"admin@example.com","Admin@123")
check("Admin login, Admin SiteMesh, menu", status == 200 and "/admin/books" in url and 'class="admin-layout"' in page and ">Trang quản trị</a>" in page)
check("Admin list page size 3", len(re.findall(r'<tr>\s*<td>',page)) == 3)
check("Admin pagination page 2", request(admin,"/admin/books?page=2")[0] == 200)
status, page, _ = request(admin,"/admin/books?action=new")
check("Admin create JSP renders", status == 200)
book_data = dict(csrf=token(page), action="save", isbn="900001", title="HTTP Test Book",
                 publisher="Test Publisher", price="25.50", publish_date="2026-01-01",
                 quantity="5", cover_image="assets/cover.svg", description="Temporary integration fixture",
                 authorIds=["1","2"])
status, page, url = request(admin,"/admin/books",book_data)
check("Create book and redirect detail", status == 200 and "action=view&id=" in url and "HTTP Test Book" in page)
check("Create without upload uses default cover", "/assets/cover.svg" in page)
book_id = urllib.parse.parse_qs(urllib.parse.urlparse(url).query)["id"][0]
try:
    status, page, _ = request(admin,f"/admin/books?action=edit&id={book_id}")
    check("Edit preselects both authors", status == 200 and page.count("checked") == 2)
    book_data.update(csrf=token(page), id=book_id, title="HTTP Test Updated")
    check("Update book persists", "HTTP Test Updated" in request(admin,"/admin/books",book_data)[1])
    _, page, _ = request(user,f"/book-detail?id={book_id}")
    review = dict(csrf=token(page), bookid=book_id, userid="1", rating="5", review_text="<script>alert(1)</script>")
    status, page, _ = request(user,"/review",review)
    check("Review inserted, browser userid ignored, XSS escaped", status == 200 and "Nguyễn Văn An" in page and "&lt;script&gt;alert(1)&lt;/script&gt;" in page and "<script>alert(1)</script>" not in page)
    review.update(csrf=token(page), review_text="Updated review")
    _, page, _ = request(user,"/review",review)
    check("Duplicate review updates one row", "Reviews (1)" in page and "Updated review" in page)
    review.update(csrf=token(page), rating="6")
    check("Invalid rating rejected", "Điểm đánh giá từ 1 đến 5." in request(user,"/review",review)[1])
finally:
    _, page, _ = request(admin,"/admin/books")
    status, page, _ = request(admin,"/admin/books",dict(csrf=token(page),action="delete",id=book_id))
    check("Delete with author mappings and review succeeds", status == 200 and request(guest,f"/book-detail?id={book_id}")[0] == 404)

# OTP only in explicit demo mode; no bypass endpoint is used.
registration = client()
email = f"smoke{int(time.time())}@example.com"
_, page, _ = request(registration,"/register")
status, page, url = request(registration,"/register",dict(csrf=token(page),email=email,fullname="OTP Test",phone="912345678",password="Test@1234"))
check("Register -> OTP page", status == 200 and url.endswith("/verify-otp"))
check("Cannot login before OTP activation", "không đúng" in login(client(),email,"Test@1234")[1])
_, page, _ = request(registration,"/verify-otp")
_, page, _ = request(registration,"/verify-otp",dict(csrf=token(page),otp="wrong"))
check("Wrong OTP rejected", "OTP không đúng." in page)
log = Path(os.environ.get("EXAM_OTP_LOG", str(ROOT / ".runtime/logs/stderr.log"))).read_text(encoding="utf-8")
otp = re.findall(re.escape(email)+r": (\d{6})",log)[-1]
_, page, url = request(registration,"/verify-otp",dict(csrf=token(page),otp=otp))
check("Correct OTP activates account", "activated=1" in url)
_, reused_page, _ = request(registration,"/verify-otp",dict(csrf=token(request(registration,"/verify-otp")[1]),otp=otp))
check("Activated OTP cannot be reused before login", "Không có đăng ký" in reused_page)
status, page, url = login(registration,email,"Test@1234")
check("Activated account can login", status == 200 and url.endswith("/home"))
_, page, _ = request(registration,"/login")
_, logged_out_page, _ = request(registration,"/logout",dict(csrf=token(page)))
check("Logout removes authenticated header", "OTP Test" not in logged_out_page and ">Đăng xuất</button>" not in logged_out_page)
check("Logout invalidates session", request(registration,"/admin/books")[2].endswith("/login"))
check("OTP cannot be reused", "Không có đăng ký" in request(registration,"/verify-otp",dict(csrf=token(request(registration,"/verify-otp")[1]),otp=otp))[1])
(ROOT / ".runtime/smoke-user.txt").write_text(email)
(ROOT / ".runtime/cleanup-smoke.sql").write_text(
    "USE WebExam02_24162023;\nDELETE FROM users WHERE email='" + email + "';\n", encoding="utf-8")
(ROOT / ".runtime/http-results.json").write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding="utf-8")
print(f"Completed {len(results)} HTTP checks. Temporary OTP account: {email}")
