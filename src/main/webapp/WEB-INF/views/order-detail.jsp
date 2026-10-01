<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title>Chi tiết đơn hàng</title></head><body>
<h1>Đơn hàng #${order.orderId}</h1>
<p>Ngày đặt (UTC): <c:out value="${order.createdAt.format(DateTimeFormatter.ofPattern('dd/MM/yyyy HH:mm:ss'))}"/></p>
<p>Trạng thái: <span class="order-status status-${order.status}"><c:out value="${order.status.label}"/></span></p>
<p>Phương thức: <c:out value="${order.paymentMethod}"/> — thanh toán khi nhận hàng.</p>
<p>Thanh toán: <c:out value="${order.paymentStatus}"/></p>
<div class="panel"><p>Người nhận: <c:out value="${order.receiver.receiverName}"/></p>
<p>Điện thoại: <c:out value="${order.receiver.receiverPhone}"/></p><p>Địa chỉ: <c:out value="${order.receiver.shippingAddress}"/></p></div>
<div class="table-wrap"><table><thead><tr><th>Sách</th><th>Đơn giá đã đặt</th><th>Số lượng</th><th>Thành tiền</th></tr></thead><tbody>
<c:forEach var="item" items="${order.items}"><tr><td><c:out value="${item.bookName}"/></td><td><c:out value="${item.unitPrice}"/></td><td>${item.quantity}</td><td><c:out value="${item.lineTotal}"/></td></tr></c:forEach>
</tbody></table></div>
<p>Tổng tiền: <strong class="order-total"><c:out value="${order.totalAmount}"/></strong></p>
<p><a href="${pageContext.request.contextPath}/orders">Lịch sử đơn hàng</a></p>
</body></html>
