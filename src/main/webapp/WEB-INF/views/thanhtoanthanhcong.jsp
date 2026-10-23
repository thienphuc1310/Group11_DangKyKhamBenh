<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>

<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Thanh toán thành công"/>
<jsp:param name="activeMenu" value="payments"/></jsp:include>

<section class="patient-card patient-success">
    <img class="patient-success-mark ui-success" src="${pageContext.request.contextPath}/images/icons/ui/success.png" alt="" aria-hidden="true">
    <h1>Thanh toán thành công</h1>
    <p>Giao dịch đã được ghi nhận. Bạn có thể xem lại trong lịch sử thanh toán.</p>
    <div class="patient-actions"><a class="patient-button" href="${pageContext.request.contextPath}/thanhtoan">Lịch sử thanh toán</a>
    <a class="patient-button patient-button-primary" href="${pageContext.request.contextPath}/patient/lich-kham-cua-toi">Lịch khám của tôi</a></div>
</section>

<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
