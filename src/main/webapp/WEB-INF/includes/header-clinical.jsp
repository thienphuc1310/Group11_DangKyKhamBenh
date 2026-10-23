<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staffName" value="${not empty sessionScope.currentUser ? sessionScope.currentUser.fullName : sessionScope.fullName}"/>
<c:choose>
    <c:when test="${sessionScope.role == 'NURSE'}">
        <c:set var="areaTitle" value="Quản lý điều dưỡng"/>
        <c:set var="staffRoleLabel" value="ĐIỀU DƯỠNG"/>
        <c:set var="areaHome" value="/nurse/assessment"/>
    </c:when>
    <c:when test="${sessionScope.role == 'STAFF' && sessionScope.staffJobType == 'RECEPTIONIST'}">
        <c:set var="areaTitle" value="Quản lý tiếp tân"/>
        <c:set var="staffRoleLabel" value="TIẾP TÂN"/>
        <c:set var="areaHome" value="/staff/reception"/>
    </c:when>
    <c:when test="${sessionScope.role == 'PHARMACIST'}">
        <c:set var="areaTitle" value="Quản lý thuốc"/>
        <c:set var="staffRoleLabel" value="DƯỢC SĨ"/>
        <c:set var="areaHome" value="/pharmacy/medicines"/>
    </c:when>
    <c:otherwise>
        <c:set var="areaTitle" value="Cận lâm sàng"/>
        <c:set var="staffRoleLabel" value="${sessionScope.role == 'DOCTOR' ? 'BÁC SĨ' : 'CHUYÊN VIÊN KỸ THUẬT'}"/>
        <c:set var="areaHome" value="/diagnostics/worklist"/>
    </c:otherwise>
</c:choose>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${param.pageTitle}"/> - UCare</title>
    <link rel="stylesheet" href="${ctx}/css/doctor.css?v=20261023-9">
    <link rel="stylesheet" href="${ctx}/css/clinical.css?v=20261023-7">
    <c:if test="${sessionScope.role == 'PHARMACIST'}">
        <link rel="stylesheet" href="${ctx}/css/pharmacy.css?v=20261023-7">
    </c:if>
</head>
<body>
<div class="doctor-layout">
    <%-- Cùng bố cục sidebar với header-doctor.jsp, menu thay theo vai trò. --%>
    <aside class="doctor-sidebar clinical-sidebar">
        <div class="doctor-sidebar-brand">
            <a class="doctor-logo" href="${ctx}${areaHome}">
                <img class="ui-logo logo-icon" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true">
                <span class="doctor-brand-text">
                    <span class="doctor-brand-title">UCare</span>
                    <span class="doctor-brand-subtitle"><c:out value="${areaTitle}"/></span>
                </span>
            </a>
        </div>
        <nav aria-label="Điều hướng nhân sự">
            <c:choose>
                <c:when test="${sessionScope.role == 'PHARMACIST'}">
                    <a class="${param.activePage == 'medicines' ? 'active' : ''}" href="${ctx}/pharmacy/medicines">Quản lý thuốc</a>
                    <a class="${param.activePage != 'medicines' ? 'active' : ''}" href="${ctx}/pharmacy/prescriptions">Đơn chờ cấp thuốc</a>
                </c:when>
                <c:when test="${sessionScope.role == 'NURSE'}">
                    <a class="${param.activeMenu == 'assessment' ? 'active' : ''}" href="${ctx}/nurse/assessment">Đánh giá ban đầu</a>
                    <a class="${param.activeMenu == 'schedule' ? 'active' : ''}" href="${ctx}/nurse/schedule">Lịch làm việc</a>
                </c:when>
                <c:when test="${sessionScope.role == 'STAFF' && sessionScope.staffJobType == 'RECEPTIONIST'}">
                    <a class="${param.activeMenu == 'reception' ? 'active' : ''}" href="${ctx}/staff/reception">Tiếp nhận bệnh nhân</a>
                    <a class="${param.activeMenu == 'schedule' ? 'active' : ''}" href="${ctx}/staff/schedule">Lịch làm việc</a>
                </c:when>
                <c:otherwise>
                    <a class="${param.activeMenu != 'schedule' ? 'active' : ''}" href="${ctx}/diagnostics/worklist">Danh sách chỉ định</a>
                </c:otherwise>
            </c:choose>
            <c:if test="${sessionScope.role == 'TECHNICIAN'}">
                <a class="${param.activeMenu == 'schedule' ? 'active' : ''}" href="${ctx}/technician/schedule">Lịch làm việc</a>
            </c:if>
            <c:if test="${sessionScope.role == 'DOCTOR'}">
                <span class="doctor-sidebar-divider"></span>
                <a href="${ctx}/doctor/schedule">Trang bác sĩ</a>
            </c:if>
        </nav>
        <div class="doctor-sidebar-footer">
            <c:if test="${not empty staffName}">
                <div class="doctor-sidebar-user">
                    <span class="doctor-user-avatar"><c:out value="${fn:toUpperCase(fn:substring(staffName, 0, 1))}"/></span>
                    <span class="doctor-user-text">
                        <span class="doctor-user-name"><c:out value="${staffName}"/></span>
                        <span class="doctor-user-role"><c:out value="${staffRoleLabel}"/></span>
                    </span>
                </div>
            </c:if>
            <form method="post" action="${ctx}/logout">
                <button class="doctor-logout" type="submit">Đăng xuất</button>
            </form>
        </div>
    </aside>
    <div class="doctor-content">
