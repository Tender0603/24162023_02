<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<title>Chi tiết sách</title>
</head>
<body>
<h1>Chi tiết sách</h1>
<article class="panel detail">
<%@ include file="fragments/book.jspf" %>
</article>
<p>Giá: <c:out value="${book.price}"/>
</p>
<p>
<c:out value="${book.description}"/>
</p>
<c:choose>
<c:when test="${empty sessionScope.user}">
<p><a href="${pageContext.request.contextPath}/login">Đăng nhập để thêm vào giỏ</a></p>
</c:when>
<c:when test="${book.price != null and book.price >= 0 and book.quantity != null and book.quantity > 0}">
<form method="post" class="panel cart-add" action="${pageContext.request.contextPath}/cart">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="action" value="add">
<input type="hidden" name="bookId" value="${book.bookid}">
<label for="buy-quantity">Số lượng mua</label>
<input id="buy-quantity" type="number" name="quantity" value="1" min="1" max="${book.quantity}" required>
<button>Thêm vào giỏ</button>
</form>
</c:when>
<c:otherwise><p class="error">Sách hết hàng hoặc chưa có giá / tồn kho hợp lệ.</p></c:otherwise>
</c:choose>
<h2>Reviews (${book.reviewCount})</h2>
<c:forEach var="review" items="${reviews}">
<div class="panel">
<strong>
<c:out value="${review.fullname}"/>
</strong> · <c:out value="${review.rating}"/>/5<p>
<c:out value="${review.reviewText}"/>
</p>
</div>
</c:forEach>
<c:if test="${empty reviews}">
<p>Chưa có đánh giá.</p>
</c:if>
<c:if test="${param.saved == '1'}">
<p class="success">Đã lưu đánh giá.</p>
</c:if>
<c:if test="${not empty error}">
<p class="error">
<c:out value="${error}"/>
</p>
</c:if>
<c:choose>
<c:when test="${not empty sessionScope.user}">
<h2>Thêm / cập nhật review</h2>
<p>Mỗi tài khoản có một review cho mỗi sách. Gửi lại sẽ cập nhật review của bạn.</p>
<form method="post" class="panel" action="${pageContext.request.contextPath}/review">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="bookid" value="${book.bookid}">
<label>Điểm đánh giá</label>
<select name="rating">
<c:forEach begin="1" end="5" var="score">
<option value="${score}" ${myReview.rating == score ? 'selected' : ''}>${score}/5</option>
</c:forEach>
</select>
<label>Nội dung review</label>
<textarea name="review_text" maxlength="4000" required>
<c:out value="${myReview.reviewText}"/>
</textarea>
<button>Submit</button>
</form>
</c:when>
<c:otherwise>
<p>
<a href="${pageContext.request.contextPath}/login">Đăng nhập để thêm review</a>
</p>
</c:otherwise>
</c:choose>
</body>
</html>
