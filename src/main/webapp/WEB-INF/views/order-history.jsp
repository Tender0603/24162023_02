<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title>Lịch sử đơn hàng</title></head><body>
<h1>Lịch sử đơn hàng</h1>
<nav class="pagination order-filters" aria-label="Lọc theo trạng thái đơn hàng">
<a href="${pageContext.request.contextPath}/orders" class="${empty selectedStatus ? 'active' : ''}" aria-current="${empty selectedStatus ? 'page' : 'false'}">Tất cả</a>
<c:forEach var="status" items="${statuses}">
<a href="${pageContext.request.contextPath}/orders?status=${status}" class="${selectedStatus == status ? 'active' : ''}" aria-current="${selectedStatus == status ? 'page' : 'false'}"><c:out value="${status.label}"/></a>
</c:forEach>
</nav>
<c:choose><c:when test="${empty orders}">
<p class="panel"><c:choose><c:when test="${empty selectedStatus}">Bạn chưa có đơn hàng nào.</c:when><c:otherwise>Không có đơn hàng ở trạng thái này.</c:otherwise></c:choose></p>
</c:when><c:otherwise>
<div class="table-wrap"><table><thead><tr><th>Mã đơn</th><th>Ngày đặt (UTC)</th><th>Người nhận</th><th>Tổng tiền</th><th>Phương thức</th><th>Thanh toán</th><th>Trạng thái</th><th>Chi tiết</th></tr></thead><tbody>
<c:forEach var="order" items="${orders}">
<tr data-order-id="${order.orderId}"><td>#${order.orderId}</td><td><c:out value="${order.createdAt.format(DateTimeFormatter.ofPattern('dd/MM/yyyy HH:mm:ss'))}"/></td><td><c:out value="${order.receiver.receiverName}"/></td><td><c:out value="${order.totalAmount}"/></td><td><c:out value="${order.paymentMethod}"/></td><td><c:out value="${order.paymentStatus}"/></td><td><span class="order-status status-${order.status}"><c:out value="${order.status.label}"/></span></td><td><a href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}">Xem chi tiết</a></td></tr>
</c:forEach>
</tbody></table></div>
</c:otherwise></c:choose>
</body></html>
