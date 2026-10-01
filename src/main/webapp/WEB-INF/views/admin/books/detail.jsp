<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Admin · Chi tiết sách</title>
</head>
<body>
<h1>Book #${book.bookid}</h1>
<article class="panel detail">
<%@ include file="../../fragments/book.jspf" %>
</article>
<p>Price: <c:out value="${book.price}"/>
</p>
<p>
<c:out value="${book.description}"/>
</p>
<a class="button" href="${pageContext.request.contextPath}/admin/books?action=edit&id=${book.bookid}">Edit</a> <a href="${pageContext.request.contextPath}/admin/books">Danh sách</a>
</body>
</html>
