<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Thêm / sửa Books</title>
</head>
<body>
<h1>${book.bookid == 0 ? 'Add Book' : 'Edit Book'}</h1>
<c:if test="${not empty error}">
<p class="error">
<c:out value="${error}"/>
</p>
</c:if>
<form class="panel" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/books">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="action" value="save">
<c:if test="${book.bookid > 0}">
<input type="hidden" name="id" value="${book.bookid}">
<input type="hidden" name="originalQuantity" value="${book.originalQuantity}">
</c:if>
<label>ISBN</label>
<input type="number" name="isbn" min="0" max="2147483647" value="<c:out value='${book.isbn}'/>">
<label>Tiêu đề *</label>
<input name="title" maxlength="200" required value="<c:out value='${book.title}'/>">
<label>Publisher</label>
<input name="publisher" maxlength="100" value="<c:out value='${book.publisher}'/>">
<label>Price</label>
<input type="number" step="0.01" min="0" max="9999.99" name="price" value="<c:out value='${book.price}'/>">
<label>Publish date</label>
<input type="date" name="publish_date" value="<c:out value='${book.publishDate}'/>">
<label>Quantity</label>
<input type="number" min="0" max="2147483647" name="quantity" value="<c:out value='${book.quantity}'/>">
<label>Cover image</label>
<c:if test="${book.bookid > 0}">
<%@ include file="../../fragments/cover-url.jspf" %>
<p>Ảnh bìa hiện tại:</p>
<img src="<c:out value='${coverUrl}'/>" alt="Ảnh bìa hiện tại" width="160">
</c:if>
<input type="file" name="cover_file" accept="image/*">
<p class="muted">JPG, JPEG, PNG hoặc WEBP, tối đa 5 MB. Khi sửa, không chọn ảnh mới để giữ ảnh hiện tại.</p>
<label>Description</label>
<textarea name="description">
<c:out value="${book.description}"/>
</textarea>
<fieldset>
<legend>Tác giả *</legend>
<c:forEach var="author" items="${authors}">
<label>
<input type="checkbox" name="authorIds" value="${author.authorId}" ${selectedAuthors.contains(author.authorId) ? 'checked' : ''}> <c:out value="${author.authorName}"/>
</label>
</c:forEach>
</fieldset>
<button>Lưu sách</button> <a href="${pageContext.request.contextPath}/admin/books">Quay lại danh sách</a>
</form>
</body>
</html>
