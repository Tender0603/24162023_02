<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>
<sitemesh:write property="title"/>
</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
<sitemesh:write property="head"/>
</head>
<body class="user-layout">
<%@ include file="header.jspf" %>
<main>
<sitemesh:write property="body"/>
</main>
<%@ include file="footer.jspf" %>
</body>
</html>
