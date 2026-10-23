<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css?v=20261023-9">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tintuc.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/gioithieu.css?v=20261023-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/lienhe.css?v=20261023-7">
    <c:if test="${param.bodyClass == 'dashboard-page'}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css?v=20261023-7">
    </c:if>
    <title><c:out value="${param.pageTitle}" default="UCare"/></title>
    <c:if test="${param.clinical == 'true'}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/clinical.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-site')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-site')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient-footer.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-home')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient-home.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-auth')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient-auth.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-public')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient-public.css?v=20261023-7"></c:if>
    <c:if test="${fn:contains(param.bodyClass, 'patient-contact')}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/patient-contact.css?v=20261023-7"></c:if>
</head>
<body class="<c:out value='${param.bodyClass}'/>">
<header class="site-header">
    <a href="${pageContext.request.contextPath}/main" class="header-logo" aria-label="UCare - Trang chủ">
        <img class="ui-logo" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true"> UCare
    </a>

    <nav aria-label="Điều hướng chính">
        <ul class="header-menu">
            <li><a href="${pageContext.request.contextPath}/main">Trang chủ</a></li>
            <li><a href="${pageContext.request.contextPath}/chuyenkhoa">Chuyên khoa</a></li>
            <li><a href="${pageContext.request.contextPath}/bacsi">Bác sĩ</a></li>
            <li><a href="${pageContext.request.contextPath}/tintuc">Tin tức</a></li>
            <li><a href="${pageContext.request.contextPath}/main#huong-dan">Hướng dẫn</a></li>
        </ul>
    </nav>

    <c:choose>
        <c:when test="${not empty sessionScope.currentUser}">
            <div class="header-account">
            <details class="member-account">
                <summary class="member-account-toggle">
                    <span class="member-avatar" aria-hidden="true">
                        <c:out value="${fn:toUpperCase(fn:substring(sessionScope.fullName, 0, 1))}"/>
                    </span>
                    <span class="member-name"><c:out value="${sessionScope.currentUser.fullName}"/></span>
                    <span aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-down.png" alt="" aria-hidden="true"></span>
                </summary>

                <ul class="member-dropdown">
                    <c:choose>
                        <c:when test="${sessionScope.role == 'PATIENT'}">
                            <li><a href="${pageContext.request.contextPath}/patient/profile">Thông tin cá nhân</a></li>
                            <li><a href="${pageContext.request.contextPath}/patient/lich-kham-cua-toi">Lịch khám của tôi</a></li>
                            <li><a href="${pageContext.request.contextPath}/notifications">Thông báo <span data-notification-count aria-live="polite"></span></a></li>
                            <li><a href="${pageContext.request.contextPath}/patient/ho-so-benh-an">Hồ sơ bệnh án</a></li>
                            <li><a href="${pageContext.request.contextPath}/patient/ket-qua-can-lam-sang">Kết quả cận lâm sàng</a></li>
                            <li><a href="${pageContext.request.contextPath}/patient/don-thuoc">Đơn thuốc</a></li>
                            <li><a href="${pageContext.request.contextPath}/thanhtoan">Thanh toán</a></li>
                        </c:when>
                        <c:when test="${sessionScope.role == 'DOCTOR'}">
                            <li><a href="${pageContext.request.contextPath}/doctor/schedule">Trang bác sĩ</a></li>
                        </c:when>
                        <c:when test="${sessionScope.role == 'ADMIN'}">
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard">Trang quản trị</a></li>
                        </c:when>
                    </c:choose>
                    <li class="member-logout">
                        <form action="${pageContext.request.contextPath}/logout" method="post">
                            <button type="submit">Đăng xuất</button>
                        </form>
                    </li>
                </ul>
            </details>
            </div>
        </c:when>
        <c:otherwise>
            <div class="header-actions">
                <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                <a href="${pageContext.request.contextPath}/register" class="register-link">Đăng ký tài khoản</a>
            </div>
        </c:otherwise>
    </c:choose>
</header>
<c:if test="${sessionScope.role == 'PATIENT'}"><script src="${pageContext.request.contextPath}/js/notifications.js" data-notification-url="${pageContext.request.contextPath}/notifications?count=1" defer></script></c:if>
