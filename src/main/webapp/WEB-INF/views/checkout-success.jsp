<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title>Đơn hàng đã tạo</title></head><body>
<h1>Đơn hàng #${order.orderId}</h1>
<p class="success">Đơn hàng đã được ghi nhận.</p>
<p>Trạng thái: <strong><c:out value="${order.status.label}"/></strong></p>
<p>Phương thức: <c:out value="${order.paymentMethod}"/> — thanh toán khi nhận hàng.</p>
<p>Thanh toán: <c:choose><c:when test="${order.paymentStatus == 'UNPAID'}">Chưa thanh toán</c:when><c:when test="${order.paymentStatus == 'PAID'}">Đã thanh toán</c:when><c:otherwise>Đã hoàn tiền</c:otherwise></c:choose></p>
<div class="panel"><p>Người nhận: <c:out value="${order.receiver.receiverName}"/></p>
<p>Điện thoại: <c:out value="${order.receiver.receiverPhone}"/></p><p>Địa chỉ: <c:out value="${order.receiver.shippingAddress}"/></p></div>
<div class="table-wrap"><table><thead><tr><th>Sách</th><th>Đơn giá đã đặt</th><th>Số lượng</th><th>Thành tiền</th></tr></thead><tbody>
<c:forEach var="item" items="${order.items}"><tr><td><c:out value="${item.bookName}"/></td><td><c:out value="${item.unitPrice}"/></td><td>${item.quantity}</td><td><c:out value="${item.lineTotal}"/></td></tr></c:forEach>
</tbody></table></div>
<p>Tổng tiền: <strong class="order-total"><c:out value="${order.totalAmount}"/></strong></p>
<p><a href="${pageContext.request.contextPath}/products">Tiếp tục xem sách</a> · <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a></p>
</body></html>
