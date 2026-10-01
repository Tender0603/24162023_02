<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Thư viện sách · Trang chủ</title>
</head>
<body>
<div class="eyebrow">BOOK HOUSE / KHÁM PHÁ</div>
<h1>Sách theo từng tác giả</h1>
<p class="muted">Mỗi trang 3 cuốn sách · Chọn tiêu đề để đọc chi tiết và đánh giá.</p>
<c:if test="${empty groups}">
<p>Chưa có tác giả. Hãy chạy database.sql.</p>
</c:if>
<c:forEach var="group" items="${groups}">
<section id="author-${group.authorId}">
<h2>Tác giả: <c:out value="${group.authorName}"/>
</h2>
<p>${group.total} sách</p>
<div class="grid">
<c:forEach var="book" items="${group.books}">
<article class="card">
<%@ include file="fragments/book.jspf" %>
</article>
</c:forEach>
</div>
<nav class="pagination">
<c:if test="${group.page > 1}">
<c:set var="targetPage" value="${group.page-1}"/>
<%@ include file="fragments/author-link.jspf" %>
<a href="<c:out value='${pageUrl}'/>#author-${group.authorId}">Trang trước</a>
</c:if>
<c:forEach begin="1" end="${group.pages}" var="p">
<c:set var="targetPage" value="${p}"/>
<%@ include file="fragments/author-link.jspf" %>
<c:choose>
<c:when test="${p == group.page}">
<strong aria-current="page">${p}</strong>
</c:when>
<c:otherwise>
<a href="<c:out value='${pageUrl}'/>#author-${group.authorId}">${p}</a>
</c:otherwise>
</c:choose>
</c:forEach>
<c:if test="${group.page < group.pages}">
<c:set var="targetPage" value="${group.page+1}"/>
<%@ include file="fragments/author-link.jspf" %>
<a href="<c:out value='${pageUrl}'/>#author-${group.authorId}">Trang sau</a>
</c:if>
</nav>
</section>
</c:forEach>
</body>
</html>
