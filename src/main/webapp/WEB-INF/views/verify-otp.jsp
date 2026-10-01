<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Kích hoạt OTP</title>
</head>
<body>
<h1>Kích hoạt OTP</h1>
<c:if test="${not empty error}">
<p class="error">
<c:out value="${error}"/>
</p>
</c:if>
<a href="${pageContext.request.contextPath}/register">Đăng ký lại / gửi mã mới</a>
<form class="panel" method="post" action="${pageContext.request.contextPath}/verify-otp">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<p>OTP có hiệu lực 5 phút và tối đa 5 lần thử. Tài khoản chỉ được lưu sau khi xác thực.</p>
<label>Mã OTP</label>
<input name="otp" pattern="[0-9]{6}" maxlength="6" required autocomplete="one-time-code">
<button type="submit">Kích hoạt OTP</button>
</form>
</body>
</html>
