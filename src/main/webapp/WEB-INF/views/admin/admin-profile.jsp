<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin cá nhân - UCare</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp">
        <jsp:param name="activeMenu" value="profile"/>
    </jsp:include>

    <main class="content-area admin-profile-page">
        <h2>THÔNG TIN CÁ NHÂN</h2>

        <c:if test="${updated}">
            <div class="alert alert-ok">Đã lưu thông tin thành công.</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-fail"><c:out value="${error}"/></div>
        </c:if>

        <section class="admin-card admin-profile-card">
            <div class="admin-profile-summary">
                <span class="admin-profile-avatar">
                    <c:choose>
                        <c:when test="${sessionScope.role == 'ADMIN'}">QT</c:when>
                        <c:otherwise>NV</c:otherwise>
                    </c:choose>
                </span>
                <div>
                    <h3><c:out value="${profile.user.fullName}"/></h3>
                    <p>
                        <c:choose>
                            <c:when test="${sessionScope.role == 'ADMIN'}">Tài khoản quản trị viên</c:when>
                            <c:otherwise>Tài khoản nhân viên</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </div>

            <form id="profile-form" method="post"
                  action="${pageContext.request.contextPath}/admin/profile"
                  data-editing="${not empty error}">
                <div class="admin-profile-grid">
                    <div class="form-group">
                        <label for="fullName">Họ và tên *</label>
                        <input id="fullName" name="fullName" maxlength="100"
                               value="<c:out value='${profile.user.fullName}'/>" required disabled>
                    </div>

                    <div class="form-group">
                        <label for="birthDate">Ngày sinh</label>
                        <input type="date" id="birthDate" name="birthDate"
                               value="<c:out value='${profile.user.dateOfBirth}'/>" disabled>
                    </div>

                    <div class="form-group">
                        <label for="gender">Giới tính</label>
                        <select id="gender" name="gender" disabled>
                            <option value="">Chọn giới tính</option>
                            <option value="Nam" ${profile.user.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                            <option value="Nữ" ${profile.user.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                            <option value="Khác" ${profile.user.gender == 'Khác' ? 'selected' : ''}>Khác</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="phone">Số điện thoại</label>
                        <input type="tel" id="phone" name="phone" maxlength="15"
                               value="<c:out value='${profile.user.phone}'/>" disabled>
                    </div>

                    <div class="form-group admin-profile-wide">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" maxlength="255"
                               value="<c:out value='${profile.user.email}'/>" disabled>
                    </div>

                    <div class="form-group admin-profile-wide">
                        <label for="address">Địa chỉ</label>
                        <input id="address" name="address" maxlength="255"
                               value="<c:out value='${profile.user.address}'/>" disabled>
                    </div>
                </div>

                <div class="admin-profile-actions profile-view-actions">
                    <button type="button" class="btn btn-primary" id="edit-profile">
                        Thay đổi thông tin
                    </button>
                </div>

                <div class="admin-profile-actions profile-edit-actions" hidden>
                    <button type="button" class="btn btn-outline" id="cancel-profile">
                        Huỷ thay đổi
                    </button>
                    <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                </div>
            </form>
        </section>
    </main>
</div>

<script src="${pageContext.request.contextPath}/js/profile.js?v=20261012-1"></script>
</body>
</html>
