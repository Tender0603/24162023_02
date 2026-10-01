<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Quản trị Books</title>
</head>
<body>
<h1>Quản lý Books</h1>
<a class="button" href="${pageContext.request.contextPath}/admin/books?action=new">+ Add Book</a>
<c:if test="${not empty message}">
<p class="success">
<c:out value="${message}"/>
</p>
</c:if>
<div class="table-wrap panel">
<table>
<thead>
<tr>
<th>bookid</th>
<th>isbn</th>
<th>title</th>
<th>publisher</th>
<th>price</th>
<th>publish_date</th>
<th>quantity</th>
<th>Action</th>
</tr>
</thead>
<tbody>
<c:forEach var="book" items="${books}">
<tr>
<td>${book.bookid}</td>
<td>
<c:out value="${book.isbn}"/>
</td>
<td>
<c:out value="${book.title}"/>
</td>
<td>
<c:out value="${book.publisher}"/>
</td>
<td>
<c:out value="${book.price}"/>
</td>
<td>
<c:out value="${book.publishDate}"/>
</td>
<td>
<c:out value="${book.quantity}"/>
</td>
<td>
<div class="actions">
<a href="${pageContext.request.contextPath}/admin/books?action=view&id=${book.bookid}">View</a>
<a href="${pageContext.request.contextPath}/admin/books?action=edit&id=${book.bookid}">Edit</a>
<form method="post" action="${pageContext.request.contextPath}/admin/books" onsubmit="return confirm('Xóa sách cùng tác giả liên kết và tất cả review?');">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="action" value="delete">
<input type="hidden" name="id" value="${book.bookid}">
<button>Delete</button>
</form>
</div>
</td>
</tr>
</c:forEach>
</tbody>
</table>
<c:if test="${empty books}">
<p>Chưa có sách.</p>
</c:if>
</div>
<nav class="pagination">
<c:if test="${page > 1}">
<a href="?page=${page-1}">Trang trước</a>
</c:if>
<c:forEach begin="1" end="${pages}" var="p">
<c:choose>
<c:when test="${p == page}">
<strong aria-current="page">${p}</strong>
</c:when>
<c:otherwise>
<a href="?page=${p}">${p}</a>
</c:otherwise>
</c:choose>
</c:forEach>
<c:if test="${page < pages}">
<a href="?page=${page+1}">Trang sau</a>
</c:if>
</nav>
</body>
</html>
