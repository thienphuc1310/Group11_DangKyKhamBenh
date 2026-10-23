<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>


<jsp:include page="/WEB-INF/includes/header.jsp">
<jsp:param name="pageTitle" value="Đặt lịch khám - UCare"/>
<jsp:param name="bodyClass" value="patient-site patient-home"/></jsp:include>

<main class="patient-home-content">
    <div class="home-booking-layout">
        <section class="home-introduction">
            <div class="home-greeting">
            <c:choose><c:when test="${sessionScope.role == 'PATIENT'}">Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/></c:when><c:otherwise>Chào mừng bạn đến với UCare</c:otherwise></c:choose>
            <p>Hôm nay bạn cần chăm sóc sức khỏe như thế nào?</p></div>
            <h1>Đặt lịch khám,<br>nhẹ nhàng hơn.</h1>
            <p class="home-intro-description">Chọn chuyên khoa, bác sĩ và khung giờ phù hợp với bạn.</p>
            <a class="home-doctor-link" href="${ctx}/bacsi">Tìm hiểu đội ngũ bác sĩ <jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="arrow"/></jsp:include></a>
            <div class="home-hospital-info"><img class="home-brand-mark" src="${pageContext.request.contextPath}/images/icons/ui/hospital-mark.png" alt="" aria-hidden="true"><div><strong>Bệnh viện UCare</strong><p>215 Hồng Bàng, Phường Chợ Lớn, TP. Hồ Chí Minh</p></div></div>
        </section>

        <section class="patient-card home-booking-card" aria-labelledby="booking-title">
            <h2 id="booking-title">Đăng ký khám bệnh</h2>
            <ol class="home-booking-steps"><li class="active"><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-1.png" alt="1">Chọn lịch</li><li><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-2.png" alt="2">Đăng ký</li><li><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-3.png" alt="3">Thanh toán</li></ol>
            <c:if test="${param.loginSuccess == '1'}"><p class="patient-notice patient-notice-success" role="status">Đăng nhập thành công.</p></c:if>
            <c:if test="${param.bookingAuthenticated == '1'}"><p class="patient-notice">Vui lòng chọn lại thông tin lịch khám.</p></c:if>
            <c:if test="${not empty bookingError}"><p class="patient-notice patient-notice-error" role="alert"><c:out value="${bookingError}"/></p></c:if>
            <form id="booking-form" method="post" action="${ctx}/dangkykham" data-context-path="${ctx}">
                <div class="home-booking-fields">
                    <div class="patient-field"><label for="appointment-date">Ngày khám</label><input type="date" id="appointment-date" name="appointmentDate" required></div>
                    <div class="patient-field"><label for="chuyenkhoa">Chuyên khoa</label><select id="chuyenkhoa" name="chuyenkhoa" required><option value="">Chọn chuyên khoa</option><c:forEach var="specialty" items="${specialties}"><option value="${specialty.specialtyId}"><c:out value="${specialty.name}"/></option></c:forEach></select></div>
                    <div class="patient-field"><label for="doctor">Bác sĩ</label><select id="doctor" name="doctor" required disabled><option value="">Chọn ngày và chuyên khoa trước</option></select></div>
                </div>
                <fieldset class="home-time-slots"><legend>Khung giờ còn trống</legend><div id="time-slot-list"><p class="patient-muted">Chọn bác sĩ để xem khung giờ.</p></div></fieldset>
                <div class="patient-field"><label for="reason">Lý do khám</label><textarea id="reason" name="reason" rows="3" placeholder="Nhập triệu chứng hoặc vấn đề cần tư vấn"></textarea></div>
                <div class="home-booking-fee"><span>Phí khám</span><strong class="booking-fee"><c:choose><c:when test="${not empty bookingprice}"><fmt:formatNumber value="${bookingprice.amount}" pattern="#,##0"/> đ</c:when><c:otherwise>Chưa cập nhật</c:otherwise></c:choose></strong></div>
                <button class="patient-button patient-button-primary home-booking-submit" type="submit" ${empty bookingprice ? 'disabled' : ''}>Tiếp tục đăng ký <jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="arrow"/></jsp:include></button>
            </form>
        </section>

    </div>
    <nav class="home-quick-links" aria-label="Truy cập nhanh">
        <a href="${ctx}/patient/lich-kham-cua-toi"><span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="calendar"/></jsp:include></span>Lịch khám của tôi<b aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></b></a>
        <a href="${ctx}/patient/ket-qua-can-lam-sang"><span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="lab"/></jsp:include></span>Kết quả cận lâm sàng<b aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></b></a>
        <a href="${ctx}/patient/ho-so-benh-an"><span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="folder"/></jsp:include></span>Hồ sơ bệnh án<b aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></b></a>
        <a href="${ctx}/thanhtoan"><span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="card"/></jsp:include></span>Thanh toán<b aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></b></a>
    </nav>
    <section class="patient-card home-specialties"><div class="patient-card-header"><h2>Khám theo chuyên khoa</h2><a href="${ctx}/chuyenkhoa">Xem tất cả <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></a></div>
        <div class="home-specialty-list"><c:forEach var="specialty" items="${specialties}" end="5"><a href="${ctx}/bacsi?specialtyId=${specialty.specialtyId}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/plus.png" alt="" aria-hidden="true"><strong><c:out value="${specialty.name}"/></strong><span aria-hidden="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></span></a></c:forEach></div>
        <c:if test="${empty specialties}"><p class="patient-muted">Chưa có chuyên khoa nhận đặt khám.</p></c:if>
    </section>
    <section id="huong-dan" class="patient-card home-guide"><h2>Đặt lịch trong 3 bước</h2><ol><li><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-1.png" alt="1"><div><h3>Chọn lịch khám</h3><p>Chọn ngày, chuyên khoa và bác sĩ phù hợp.</p></div></li><li><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-2.png" alt="2"><div><h3>Đăng ký lịch hẹn</h3><p>Kiểm tra thông tin trước khi xác nhận.</p></div></li><li><img class="ui-step" src="${pageContext.request.contextPath}/images/icons/ui/step-3.png" alt="3"><div><h3>Thanh toán</h3><p>Xem lại lịch hẹn trong tài khoản của bạn.</p></div></li></ol></section>
</main>

<script src="${ctx}/js/booking.js"></script>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
