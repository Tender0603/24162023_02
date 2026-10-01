"""Local SQL Server + rendered HTTP checks. Uses only isolated fixtures and cleans them.
Set CART_COD_DB_CONFIRMED=YES; optional ORDER_BROWSER=YES runs real Chrome checks.
"""
import base64
import hashlib
import http.cookiejar
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import urllib.error
import urllib.parse
import urllib.request
import uuid

ROOT = Path(__file__).resolve().parent.parent
BASE = os.environ.get("EXAM_BASE_URL", "http://127.0.0.1:18082/24162023_02")
assert os.environ.get("CART_COD_DB_CONFIRMED") == "YES"
assert urllib.parse.urlparse(BASE).hostname in ("localhost", "127.0.0.1")
STATUSES = dict(NEW="Đơn hàng mới", CONFIRMED="Đã xác nhận", PREPARING="Chuẩn bị hàng",
               SHIPPING="Vận chuyển", DELIVERING="Giao hàng", DELIVERED="Đã giao",
               CANCELLED="Đơn hàng hủy", RETURNED="Đơn hàng hoàn")
users, books, results = [], [], []


def sql(statement):
    result = subprocess.run(["sqlcmd", "-S", os.environ.get("EXAM_SQL_SERVER", r".\SQLEXPRESS"),
                             "-E", "-C", "-b", "-l", "5", "-d", "WebExam02_24162023", "-h", "-1", "-W", "-s", "|"],
                            input="SET NOCOUNT ON;\n" + statement + "\nGO\n",
                            capture_output=True, text=True, timeout=30)
    if result.returncode:
        raise RuntimeError("SQL history fixture/check failed: " + str(result.returncode))
    return result.stdout.strip()


def check(name, condition):
    assert condition, name
    results.append(name)
    print("PASS:", name, flush=True)


def client():
    jar = http.cookiejar.CookieJar()
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar)), jar


def request(opener, path, data=None):
    try:
        response = opener.open(BASE + path, None if data is None else urllib.parse.urlencode(data).encode(), timeout=20)
    except urllib.error.HTTPError as e:
        response = e
    return response.status, response.read().decode("utf-8"), response.geturl(), response.headers


def fixture_user():
    email, password = "history-" + uuid.uuid4().hex + "@t.dev", secrets.token_urlsafe(18)
    hashed = base64.b64encode(hashlib.pbkdf2_hmac("sha256", password.encode(), ("WebExam02:" + email).encode(), 210000, 24)).decode()
    uid = int(sql(f"INSERT users(email,fullname,passwd,is_admin) VALUES('{email}','History fixture','{hashed}',0); SELECT CAST(SCOPE_IDENTITY() AS int);"))
    users.append(uid)
    opener, jar = client()
    page = request(opener, "/login")[1]
    csrf = re.search(r'name="csrf" value="([^"]+)"', page).group(1)
    check("Fixture login", request(opener, "/login", dict(email=email, password=password, csrf=csrf))[2].endswith("/home"))
    return uid, opener, jar


def order(user, book, status="NEW"):
    oid = int(sql(f"INSERT orders(user_id,receiver_name,receiver_phone,shipping_address,total_amount,status,payment_method,payment_status,checkout_key,created_at) VALUES({user},N'History <b>receiver</b>','0901234567',N'History <script>address</script>',24.70,'{status}','COD','UNPAID','{uuid.uuid4()}','2026-01-01'); SELECT CAST(SCOPE_IDENTITY() AS bigint);"))
    sql(f"INSERT order_items(order_id,book_id,book_name_snapshot,unit_price_snapshot,quantity,line_total) VALUES({oid},{book},N'Original <b>snapshot</b>',12.35,2,24.70);")
    return oid


def ids(page):
    return [int(x) for x in re.findall(r'data-order-id="(\d+)"', page)]


def snapshot(user_ids, book):
    owners = ",".join(map(str, user_ids))
    # Full fixture rows, not just counts: detects a GET changing status, totals, stock or snapshots.
    return sql(f"SELECT * FROM orders WHERE user_id IN ({owners}) ORDER BY order_id; SELECT * FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id IN ({owners})) ORDER BY order_item_id; SELECT * FROM cart_items WHERE user_id IN ({owners}); SELECT * FROM books WHERE bookid={book};")


try:
    check("Verified project DB", sql("SELECT DB_NAME();") == "WebExam02_24162023")
    a, alice, alice_jar = fixture_user()
    b, bob, bob_jar = fixture_user()
    check("Empty history", "Bạn chưa có đơn hàng nào." in request(alice, "/orders")[1])
    book = int(sql("INSERT books(title,price,quantity) VALUES('Original book',12.35,10); SELECT CAST(SCOPE_IDENTITY() AS int);"))
    books.append(book)
    first = order(a, book)
    second = order(a, book, "DELIVERED")
    foreign = order(b, book)
    guest, _ = client()
    for path in ("/orders", f"/orders/detail?id={first}", "/orders/anything"):
        check("Guest blocked " + path, request(guest, path)[2].endswith("/login"))
    status, page, _, headers = request(alice, "/orders")
    check("ALL: own orders with different statuses, newest ID on timestamp tie", status == 200 and ids(page) == [second, first])
    check("All required summary values", all(x in page for x in ("24.70", "COD", "UNPAID", "<td>01/01/2026 00:00:00</td>", "Ngày đặt (UTC)", "History &lt;b&gt;receiver&lt;/b&gt;")))
    check("No browser cache", headers.get("Cache-Control") == "no-store")
    check("Active ALL", re.search(r'aria-current="page">Tất cả', page) is not None)
    sql(f"UPDATE orders SET created_at='2026-10-01T07:19:56.5935399' WHERE order_id={first};")
    updated_history = request(alice, "/orders")[1]
    check("Newest created time precedes larger ID", ids(updated_history) == [first, second])
    check("History formats fractional UTC timestamp without time shift", "<td>01/10/2026 07:19:56</td>" in updated_history)
    check("Forged userId ignored", ids(request(alice, f"/orders?userId={b}")[1]) == [first, second])
    before = snapshot(users, book)
    request(alice, "/orders?status=NEW")
    check("GET history does not mutate database", snapshot(users, book) == before)
    status, detail, _, headers = request(alice, f"/orders/detail?id={first}&userId={b}")
    check("Owned detail and item snapshots", status == 200 and all(x in detail for x in ("Original &lt;b&gt;snapshot&lt;/b&gt;", "12.35", "24.70", "0901234567", "<td>2</td>", "Ngày đặt (UTC): 01/10/2026 07:19:56</p>", "COD", "UNPAID")))
    check("Detail escapes receiver/address", "History &lt;b&gt;receiver&lt;/b&gt;" in detail and "&lt;script&gt;address&lt;/script&gt;" in detail)
    check("GET detail does not mutate database", snapshot(users, book) == before)
    check("Detail no browser cache", headers.get("Cache-Control") == "no-store")
    for opener, oid in ((alice, foreign), (bob, first)):
        status, detail, _, _ = request(opener, f"/orders/detail?id={oid}")
        check("Cross-user detail 404 with no private data", status == 404 and all(x not in detail for x in ("0901234567", "24.70", "Original", "History &lt;b&gt;receiver")))
    for invalid in ("HACK", "abc", "", "NEW' OR 1=1--", "'; DROP TABLE orders;--"):
        status, body, _, _ = request(alice, "/orders?" + urllib.parse.urlencode(dict(status=invalid)))
        check("Invalid filter safely rejected " + invalid, status == 400 and "Exception" not in body and not ids(body))
    check("Injection has no database side effects", snapshot(users, book) == before)
    for invalid in ("abc", "0", "-1", "9223372036854775808", "1 OR 1=1"):
        check("Invalid detail ID " + invalid, request(alice, "/orders/detail?" + urllib.parse.urlencode(dict(id=invalid)))[0] == 400)
    check("Missing detail ID", request(alice, "/orders/detail")[0] == 400)
    check("Nonexistent detail", request(alice, "/orders/detail?id=9223372036854775807")[0] == 404)
    for code, label in STATUSES.items():
        sql(f"UPDATE orders SET status='{code}' WHERE order_id IN ({first},{foreign});")
        status, page, _, _ = request(alice, "/orders")
        row = re.search(rf'<tr data-order-id="{first}">(.*?)</tr>', page, re.S).group(1)
        check("Direct SQL -> refresh history " + code, status == 200 and label in row)
        status, filtered, _, _ = request(alice, "/orders?status=" + code)
        check("Filter ownership/current status " + code, status == 200 and set(ids(filtered)) == ({first, second} if code == "DELIVERED" else {first}))
        check("Active filter " + code, re.search(rf'aria-current="page">{label}</a>', filtered) is not None)
        check("Detail live status " + code, label in request(alice, f"/orders/detail?id={first}")[1])
        if code != "NEW":
            check("Updated order leaves NEW " + code, first not in ids(request(alice, "/orders?status=NEW")[1]))
    sql(f"UPDATE books SET title='Changed title',price=999 WHERE bookid={book};")
    detail = request(alice, f"/orders/detail?id={first}")[1]
    check("Name and price snapshots survive book edit", "Original &lt;b&gt;snapshot&lt;/b&gt;" in detail and "12.35" in detail and "Changed title" not in detail and "999" not in detail)
    sql(f"DELETE books WHERE bookid={book};")
    check("Deleted book FK set null", sql(f"SELECT COUNT(*) FROM order_items WHERE order_id={first} AND book_id IS NULL;") == "1")
    check("Deleted book history remains", set(ids(request(alice, "/orders")[1])) == {first, second})
    detail = request(alice, f"/orders/detail?id={first}")[1]
    check("Deleted book detail retains snapshots", "Original &lt;b&gt;snapshot&lt;/b&gt;" in detail and "12.35" in detail and "24.70" in detail)
    if os.environ.get("ORDER_BROWSER") == "YES":
        cookie = next(c for c in alice_jar if c.name == "JSESSIONID")
        other = next(c for c in bob_jar if c.name == "JSESSIONID")
        process = subprocess.run(["node", "scripts/order-history-browser.cjs"], cwd=ROOT,
                                 input=json.dumps(dict(base=BASE, orderId=first, foreignId=foreign,
                                                       session=cookie.value, otherSession=other.value)),
                                 text=True, timeout=120)
        check("Real Chrome navigation, filters, SQL refresh and ownership", process.returncode == 0)
finally:
    if users:
        owners = ",".join(map(str, users))
        sql(f"BEGIN TRAN; DELETE order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id IN ({owners})); DELETE orders WHERE user_id IN ({owners}); DELETE users WHERE id IN ({owners}); COMMIT;")
    if books:
        sql("DELETE books WHERE bookid IN (" + ",".join(map(str, books)) + ");")
(ROOT / ".runtime/order-history-results.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"Completed {len(results)} HTTP/DB checks; isolated fixtures cleaned.")
