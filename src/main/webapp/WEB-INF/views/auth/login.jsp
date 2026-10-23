<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-auth"/><jsp:param name="pageTitle" value="Đăng nhập | UCare"/></jsp:include>

<main>
    <div class = "login-layout">
        <jsp:include page="/WEB-INF/includes/patient-auth-intro.jsp"/>

        <section class="login-card">
            <h2>Đăng nhập</h2>
            <p>Chào mừng bạn trở lại với UCare.</p>

            <c:if test="${not empty loginError}">
                <p class="login-error"><c:out value="${loginError}"/></p>
            </c:if>
            <c:if test="${not empty successMessage}">
                <p class="profile-success"><c:out value="${successMessage}"/></p>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label for="username">Email hoặc tên đăng nhập: *</label>
                <input type="text" id="username" name="username"
                       value="<c:out value='${username}'/>"
                       placeholder="Email hoặc tên đăng nhập" autocomplete="username" required>
            </div>

            <div class="form-group">
                <label for="password">Mật khẩu *</label>
                <div class="password-field">
                    <input type="password" id="password" name="password" placeholder="Nhập mật khẩu" autocomplete="current-password" required>
                    <button type="button" class="toggle-password" aria-controls="password" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>
            <button type="submit" class=login-button>Đăng nhập</button>
            </form>

            <p>
            Chưa có tài khoản?
            <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
            </p>

            <p>
            <a href="${pageContext.request.contextPath}/main">Về trang chủ</a>
            </p>
        </section>
    </div>
</main>

<%--footer--%>
<script src="${pageContext.request.contextPath}/js/patient-auth.js?v=20261019-1" defer></script>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>

