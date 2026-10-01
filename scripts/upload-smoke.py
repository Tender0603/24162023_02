"""Upload HTTP checks. Requires Pillow only to generate small test images.
Run normally, restart Tomcat, then run --verify-redeploy to check persistence and clean fixtures.
"""
import http.cookiejar
import io
import json
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
from pathlib import Path
from PIL import Image

BASE = "http://127.0.0.1:18080/24162023_02"
ROOT = Path(__file__).resolve().parent.parent
STATE = ROOT / ".runtime/upload-test.json"
UPLOADS = ROOT / ".runtime/uploads/books"
results = []

def check(name, condition):
    assert condition, name
    results.append(name)
    print("OK:", name, flush=True)

def client():
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))

def request(opener, path, data=None, upload=None):
    headers = {}
    if upload is not None:
        boundary = "Exam" + uuid.uuid4().hex
        body = bytearray()
        for key, value in data.items():
            for entry in value if isinstance(value, list) else [value]:
                body.extend((f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{entry}\r\n').encode())
        filename, content, mime = upload
        body.extend((f'--{boundary}\r\nContent-Disposition: form-data; name="cover_file"; filename="{filename}"\r\nContent-Type: {mime}\r\n\r\n').encode())
        body.extend(content)
        body.extend(f"\r\n--{boundary}--\r\n".encode())
        body = bytes(body)
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
    else:
        body = urllib.parse.urlencode(data, doseq=True).encode() if data is not None else None
    try:
        response = opener.open(urllib.request.Request(BASE + path, body, headers), timeout=30)
    except urllib.error.HTTPError as error:
        response = error
    return response.status, response.read(), response.geturl(), response.headers

def text(opener, path, data=None, upload=None):
    status, body, url, headers = request(opener,path,data,upload)
    return status, body.decode("utf-8"), url

def token(page):
    return re.search(r'name="csrf" value="([^"]+)"',page).group(1)

def login(opener,email,password):
    _,page,_ = text(opener,"/login")
    return text(opener,"/login",dict(csrf=token(page),email=email,password=password))

def cover(page):
    return re.search(r'<img src="([^"]+)"',page).group(1).split("/24162023_02/",1)[1]

def image_data(format):
    output = io.BytesIO()
    Image.new("RGB",(24,32),(25,100,60)).save(output,format=format)
    return output.getvalue()

def files():
    return set(UPLOADS.glob("*")) if UPLOADS.exists() else set()

def cleanup(admin, state):
    _,page,_ = text(admin,"/admin/books")
    status,_,_ = text(admin,"/admin/books",dict(csrf=token(page),action="delete",id=state["id"]))
    check("Delete uploaded book still works",status == 200)
    for url in state["uploads"]:
        name = url.rsplit("/",1)[1]
        assert re.fullmatch(r"[0-9a-f-]{36}\.(png|jpg|jpeg|webp)",name)
        target = (UPLOADS / name).resolve()
        assert target.parent == UPLOADS.resolve()
        target.unlink(missing_ok=True)

admin = client()
check("Admin login",login(admin,"admin@example.com","Admin@123")[0] == 200)
if "--verify-redeploy" in sys.argv:
    state = json.loads(STATE.read_text())
    status,page,_ = text(admin,f'/book-detail?id={state["id"]}')
    check("Database cover path survives redeploy",status == 200 and cover(page) == state["current"])
    check("Image survives redeploy",request(admin,"/"+state["current"])[0] == 200)
    cleanup(admin,state)
    STATE.unlink()
    sys.exit(0)

state = {"id":None,"uploads":[]}
success = False
try:
    status,page,_ = text(admin,"/admin/books?action=new")
    check("Multipart file picker replaces text input",status == 200 and 'enctype="multipart/form-data"' in page and 'type="file"' in page and 'name="cover_image"' not in page)
    fields = dict(csrf=token(page),action="save",title="Upload Test",isbn="900002",
                  publisher="Upload Test",price="12.50",quantity="5",publish_date="2026-01-01",
                  description="Temporary upload fixture",authorIds=["1"])
    png = image_data("PNG")
    status,page,url = text(admin,"/admin/books",fields,("../cover.PNG",png,"image/png"))
    check("Create multipart PNG",status == 200 and "action=view&id=" in url)
    state["id"] = urllib.parse.parse_qs(urllib.parse.urlparse(url).query)["id"][0]
    current = cover(page)
    state["uploads"].append(current)
    check("UUID filename fits varchar(100)",bool(re.fullmatch(r"assets/uploads/books/[0-9a-f-]{36}\.png",current)) and len(current)<=100)
    status,data,_,headers = request(admin,"/"+current)
    check("Image bytes and MIME served correctly",status==200 and data==png and headers.get_content_type()=="image/png")
    check("Admin View renders uploaded cover",current in page)
    _,page,_ = text(admin,f'/book-detail?id={state["id"]}')
    check("Book Detail renders uploaded cover",cover(page)==current)
    _,page,_ = text(admin,"/home?page_1=2147483647")
    check("Home last author page renders uploaded cover",current in page)
    _,page,_ = text(admin,f'/admin/books?action=edit&id={state["id"]}')
    check("Edit previews current image",cover(page)==current)
    fields.update(id=state["id"],csrf=token(page),cover_image="assets/forged.png")
    _,page,_ = text(admin,"/admin/books",fields,("",b"","application/octet-stream"))
    check("No new file preserves DB image, ignores forged hidden path",cover(page)==current)
    for extension,format,mime in [("jpg","JPEG","image/jpeg"),("jpeg","JPEG","image/jpeg"),("webp","WEBP","image/webp")]:
        fields["csrf"] = token(text(admin,"/admin/books")[1])
        data = image_data(format)
        status,page,_ = text(admin,"/admin/books",fields,(f"new.{extension}",data,mime))
        check(f"Edit accepts {extension.upper()}",status==200 and cover(page)!=current and cover(page).endswith("."+extension))
        current=cover(page)
        state["uploads"].append(current)
        check(f"Serves {extension.upper()} bytes",request(admin,"/"+current)[1]==data)
    state["current"]=current
    baseline = files()
    for name,data,mime in [("fake.png",b"<script>bad</script>","image/png"),
                           ("fake.jpg",png,"image/jpeg"),("script.jsp",png,"image/png"),
                           ("empty.png",b"","image/png")]:
        status,page,_=text(admin,"/admin/books",fields,(name,data,mime))
        check("Reject "+name,status==200 and 'class="error"' in page)
        check("No file created for "+name,files()==baseline)
    status,page,_=text(admin,"/admin/books",fields,("large.png",png+b"x"*(5*1024*1024),"image/png"))
    check("Oversized upload returns friendly 413",status==413 and "5 MB" in page)
    check("Oversized upload leaves no file",files()==baseline)
    invalid = dict(fields,title="")
    status,page,_=text(admin,"/admin/books",invalid,("valid.png",png,"image/png"))
    check("Book validation error cleans new upload",status==200 and 'class="error"' in page and files()==baseline)
    _,page,_=text(admin,f'/book-detail?id={state["id"]}')
    check("Rejected edits preserve stored cover",cover(page)==current)
    bad_csrf=dict(fields,csrf="invalid")
    check("Multipart CSRF enforced",text(admin,"/admin/books",bad_csrf,("valid.png",png,"image/png"))[0]==403)
    user=client()
    login(user,"user1@example.com","User@123")
    denied=dict(fields,csrf=token(text(user,"/login")[1]))
    check("User cannot upload through admin",text(user,"/admin/books",denied,("valid.png",png,"image/png"))[0]==403)
    check("Unauthorized requests create no files",files()==baseline)
    check("Traversal filename not served",request(admin,"/assets/uploads/books/..%2Fapplication.properties")[0] in (400,404))
    STATE.write_text(json.dumps(state),encoding="utf-8")
    (ROOT/".runtime/upload-results.json").write_text(json.dumps(results,indent=2),encoding="utf-8")
    success=True
    print(f"{len(results)} upload checks passed. Restart Tomcat then run --verify-redeploy.")
finally:
    if not success and state["id"]:
        cleanup(admin,state)
