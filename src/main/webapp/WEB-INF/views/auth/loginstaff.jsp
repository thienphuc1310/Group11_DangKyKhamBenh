<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập nội bộ - UCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=20261023-7">
</head>
<body>
<main class="login-layout">
    <section class="login-intro">
    </section>
    <section class="login-card">
        <h2>Đăng nhập nội bộ</h2>
        <p>Dành cho bác sĩ, kỹ thuật viên, dược sĩ và nhân viên quản lý.</p>

        <c:if test="${not empty requestScope.loginError}">
            <p class="auth-error"><c:out value="${requestScope.loginError}"/></p>
        </c:if>

        <form action="${pageContext.request.contextPath}/staff-login" method="post">
            <div class="form-group">
                <label for="admin-username">Tài khoản</label>
                <input id="admin-username" name="username" autocomplete="username"
                       value="<c:out value='${requestScope.username}'/>" required>
            </div>
            <div class="form-group">
                <label for="admin-password">Mật khẩu</label>
                <input id="admin-password" type="password" name="password" autocomplete="current-password" required>
            </div>
            <button type="submit" class="login-button">Đăng nhập</button>
        </form>
        <p><a href="${pageContext.request.contextPath}/main">Về trang chủ</a></p>
    </section>
</main>
</body>
</html>
