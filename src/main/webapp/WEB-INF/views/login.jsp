<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Đăng nhập</title>
</head>
<body>
<h1>Đăng nhập</h1>
<c:if test="${not empty error}">
<p class="error">
<c:out value="${error}"/>
</p>
</c:if>
<c:if test="${param.activated == '1'}">
<p class="success">Kích hoạt thành công. Hãy đăng nhập.</p>
</c:if>
<a href="${pageContext.request.contextPath}/register">Đăng ký tài khoản</a>
<form class="panel" method="post" action="${pageContext.request.contextPath}/login">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<label>Email</label>
<input type="email" name="email" maxlength="50" required autocomplete="username">
<label>Mật khẩu</label>
<input type="password" name="password" maxlength="128" required autocomplete="current-password">
<button type="submit">Đăng nhập</button>
</form>
</body>
</html>
