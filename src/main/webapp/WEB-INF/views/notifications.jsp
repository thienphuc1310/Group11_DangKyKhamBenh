<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:choose>
<c:when test="${sessionScope.role == 'PATIENT'}"><jsp:include page="/WEB-INF/views/patient/notifications.jsp"/></c:when>
<c:otherwise>
<c:choose>
    <c:when test="${sessionScope.role == 'DOCTOR'}"><jsp:include page="/WEB-INF/includes/header-doctor.jsp"><jsp:param name="pageTitle" value="Thông báo"/><jsp:param name="activeMenu" value="notifications"/></jsp:include></c:when>
    <c:otherwise><!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Thông báo - UCare</title><link rel="stylesheet" href="${ctx}/css/doctor.css?v=20261023-7"><link rel="stylesheet" href="${ctx}/css/clinical.css?v=20261023-7"></head><body>
        <c:if test="${sessionScope.role == 'ADMIN'}"><div class="main-layout"><jsp:include page="/WEB-INF/includes/sidebar.jsp"/><div class="content-area"></c:if>
    </c:otherwise>
</c:choose>
<main class="doctor-page clinical-workspace">
    <h1>Thông báo</h1>
    <c:if test="${sessionScope.role == 'PATIENT'}"><a class="back-link" href="${ctx}/main"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Trang chủ</a></c:if>
    <c:if test="${sessionScope.role == 'DOCTOR'}"><div class="form-actions"><a href="${ctx}/doctor/replacements">Nhận ca thay</a><a href="${ctx}/doctor/leave-requests">Đơn nghỉ của tôi</a></div></c:if>
    <c:if test="${sessionScope.role == 'ADMIN'}">
        <a class="btn btn-outline" href="${ctx}/admin/doctor-leaves">Xem tất cả đơn nghỉ</a>
        <c:if test="${param.reviewed == '1'}"><p class="success-message" role="status">Đã cập nhật đơn nghỉ.</p></c:if>
    </c:if>
    <c:if test="${empty notifications}"><p>Chưa có thông báo.</p></c:if>
    <c:forEach var="notification" items="${notifications}">
        <article class="doctor-card clinical-card-body">
            <p class="muted">${fn:substring(notification.createdAt, 0, 10)} ${fn:substring(notification.createdAt, 11, 16)} <c:if test="${!notification.read}">· Chưa đọc</c:if></p>
            <p><c:out value="${notification.message}"/></p>
            <c:if test="${sessionScope.role == 'ADMIN'}">
                <p>Trạng thái đơn: <jsp:include page="/WEB-INF/includes/leave-status.jsp"><jsp:param name="status" value="${notification.leaveRequest.status}"/></jsp:include></p>
                <c:if test="${notification.leaveRequest.status == 'PENDING_APPROVAL'}">
                    <form method="post" action="${ctx}/admin/doctor-leaves" class="clinical-actions">
                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                        <input type="hidden" name="leaveId" value="${notification.leaveRequest.leaveRequestId}">
                        <input type="hidden" name="returnTo" value="notifications">
                        <button type="submit" class="btn btn-primary save-button" name="action" value="approve">Duyệt</button>
                        <button type="submit" class="btn btn-outline" name="action" value="reject">Từ chối</button>
                    </form>
                </c:if>
            </c:if>
            <c:if test="${!notification.read}"><form method="post" action="${ctx}/notifications">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="notificationId" value="${notification.notificationId}">
                <button type="submit">Đánh dấu đã đọc</button>
            </form></c:if>
        </article>
    </c:forEach>
</main>
<c:choose>
    <c:when test="${sessionScope.role == 'DOCTOR'}"><jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/></c:when>
    <c:otherwise><c:if test="${sessionScope.role == 'ADMIN'}"></div></div></c:if></body></html></c:otherwise>
</c:choose>
</c:otherwise>
</c:choose>
