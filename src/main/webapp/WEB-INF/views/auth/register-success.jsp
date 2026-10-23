<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>

<%-- Header dùng chung với main.jsp --%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-auth"/><jsp:param name="pageTitle" value="Đăng ký thành công | UCare"/></jsp:include>

<main class="register-success-page">
    <section class="register-success-card" aria-labelledby="register-success-title">
        <img class="register-success-icon ui-success" src="${pageContext.request.contextPath}/images/icons/ui/success.png" alt="" aria-hidden="true">
        <h1 id="register-success-title">Đăng ký thành công</h1>
        <p>Tài khoản của bạn đã được tạo. Bạn có thể đăng nhập để sử dụng các dịch vụ của UCare.</p>
        <a class="register-success-button"
           href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
    </section>
</main>

<%-- Footer dùng chung với main.jsp --%>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
