r"""Real HTTP + SQL Server checks on the explicitly confirmed local project instance.
Creates isolated fixture users/books; finally removes only their records. No existing user carts touched.
Set CART_COD_DB_CONFIRMED=YES and EXAM_BASE_URL to the D:\24162023_02 instance.
SQLCMD uses Windows Authentication; no passwords are printed or saved.
"""
import base64
import hashlib
import http.cookiejar
import os
import re
import secrets
import subprocess
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = os.environ.get("EXAM_BASE_URL", "http://127.0.0.1:18082/24162023_02")
assert os.environ.get("CART_COD_DB_CONFIRMED") == "YES", "Confirm the target project database first"
assert urllib.parse.urlparse(BASE).hostname in ("127.0.0.1", "localhost"), "Local demo only"
results = []
users, books = [], []


def sql(statement):
    result = subprocess.run([
        "sqlcmd", "-S", os.environ.get("EXAM_SQL_SERVER", r".\SQLEXPRESS"), "-E", "-C",
        "-b", "-l", "5", "-d", "WebExam02_24162023", "-h", "-1", "-W", "-s", "|",
    ], input="SET NOCOUNT ON;\n" + statement + "\nGO\n", capture_output=True, text=True, timeout=30)
    if result.returncode:
        raise RuntimeError("SQL fixture/check failed; exit=" + str(result.returncode))
    return result.stdout.strip()


def check(name, condition):
    assert condition, name
    results.append(name)
    print("PASS:", name, flush=True)


def client():
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))


def request(opener, path, data=None):
    body = None if data is None else urllib.parse.urlencode(data).encode()
    try:
        response = opener.open(BASE + path, body, timeout=20)
    except urllib.error.HTTPError as error:
        response = error
    return response.status, response.read().decode("utf-8"), response.geturl()


def token(page, name="csrf"):
    return re.search(r'name="' + name + r'" value="([^"]+)"', page).group(1)


def fixture_user():
    email = "http-" + uuid.uuid4().hex + "@t.dev"
    password = secrets.token_urlsafe(18)
    encoded = base64.b64encode(hashlib.pbkdf2_hmac("sha256", password.encode(), ("WebExam02:" + email).encode(), 210000, 24)).decode()
    user = int(sql(f"INSERT users(email,fullname,passwd,is_admin) VALUES('{email}',N'HTTP Cart COD','{encoded}',0); SELECT CAST(SCOPE_IDENTITY() AS int);"))
    users.append(user)
    opener = client()
    _, page, _ = request(opener, "/login")
    status, _, url = request(opener, "/login", dict(csrf=token(page), email=email, password=password))
    check("Fixture user login", status == 200 and url.endswith("/home"))
    return user, opener


def cart_quantity(user, book):
    return int(sql(f"SELECT COALESCE((SELECT quantity FROM cart_items WHERE user_id={user} AND book_id={book}),0);"))


def order_count(user):
    return int(sql(f"SELECT COUNT(*) FROM orders WHERE user_id={user};"))


try:
    a, alice = fixture_user()
    b, bob = fixture_user()
    book = int(sql("INSERT books(title,price,quantity,cover_image) VALUES('HTTP Cart COD Book',12.35,5,'assets/cover.svg'); SELECT CAST(SCOPE_IDENTITY() AS int);"))
    books.append(book)
    guest = client()
    for path in ("/cart", "/checkout", "/checkout/success?id=1", "/cart/anything", "/checkout/anything"):
        check("Anonymous protected " + path, request(guest, path)[2].endswith("/login"))
    _, guest_page, _ = request(guest, "/login")
    for path in ("/cart", "/checkout"):
        check("Anonymous POST protected " + path, request(guest, path, dict(csrf=token(guest_page)))[2].endswith("/login"))
    empty_status, empty_page, empty_url = request(alice, "/checkout")
    check("Empty checkout redirects to rendered cart", empty_status == 200 and empty_url.endswith("/cart") and "Giỏ hàng đang trống" in empty_page)
    status, page, _ = request(alice, f"/book-detail?id={book}")
    check("Book detail Add to cart form renders", status == 200 and "Thêm vào giỏ" in page)
    csrf = token(page)

    def change(opener=alice, **values):
        return request(opener, "/cart", dict(csrf=token(request(opener, "/cart")[1]), bookId=book, **values))

    for path in ("/cart", "/checkout"):
        for value in (None, "invalid"):
            data = dict(action="add", bookId=book, quantity=1)
            if value is not None:
                data["csrf"] = value
            check("CSRF rejected " + path + " " + str(value), request(alice, path, data)[0] == 403)
    request(alice, f"/cart?action=add&bookId={book}&quantity=1")
    check("GET cart does not mutate", cart_quantity(a, book) == 0)
    status, page, _ = change(action="add", quantity=2, userId=b, price="0.01", total="0.01")
    check("Add belongs to session user and total uses DB", status == 200 and cart_quantity(a, book) == 2 and cart_quantity(b, book) == 0 and "24.70" in page)
    change(action="add", quantity=3)
    check("Same book increments one row", cart_quantity(a, book) == 5 and int(sql(f"SELECT COUNT(*) FROM cart_items WHERE user_id={a};")) == 1)
    for quantity in ("6", "0", "-1", "abc", "2147483648"):
        _, page, _ = change(action="update", quantity=quantity)
        check("Invalid quantity rejected " + quantity, 'role="alert"' in page and cart_quantity(a, book) == 5)
    change(action="update", quantity=1)
    check("Quantity one valid", cart_quantity(a, book) == 1)
    _, page, _ = change(action="decrease")
    check("Minus cannot go below one", 'role="alert"' in page and cart_quantity(a, book) == 1)
    change(action="increase")
    check("Plus increments", cart_quantity(a, book) == 2)
    change(action="decrease")
    check("Minus decrements", cart_quantity(a, book) == 1)
    change(action="update", quantity=5)
    _, page, _ = change(action="increase")
    check("Plus cannot exceed stock", 'role="alert"' in page and cart_quantity(a, book) == 5)
    change(opener=bob, action="update", quantity=1, userId=a)
    change(opener=bob, action="remove", userId=a)
    check("Other user cannot update/remove Alice cart", cart_quantity(a, book) == 5 and cart_quantity(b, book) == 0)
    change(action="remove")
    check("Remove works", cart_quantity(a, book) == 0)
    for bad in ("price=NULL", "price=-1", "price=12.35,quantity=NULL", "quantity=0"):
        sql(f"UPDATE books SET {bad} WHERE bookid={book};")
        _, page, _ = change(action="add", quantity=1)
        check("Unavailable book rejected " + bad, 'role="alert"' in page and cart_quantity(a, book) == 0)
    sql(f"UPDATE books SET price=12.35,quantity=5 WHERE bookid={book};")
    _, page, _ = request(alice, "/cart", dict(csrf=csrf, action="add", bookId=2147483647, quantity=1))
    check("Missing book rejected", 'role="alert"' in page)
    change(action="add", quantity=2)
    status, page, _ = request(alice, "/checkout")
    check("Checkout JSP renders COD and server total", status == 200 and "24.70" in page and "COD" in page)
    key = token(page, "checkoutKey")
    fields = dict(csrf=token(page), checkoutKey=key, receiverName="  Nguyễn An  ", receiverPhone="0901234567", shippingAddress="Số 1, TP HCM", total="0.01", price="0.01", userId=b, paymentMethod="CARD")
    request(alice, "/checkout?" + urllib.parse.urlencode(fields))
    check("GET checkout never creates order", order_count(a) == 0 and cart_quantity(a, book) == 2)
    for override in (dict(receiverName=" "), dict(receiverPhone="123"), dict(shippingAddress=" "), dict(checkoutKey=str(uuid.uuid4()))):
        status, _, _ = request(alice, "/checkout", dict(fields, **override))
        check("Receiver/key validation " + next(iter(override)), status == 400 and order_count(a) == 0)
    bob_csrf = token(request(bob, "/cart")[1])
    request(bob, "/checkout", dict(fields, csrf=bob_csrf, userId=a))
    check("Other user cannot checkout Alice cart/key", order_count(a) == 0 and order_count(b) == 0 and cart_quantity(a, book) == 2)
    sql(f"UPDATE books SET quantity=1 WHERE bookid={book};")
    status, page, _ = request(alice, "/checkout", fields)
    check("Stock change rejected with cart preserved", status == 400 and "tồn kho" in page and order_count(a) == 0 and cart_quantity(a, book) == 2)
    sql(f"UPDATE books SET quantity=5,price=15.25 WHERE bookid={book};")
    status, page, url = request(alice, "/checkout", fields)
    check("Valid checkout PRG success", status == 200 and "/checkout/success?id=" in url)
    order_id = int(urllib.parse.parse_qs(urllib.parse.urlparse(url).query)["id"][0])
    check("Success shows NEW COD unpaid current-price total and receiver", "Đơn hàng mới" in page and "COD" in page and "Chưa thanh toán" in page and "30.50" in page and "0901234567" in page and "Nguyễn An" in page)
    row = sql(f"SELECT user_id,total_amount,status,payment_method,payment_status,receiver_phone FROM orders WHERE order_id={order_id};")
    check("DB header belongs to Alice, ignores forged fields", row == f"{a}|30.50|NEW|COD|UNPAID|0901234567")
    check("DB item snapshot correct", sql(f"SELECT unit_price_snapshot,quantity,line_total FROM order_items WHERE order_id={order_id};") == "15.25|2|30.50")
    check("DB stock decremented and cart cleared", sql(f"SELECT quantity FROM books WHERE bookid={book};") == "3" and cart_quantity(a, book) == 0)
    _, _, duplicate_url = request(alice, "/checkout", fields)
    check("Duplicate submit returns same order", duplicate_url == url and order_count(a) == 1)
    request(alice, f"/checkout/success?id={order_id}")
    request(alice, f"/checkout/success?id={order_id}")
    check("Refresh success does not create orders", order_count(a) == 1)
    check("Success ownership enforced", request(bob, f"/checkout/success?id={order_id}")[0] == 404)
    check("POST success does not create orders", request(alice, f"/checkout/success?id={order_id}", dict(csrf=csrf))[0] == 405 and order_count(a) == 1)
    sql(f"UPDATE books SET price=99,title='Changed title' WHERE bookid={book};")
    _, page, _ = request(alice, f"/checkout/success?id={order_id}")
    check("Success retains snapshots after book changes", "HTTP Cart COD Book" in page and "30.50" in page)
    sql(f"UPDATE orders SET status='CONFIRMED' WHERE order_id={order_id};")
    check("Order model reads changed DB status", "Đã xác nhận" in request(alice, f"/checkout/success?id={order_id}")[1])
finally:
    if users:
        ids = ",".join(map(str, users))
        sql(f"BEGIN TRAN; DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id IN ({ids})); DELETE FROM orders WHERE user_id IN ({ids}); DELETE FROM cart_items WHERE user_id IN ({ids}); DELETE FROM users WHERE id IN ({ids}); COMMIT;")
    if books:
        sql("DELETE FROM books WHERE bookid IN (" + ",".join(map(str, books)) + ");")
print(f"Completed {len(results)} HTTP/DB checks; isolated fixtures cleaned.")
