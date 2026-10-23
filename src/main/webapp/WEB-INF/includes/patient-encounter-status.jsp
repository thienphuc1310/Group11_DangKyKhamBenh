<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${param.status == 'WAITING_ASSESSMENT'}">Chờ điều dưỡng đánh giá</c:when>
    <c:when test="${param.status == 'ASSESSING'}">Đang đánh giá ban đầu</c:when>
    <c:when test="${param.status == 'WAITING_DOCTOR'}">Chờ bác sĩ</c:when>
    <c:when test="${param.status == 'IN_CONSULTATION'}">Đang khám</c:when>
    <c:when test="${param.status == 'WAITING_RESULTS'}">Chờ kết quả cận lâm sàng</c:when>
    <c:when test="${param.status == 'READY_FOR_REVIEW'}">Đã có kết quả, chờ khám tiếp</c:when>
    <c:when test="${param.status == 'WAITING_CHECKOUT'}">Chờ thanh toán / cấp thuốc</c:when>
    <c:when test="${param.status == 'COMPLETED'}">Hoàn tất lượt khám</c:when>
    <c:when test="${param.status == 'CANCELLED'}">Lượt khám đã hủy</c:when>
    <c:otherwise>Chưa tiếp nhận</c:otherwise>
</c:choose>
