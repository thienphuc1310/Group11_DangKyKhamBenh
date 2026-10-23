<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-auth"/><jsp:param name="pageTitle" value="Đăng ký | UCare"/></jsp:include>

<main>
    <div class = "login-layout">
        <jsp:include page="/WEB-INF/includes/patient-auth-intro.jsp"/>

        <section class="register-card">
            <h2>Đăng ký tài khoản</h2>
            <p>Điền thông tin bên dưới để truy cập vào UCare.</p>

            <c:if test="${not empty registerError}">
                <p class="login-error"><c:out value="${registerError}"/></p>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label for="full-name">Họ và tên *</label>
                <input type="text" id="full-name" name="fullname"
                       value="<c:out value='${fullName}'/>" placeholder="Nguyễn Văn A" autocomplete="name" required>
            </div>

            <div class="form-group">
                <label for="email">Email *</label>
                <input type="email" id="email" name="email"
                       value="<c:out value='${email}'/>" placeholder="Email của bạn" autocomplete="email" required>
            </div>

            <div class="form-group">
                <label for="phone">Số điện thoại *</label>
                <input type="tel" id="phone" name="phone"
                       value="<c:out value='${phone}'/>" placeholder="0912345678" autocomplete="tel" required>
            </div>

            <div class="form-group">
                <label for="password">Mật khẩu *</label>
                <div class="password-field">
                    <input type="password" id="password" name="password" placeholder="Ít nhất 8 ký tự" minlength="8" autocomplete="new-password" required>
                    <button type="button" class="toggle-password" aria-controls="password" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>

            <div class="form-group">
            <label for="confirm-password">Nhập lại mật khẩu *</label>
                <div class="password-field">
                    <input type="password" id="confirm-password" name="confirm-password" placeholder="Nhập lại mật khẩu" minlength="8" autocomplete="new-password" required>
                    <button type="button" class="toggle-password" aria-controls="confirm-password" aria-label="Hiện mật khẩu nhập lại">Hiện</button>
                </div>
            </div>
            <button type="submit" class=login-button>Đăng ký</button>
            </form>

            <p>
            Đã có tài khoản
            <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            </p>
        </section>
    </div>
</main>

<%--footer--%>
<script src="${pageContext.request.contextPath}/js/patient-auth.js?v=20261019-1" defer></script>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>

