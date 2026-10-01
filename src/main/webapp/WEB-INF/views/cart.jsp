<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title>Giỏ hàng</title></head><body>
<h1>Giỏ hàng</h1>
<c:if test="${not empty cartMessage}"><p class="success"><c:out value="${cartMessage}"/></p></c:if>
<c:if test="${not empty cartError}"><p class="error" role="alert"><c:out value="${cartError}"/></p></c:if>
<c:choose><c:when test="${empty cart.items}"><p>Giỏ hàng đang trống.</p></c:when><c:otherwise>
<div class="table-wrap"><table><thead><tr><th>Sách</th><th>Giá hiện tại</th><th>Số lượng</th><th>Thành tiền</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="item" items="${cart.items}">
<c:set var="book" value="${item.book}"/>
<tr data-book-id="${book.bookid}"><td>
<%@ include file="fragments/cover-url.jspf" %>
<img class="cart-cover" src="<c:out value='${coverUrl}'/>" alt="Bìa sách">
<a href="${pageContext.request.contextPath}/book-detail?id=${book.bookid}"><c:out value="${book.title}"/></a>
<p class="muted">Tồn kho: <c:out value="${book.quantity}" default="Chưa xác định"/></p>
<c:if test="${not item.available}"><p class="error">Không đủ tồn kho hoặc giá không hợp lệ. Hãy cập nhật / xóa dòng này.</p></c:if>
</td><td><c:out value="${book.price}" default="Chưa có giá"/></td><td>
<form method="post" class="quantity-form" action="${pageContext.request.contextPath}/cart">
<input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="bookId" value="${book.bookid}">
<button name="action" value="decrease" formnovalidate aria-label="Giảm số lượng" ${item.quantity <= 1 ? 'disabled' : ''}>−</button>
<input aria-label="Số lượng" type="number" name="quantity" value="${item.quantity}" min="1" max="${book.quantity}" required>
<button name="action" value="increase" formnovalidate aria-label="Tăng số lượng" ${book.quantity == null or item.quantity >= book.quantity ? 'disabled' : ''}>+</button>
<button name="action" value="update">Cập nhật</button>
</form></td><td class="line-total"><c:out value="${item.lineTotal}" default="Không xác định"/></td><td>
<form method="post" action="${pageContext.request.contextPath}/cart">
<input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="bookId" value="${book.bookid}">
<button name="action" value="remove">Xóa</button></form></td></tr>
</c:forEach></tbody></table></div>
<p class="cart-total">Tổng tiền: <strong><c:out value="${cart.total}" default="Không xác định"/></strong></p>
<c:if test="${cart.available}"><a class="button" href="${pageContext.request.contextPath}/checkout">Thanh toán COD</a></c:if>
</c:otherwise></c:choose>
<p><a href="${pageContext.request.contextPath}/products">Tiếp tục xem sách</a></p>
</body></html>
