<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Đăng ký</title>
</head>
<body>
<h1>Đăng ký</h1>
<c:if test="${not empty error}">
<p class="error">
<c:out value="${error}"/>
</p>
</c:if>
<form class="panel" method="post" action="${pageContext.request.contextPath}/register">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<label>Email</label>
<input type="email" name="email" maxlength="50" required>
<label>Họ tên</label>
<input name="fullname" maxlength="50" required>
<label>Điện thoại</label>
<input name="phone" inputmode="numeric" pattern="[0-9]{1,10}">
<label>Mật khẩu</label>
<input type="password" name="password" minlength="8" maxlength="128" required autocomplete="new-password">
<button type="submit">Đăng ký</button>
</form>
</body>
</html>
