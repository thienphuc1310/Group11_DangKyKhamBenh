<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${param.status == 'PENDING_APPROVAL'}">Chờ admin duyệt</c:when>
    <c:when test="${param.status == 'WAITING_REPLACEMENT'}">Chờ bác sĩ nhận thay</c:when>
    <c:when test="${param.status == 'REPLACED'}">Đã có người thay</c:when>
    <c:when test="${param.status == 'REJECTED'}">Từ chối</c:when>
</c:choose>
