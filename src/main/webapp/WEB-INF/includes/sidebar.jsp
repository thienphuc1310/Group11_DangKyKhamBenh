<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="currentUri" value="${pageContext.request.requestURI}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!--Các file css thuộc admin & staff-->
<link rel="stylesheet" href="${ctx}/css/admin/admin.css?v=20261023-7">


<aside class="sidebar">
    <div class="sidebar-brand">
        <a class="sidebar-brand-link" href="${ctx}/admin/dashboard">
            <img class="ui-logo sidebar-logo" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true">
            <span class="sidebar-brand-text">
                <span class="sidebar-brand-title">UCare</span>
                <span class="sidebar-brand-subtitle">Quản trị hệ thống</span>
            </span>
        </a>
    </div>

    <nav class="sidebar-nav" aria-label="Điều hướng quản trị">
        <ul>
            <li><a class="${fn:contains(currentUri, '/admin/dashboard') ? 'active' : ''}"
                   href="${ctx}/admin/dashboard">Tổng quan</a></li>
            <li><a class="${fn:contains(currentUri, '/admin/users') ? 'active' : ''}"
                   href="${ctx}/admin/users">Bệnh nhân</a></li>
            <li><a class="${fn:contains(currentUri, '/admin/doctors') ? 'active' : ''}"
                   href="${ctx}/admin/doctors">Bác sĩ</a></li>
            <c:if test="${sessionScope.role == 'ADMIN'}">
                <li><a class="${fn:contains(currentUri, '/admin/nurses') || param.activeMenu == 'nurses' ? 'active' : ''}"
                       href="${ctx}/admin/nurses">Điều dưỡng</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/technicians') || param.activeMenu == 'technicians' ? 'active' : ''}"
                       href="${ctx}/admin/technicians">Chuyên viên</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/admins') || param.activeMenu == 'staff' ? 'active' : ''}"
                       href="${ctx}/admin/admins">Nhân viên</a></li>
            </c:if>
            <li><a class="${fn:contains(currentUri, '/admin/specialties') ? 'active' : ''}"
                   href="${ctx}/admin/specialties">Chuyên khoa</a></li>
            <li><a class="${fn:contains(currentUri, '/admin/appointments') ? 'active' : ''}"
                   href="${ctx}/admin/appointments">Lịch khám</a></li>
            <li><a class="${fn:contains(currentUri, '/admin/payments') ? 'active' : ''}"
                   href="${ctx}/admin/payments">Thanh toán</a></li>

            <c:if test="${sessionScope.role == 'ADMIN'}">

                <li class="sidebar-divider"></li>

                <li class="sidebar-group-title">Quản lý lịch làm việc</li>
                <li><a class="${param.activeMenu == 'doctor-leaves' ? 'active' : ''}" href="${ctx}/admin/doctor-leaves">Duyệt nghỉ bác sĩ</a></li>
                <li><a href="${ctx}/notifications">Thông báo <span data-notification-count aria-live="polite"></span></a></li>
                <li><a class="${fn:contains(currentUri, '/admin/doctor-schedules') ? 'active' : ''}"
                       href="${ctx}/admin/doctor-schedules">Lịch làm việc bác sĩ</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/nurse-schedules') || requestScope.scheduleRole == 'NURSE' ? 'active' : ''}"
                       href="${ctx}/admin/nurse-schedules">Lịch làm việc điều dưỡng</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/technician-schedules') || requestScope.scheduleRole == 'TECHNICIAN' ? 'active' : ''}"
                       href="${ctx}/admin/technician-schedules">Lịch làm việc chuyên viên</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/receptionist-schedules') || requestScope.scheduleRole == 'STAFF' ? 'active' : ''}"
                       href="${ctx}/admin/receptionist-schedules">Lịch làm việc tiếp tân</a></li>

                <li class="sidebar-divider"></li>

                <li class="sidebar-group-title">Quản lý tài chính</li>
                <li><a class="${fn:contains(currentUri, '/admin/price') ? 'active' : ''}"
                       href="${ctx}/admin/price">Bảng giá dịch vụ</a></li>

                <li class="sidebar-divider"></li>

                <li class="sidebar-group-title">Danh mục cận lâm sàng</li>
                <li><a class="${fn:contains(currentUri, '/admin/diagnostic-services') || fn:contains(currentUri, 'diagnostic-service-list.jsp') ? 'active' : ''}"
                       href="${ctx}/admin/diagnostic-services">Dịch vụ cận lâm sàng</a></li>

                <li class="sidebar-divider"></li>

                <li class="sidebar-group-title">Quản lý tài khoản</li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'ADMIN' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=ADMIN">Tạo quản trị viên</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'STAFF' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=STAFF">Tạo nhân viên</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'RECEPTIONIST' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=RECEPTIONIST">Tạo tiếp tân</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'DOCTOR' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=DOCTOR">Tạo bác sĩ</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'NURSE' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=NURSE">Tạo điều dưỡng</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'TECHNICIAN' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=TECHNICIAN">Tạo chuyên viên kỹ thuật</a></li>
                <li><a class="${fn:contains(currentUri, '/admin/create-account') && param.type == 'PHARMACIST' ? 'active' : ''}"
                       href="${ctx}/admin/create-account?type=PHARMACIST">Tạo dược sĩ</a></li>
            </c:if>

                <li class="sidebar-divider"></li>
                <li><a class="${param.activeMenu == 'profile' ? 'active' : ''}"
                       href="${ctx}/admin/profile">Thông tin cá nhân</a></li>
        </ul>
    </nav>

    <div class="sidebar-footer">
        <c:if test="${not empty sessionScope.currentUser}">
            <div class="sidebar-user">
                <span class="sidebar-user-avatar"><c:out value="${fn:toUpperCase(fn:substring(sessionScope.currentUser.fullName, 0, 1))}"/></span>
                <span class="sidebar-user-text">
                    <span class="sidebar-user-name"><c:out value="${sessionScope.currentUser.fullName}"/></span>
                    <span class="sidebar-user-role"><c:out value="${sessionScope.role}"/></span>
                </span>
            </div>
        </c:if>
        <a class="sidebar-logout" href="${ctx}/logout">Đăng xuất</a>
    </div>
</aside>
<c:if test="${sessionScope.role == 'ADMIN'}"><script src="${ctx}/js/notifications.js" data-notification-url="${ctx}/notifications?count=1" defer></script></c:if>
