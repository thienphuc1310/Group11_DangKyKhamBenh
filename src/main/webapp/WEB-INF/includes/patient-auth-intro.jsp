<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<section class="login-intro">
    <span class="auth-eyebrow">CHĂM SÓC SỨC KHỎE CÙNG UCARE</span>
    <h1>Chủ động đặt khám.<br>An tâm mỗi ngày.</h1>
    <p>Đặt lịch và theo dõi thông tin khám bệnh của bạn trong cùng một tài khoản.</p>
    <ul class="intro-benefits">
        <li><span class="auth-benefit-icon"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="calendar"/></jsp:include></span><div><strong>Đặt lịch thuận tiện</strong><span>Chọn bác sĩ và khung giờ phù hợp.</span></div></li>
        <li><span class="auth-benefit-icon"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="folder"/></jsp:include></span><div><strong>Theo dõi hồ sơ khám</strong><span>Xem lại lịch sử và kết quả khám bệnh.</span></div></li>
        <li><span class="auth-benefit-icon"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="bell"/></jsp:include></span><div><strong>Cập nhật thông tin</strong><span>Theo dõi lịch hẹn và thông báo của bạn.</span></div></li>
    </ul>
    <a class="auth-home-link" href="${pageContext.request.contextPath}/main"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Về trang chủ</a>
</section>
