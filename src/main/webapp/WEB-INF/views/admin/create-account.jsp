<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Tạo Tài Khoản</title>
</head>
<body>
    <div class="main-layout">
        <jsp:include page="/WEB-INF/includes/sidebar.jsp" />

        <div class="content-area">
            <h2>
                <c:choose>
                    <c:when test="${type == 'ADMIN'}">Tạo Tài Khoản Quản Trị Viên</c:when>
                    <c:when test="${type == 'DOCTOR'}">Tạo Tài Khoản Bác Sĩ</c:when>
                    <c:when test="${type == 'NURSE'}">Tạo Tài Khoản Điều Dưỡng</c:when>
                    <c:when test="${type == 'TECHNICIAN'}">Tạo Tài Khoản Chuyên Viên Kỹ Thuật</c:when>
                    <c:when test="${type == 'RECEPTIONIST'}">Tạo Tài Khoản Tiếp Tân</c:when>
                    <c:when test="${type == 'PHARMACIST'}">Tạo Tài Khoản Dược Sĩ</c:when>
                    <c:otherwise>Tạo Tài Khoản Nhân Viên</c:otherwise>
                </c:choose>
            </h2>
            <c:if test="${not empty message}">
                <p class="alert ${success ? 'alert-success' : 'alert-error'}"><c:out value="${message}"/></p>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/admin/create-account" class="admin-form">
                <input type="hidden" name="type" value="${type}">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">

                <div class="form-group">
                    <label for="username">Tên đăng nhập</label>
                    <input id="username" type="text" name="username" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="password">Mật khẩu</label>
                    <input id="password" type="password" name="password" minlength="6" maxlength="255" required>
                </div>
                <div class="form-group">
                    <label for="fullName">Họ và tên</label>
                    <input id="fullName" type="text" name="fullName" maxlength="100" required>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input id="email" type="email" name="email" maxlength="255">
                </div>
                <div class="form-group">
                    <label for="phone">Số điện thoại</label>
                    <input id="phone" type="text" name="phone" maxlength="15">
                </div>

                <c:if test="${type == 'DOCTOR' || type == 'TECHNICIAN'}">
                    <div class="form-group">
                        <label for="specialtyId">${type == 'TECHNICIAN' ? 'Khoa cận lâm sàng' : 'Chuyên khoa'}</label>
                        <select id="specialtyId" name="specialtyId" required>
                            <option value="">-- Chọn chuyên khoa --</option>
                            <c:forEach var="sp" items="${specialties}">
                                <option value="${sp.specialtyId}"><c:out value="${sp.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <c:if test="${empty specialties}">
                        <p>Chưa có chuyên khoa phù hợp.</p>
                    </c:if>
                </c:if>

                <c:if test="${type == 'NURSE'}">
                    <div class="form-group">
                        <label for="professionType">Nhóm nghề</label>
                        <select id="professionType" name="professionType" required>
                            <option value="NURSE">Điều dưỡng</option>
                            <option value="MIDWIFE">Hộ sinh</option>
                        </select>
                    </div>
                </c:if>

                <button type="submit" class="btn btn-primary">Tạo tài khoản</button>
            </form>
        </div>
    </div>
</body>
</html>
