<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.pageTitle} - UCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/doctor.css?v=20261023-9">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinical.css?v=20261023-7">
</head>
<body>
<div class="doctor-layout">

    <%--Menu bác sĩ--%>
    <aside class="doctor-sidebar">
        <div class="doctor-sidebar-brand">
            <a class="doctor-logo" href="${ctx}/doctor/schedule">
                <img class="ui-logo logo-icon" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true">
                <span class="doctor-brand-text">
                    <span class="doctor-brand-title">UCare</span>
                    <span class="doctor-brand-subtitle">Quản lý bác sĩ</span>
                </span>
            </a>
        </div>

        <nav aria-label="Điều hướng bác sĩ">
            <a class="${param.activeMenu == 'schedule' ? 'active' : ''}" href="${ctx}/doctor/schedule">Lịch làm việc</a>
            <a class="${param.activeMenu == 'appointments' ? 'active' : ''}" href="${ctx}/doctor/lich-hen">Lịch hẹn</a>
            <a class="${param.activeMenu == 'records' ? 'active' : ''}" href="${ctx}/doctor/ho-so-benh-nhan">Hồ sơ bệnh nhân</a>
            <a class="${param.activeMenu == 'leave' ? 'active' : ''}" href="${ctx}/doctor/leave-requests">Xin nghỉ theo ca</a>
            <a class="${param.activeMenu == 'replacements' ? 'active' : ''}" href="${ctx}/doctor/replacements">Nhận ca thay</a>
            <a class="${param.activeMenu == 'notifications' ? 'active' : ''}" href="${ctx}/notifications">Thông báo <span data-notification-count aria-live="polite"></span></a>
            <span class="doctor-sidebar-divider"></span>
            <a class="${param.activeMenu == 'profile' ? 'active' : ''}" href="${ctx}/doctor/profile">Thông tin cá nhân</a>
        </nav>

        <div class="doctor-sidebar-footer">
            <c:if test="${not empty sessionScope.currentUser}">
                <div class="doctor-sidebar-user">
                    <span class="doctor-user-avatar">
                        <c:out value="${fn:toUpperCase(fn:substring(sessionScope.currentUser.fullName, 0, 1))}"/>
                    </span>
                    <span class="doctor-user-text">
                        <span class="doctor-user-name"><c:out value="${sessionScope.currentUser.fullName}"/></span>
                        <span class="doctor-user-role">BÁC SĨ</span>
                    </span>
                </div>
            </c:if>
            <a class="doctor-logout" href="${ctx}/logout">Đăng xuất</a>
        </div>
    </aside>
    <div class="doctor-content">
<script src="${ctx}/js/notifications.js" data-notification-url="${ctx}/notifications?count=1" defer></script>
