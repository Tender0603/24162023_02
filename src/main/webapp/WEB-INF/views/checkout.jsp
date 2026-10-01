<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title>Thanh toán COD</title></head><body>
<h1>Thanh toán COD</h1>
<c:if test="${not empty error}"><p class="error" role="alert"><c:out value="${error}"/></p></c:if>
<div class="table-wrap"><table><thead><tr><th>Sách</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th></tr></thead><tbody>
<c:forEach var="item" items="${cart.items}"><tr><td><c:out value="${item.book.title}"/></td><td><c:out value="${item.book.price}"/></td><td>${item.quantity}</td><td><c:out value="${item.lineTotal}"/></td></tr></c:forEach>
</tbody></table></div>
<p>Tổng tiền: <strong><c:out value="${cart.total}" default="Không xác định"/></strong></p>
<p>COD — thanh toán khi nhận hàng. Giá và tồn kho sẽ được kiểm tra lại khi đặt hàng.</p>
<c:choose><c:when test="${cart.available}">
<form method="post" class="panel" action="${pageContext.request.contextPath}/checkout">
<input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="checkoutKey" value="${checkoutKey}">
<label for="receiver-name">Người nhận</label><input id="receiver-name" name="receiverName" maxlength="100" value="<c:out value='${form.receiverName}'/>" required autocomplete="name">
<label for="receiver-phone">Điện thoại</label><input id="receiver-phone" type="tel" name="receiverPhone" maxlength="16" pattern="\+?[0-9]{9,15}" value="<c:out value='${form.receiverPhone}'/>" required autocomplete="tel">
<label for="shipping-address">Địa chỉ nhận hàng</label><textarea id="shipping-address" name="shippingAddress" maxlength="500" required autocomplete="street-address"><c:out value="${form.shippingAddress}"/></textarea>
<button>Đặt hàng COD</button>
</form></c:when><c:otherwise><p class="error">Giỏ có sách không thể mua. Vui lòng cập nhật giỏ hàng.</p></c:otherwise></c:choose>
<p><a href="${pageContext.request.contextPath}/cart">Quay lại giỏ hàng</a></p>
</body></html>
