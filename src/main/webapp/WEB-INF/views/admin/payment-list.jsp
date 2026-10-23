<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Thanh Toán</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />
    <div class="content-area">
        <h2>DANH SÁCH THANH TOÁN</h2>

        <div class="admin-card">
        <table class="data-table">
            <thead>
                <tr><th>Mã GD</th>
                <th>Mã Lịch Khám</th>
                <th>Bệnh Nhân</th>
                <th>Số Tiền</th>
                <th>Phương Thức</th>
                <th>Trạng Thái</th>
                <th>Thời Gian Thanh Toán</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="pm" items="${payments}">
                <tr>
                    <td><c:out value="${pm.transactionCode}"/></td>
                    <td>
                        <a href="${ctx}/admin/appointments?id=${pm.appointment.appointmentId}">
                            <c:out value="${pm.appointment.appointmentCode}"/>
                        </a>
                    </td>
                    <td><c:out value="${pm.appointment.patient.fullName}"/></td>
                    <td><fmt:formatNumber value="${pm.amount}" type="number" groupingUsed="true"/> đ</td>
                    <td>
                        <c:choose>
                            <c:when test="${pm.method == 'CASH'}">Tiền mặt</c:when>
                            <c:when test="${pm.method == 'BANK_TRANSFER'}">Chuyển khoản</c:when>
                            <c:otherwise>${pm.method}</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${pm.status == 'PAID'}"><span class="badge badge-green">Đã thanh toán</span></c:when>
                            <c:when test="${pm.status == 'REFUNDED'}"><span class="badge badge-gray">Đã hoàn tiền</span></c:when>
                            <c:when test="${pm.status == 'FAILED'}"><span class="badge badge-red">Thất bại</span></c:when>
                            <c:otherwise><span class="badge badge-orange">Chờ thanh toán</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>${empty pm.paidAt ? '—' : fn:substring(fn:replace(pm.paidAt, 'T', ' '), 0, 16)}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty payments}">
                                                    <!--số cột -->
                <tr><td colspan="7" style="text-align:center;">Chưa có khoản thanh toán nào.</td></tr>
            </c:if>
            </tbody>
        </table>
        </div>
    </div>
</div>
</body>
</html>
