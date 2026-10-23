<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan quản trị - UCare</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />


    <main class="content-area">
        <header class="page-header">
            <div>
                <h1>Tổng quan quản trị</h1>
                <p>Chào mừng <c:out value="${sessionScope.currentUser.fullName}" default="Quản trị viên"/>.</p>
            </div>
        </header>

        <section class="stat-grid" aria-label="Các chức năng quản trị">
            <a class="stat-card" href="${pageContext.request.contextPath}/admin/users">
                <span class="stat-label">Hồ sơ</span>
                <strong class="stat-value">Bệnh nhân</strong>
                <span class="stat-note">Xem danh sách bệnh nhân</span>
            </a>
            <a class="stat-card" href="${pageContext.request.contextPath}/admin/doctors">
                <span class="stat-label">Nhân sự</span>
                <strong class="stat-value">Bác sĩ</strong>
                <span class="stat-note">Quản lý bác sĩ và chuyên khoa</span>
            </a>
            <c:if test="${sessionScope.role == 'ADMIN'}">
                <a class="stat-card" href="${pageContext.request.contextPath}/admin/admins">
                    <span class="stat-label">Nhân sự</span>
                    <strong class="stat-value">Nhân viên</strong>
                    <span class="stat-note">Quản lý nhân viên và quản trị viên</span>
                </a>
            </c:if>
            <a class="stat-card" href="${pageContext.request.contextPath}/admin/specialties">
                <span class="stat-label">Danh mục</span>
                <strong class="stat-value">Chuyên khoa</strong>
                <span class="stat-note">Xem và tìm kiếm chuyên khoa</span>
            </a>
            <a class="stat-card" href="${pageContext.request.contextPath}/admin/appointments">
                <span class="stat-label">Nghiệp vụ</span>
                <strong class="stat-value">Lịch khám</strong>
                <span class="stat-note">Theo dõi lịch hẹn khám</span>
            </a>
            <a class="stat-card highlight" href="${pageContext.request.contextPath}/admin/payments">
                <span class="stat-label">Tài chính</span>
                <strong class="stat-value">Thanh toán</strong>
                <span class="stat-note">Kiểm tra trạng thái thanh toán</span>
            </a>
        </section>
    </main>
</div>
</body>
</html>
