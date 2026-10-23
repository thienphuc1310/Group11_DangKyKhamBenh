<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Thông báo"/><jsp:param name="activeMenu" value="notifications"/></jsp:include>
<div class="patient-heading"><div><h1>Thông báo</h1><p>Cập nhật về lịch khám của bạn.</p></div></div>
<div data-patient-filter-area>
    <div class="patient-tabs"><button type="button" class="active" data-filter-value="all" aria-pressed="true">Tất cả</button><button type="button" data-filter-value="unread" aria-pressed="false">Chưa đọc</button></div>
    <c:forEach var="notification" items="${notifications}">
        <article class="patient-card patient-notification ${!notification.read ? 'unread' : ''}" data-patient-item data-status="${notification.read ? 'read' : 'unread'}">
            <span class="patient-notification-icon"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="bell"/></jsp:include></span>
            <div class="patient-notification-body">
                <time><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${notification.createdAt}"/><jsp:param name="time" value="true"/></jsp:include><c:if test="${!notification.read}"> · Chưa đọc</c:if></time>
                <p><c:out value="${notification.message}"/></p>
                <div class="patient-actions">
                    <a class="patient-button" href="${ctx}/patient/lich-kham-cua-toi">Xem lịch khám</a>
                    <c:if test="${!notification.read}"><form method="post" action="${ctx}/notifications">
                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="notificationId" value="${notification.notificationId}">
                        <button type="submit" class="patient-button">Đánh dấu đã đọc</button>
                    </form></c:if>
                </div>
            </div>
        </article>
    </c:forEach>
    <c:if test="${empty notifications}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="bell"/></jsp:include><h2>Chưa có thông báo</h2></div></c:if>
    <p class="patient-card patient-empty" data-filter-empty hidden>Không có thông báo chưa đọc.</p>
</div>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
