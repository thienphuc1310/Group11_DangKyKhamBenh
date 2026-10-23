<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Lịch khám của tôi"/><jsp:param name="activeMenu" value="appointments"/></jsp:include>
<div class="patient-heading"><div><h1>Lịch khám của tôi</h1><p>Theo dõi lịch hẹn và quá trình khám bệnh.</p></div><a class="patient-button patient-button-primary" href="${ctx}/main">+ Đặt lịch mới</a></div>
<c:if test="${param.booked == '1'}"><p class="patient-notice patient-notice-success" role="status">Đăng ký lịch khám thành công.</p></c:if>
<c:if test="${not empty errorMessage}"><p class="patient-notice patient-notice-error" role="alert"><c:out value="${errorMessage}"/></p></c:if>
<c:if test="${not empty successMessage}"><p class="patient-notice patient-notice-success" role="status"><c:out value="${successMessage}"/></p></c:if>
<div data-patient-filter-area>
    <div class="patient-tabs" aria-label="Lọc trạng thái lịch khám">
        <button type="button" class="active" data-filter-value="all" aria-pressed="true">Tất cả</button>
        <button type="button" data-filter-value="PENDING,CONFIRMED" aria-pressed="false">Chờ / Đang khám</button>
        <button type="button" data-filter-value="COMPLETED" aria-pressed="false">Đã khám</button>
        <button type="button" data-filter-value="CANCELLED" aria-pressed="false">Đã hủy</button>
    </div>
    <div class="patient-card patient-filter">
        <div class="patient-field"><label for="appointment-search">Tìm lịch hẹn</label><input type="search" id="appointment-search" data-filter-search placeholder="Mã lịch hẹn, bác sĩ, chuyên khoa..."></div>
        <div class="patient-field"><label for="appointment-filter-date">Ngày khám</label><input type="date" id="appointment-filter-date" data-filter-date></div>
        <button type="button" class="patient-button" data-filter-clear>Xóa lọc</button>
    </div>
    <c:forEach var="a" items="${appointments}">
        <c:set var="progress" value="${progressByAppointment[a.appointmentId]}"/>
        <article class="patient-card patient-appointment" data-patient-item data-status="${a.status}" data-date="${a.schedule.workDate}">
            <div class="patient-date"><strong>${fn:substring(a.schedule.workDate, 8, 10)}</strong><span>Tháng ${fn:substring(a.schedule.workDate, 5, 7)}</span></div>
            <div>
                <span class="patient-muted"><c:out value="${a.appointmentCode}"/> · ${fn:substring(a.schedule.workDate, 0, 4)}</span>
                <h2>Khám <c:out value="${a.schedule.doctor.specialty.name}"/></h2>
                <p><c:out value="${a.schedule.doctor.fullName}"/></p>
                <div class="patient-appointment-meta">
                    <span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="clock"/></jsp:include>${not empty a.slot ? a.slot.startTime : a.schedule.startTime} – ${not empty a.slot ? a.slot.endTime : a.schedule.endTime}</span>
                    <span><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="location"/></jsp:include>Phòng <c:out value="${a.schedule.room}"/></span>
                </div>
                <c:if test="${a.schedule.status == 'ON_LEAVE' && (a.status == 'PENDING' || a.status == 'CONFIRMED')}"><p class="patient-muted">Chờ bố trí bác sĩ thay thế</p></c:if>
            </div>
            <div class="patient-appointment-state"><c:choose>
                <c:when test="${a.status == 'PENDING'}"><span class="patient-badge patient-badge-amber">Chờ xác nhận</span></c:when>
                <c:when test="${a.status == 'CONFIRMED'}"><span class="patient-badge patient-badge-green">Đã xác nhận</span></c:when>
                <c:when test="${a.status == 'COMPLETED'}"><span class="patient-badge patient-badge-green">Đã khám</span></c:when>
                <c:otherwise><span class="patient-badge patient-badge-red">Đã hủy</span></c:otherwise>
            </c:choose></div>
            <div class="patient-appointment-footer">
                <div class="patient-queue">
                    <c:if test="${not empty progress.queueNumber}">Số khám: <strong><c:out value="${progress.queueNumber}"/></strong> · </c:if>
                    <c:choose><c:when test="${a.status == 'CANCELLED'}">Đã hủy</c:when><c:when test="${a.status == 'COMPLETED'}">Đã khám</c:when><c:otherwise><jsp:include page="/WEB-INF/includes/patient-encounter-status.jsp"><jsp:param name="status" value="${progress.encounterStatus}"/></jsp:include></c:otherwise></c:choose>
                </div>
                <div class="patient-actions">
                    <c:if test="${a.status == 'COMPLETED'}"><a class="patient-button" href="${ctx}/patient/ho-so-benh-an">Xem hồ sơ bệnh án</a></c:if>
                    <c:if test="${a.status == 'PENDING'}"><a class="patient-button patient-button-primary" href="${ctx}/thanhtoan?appointmentId=${a.appointmentId}">Thanh toán</a></c:if>
                    <c:if test="${a.status != 'CANCELLED' && a.status != 'COMPLETED' && empty progress.encounterStatus}">
                        <form action="${ctx}/lichkhamcuatoi" method="post" onsubmit="return confirm('Bạn có chắc chắn muốn hủy lịch? Chỉ được hủy trước 0h ngày khám.');">
                            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="action" value="cancel"><input type="hidden" name="appointmentId" value="${a.appointmentId}">
                            <button type="submit" class="patient-button patient-button-danger">Hủy lịch</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </article>
    </c:forEach>
    <c:if test="${empty appointments}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="calendar"/></jsp:include><h2>Chưa có lịch khám</h2><p>Chọn bác sĩ và thời gian phù hợp để đặt lịch.</p><a class="patient-button patient-button-primary" href="${ctx}/main">Đặt lịch khám</a></div></c:if>
    <p class="patient-card patient-empty" data-filter-empty hidden>Không có lịch khám phù hợp.</p>
    <c:if test="${not empty appointments}"><p class="patient-muted">Hiển thị <span data-filter-count>${appointments.size()}</span> lịch khám</p></c:if>
</div>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
