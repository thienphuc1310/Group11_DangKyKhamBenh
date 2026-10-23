<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${param.status == 'PAID'}"><span class="patient-badge patient-badge-green">Đã thanh toán</span></c:when>
    <c:when test="${param.status == 'PENDING'}"><span class="patient-badge patient-badge-amber">Chờ thanh toán</span></c:when>
    <c:when test="${param.status == 'FAILED'}"><span class="patient-badge patient-badge-red">Thất bại</span></c:when>
    <c:when test="${param.status == 'REFUNDED'}"><span class="patient-badge">Đã hoàn tiền</span></c:when>
</c:choose>
