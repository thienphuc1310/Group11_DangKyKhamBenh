<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Hồ sơ bệnh án"/><jsp:param name="activeMenu" value="records"/></jsp:include>
<div class="patient-heading"><div><h1>Hồ sơ bệnh án</h1><p>Xem lại thông tin các lần khám của bạn.</p></div></div>
<form class="patient-card patient-filter" method="get" action="${ctx}/patient/ho-so-benh-an">
    <div class="patient-field"><label for="startDate">Từ ngày</label><input type="date" id="startDate" name="startDate" value="<c:out value='${startDate}'/>"></div>
    <div class="patient-field"><label for="endDate">Đến ngày</label><input type="date" id="endDate" name="endDate" value="<c:out value='${endDate}'/>"></div>
    <button class="patient-button patient-button-primary" type="submit">Lọc kết quả</button><a class="patient-button" href="${ctx}/patient/ho-so-benh-an">Xóa lọc</a>
</form>
<c:choose>
    <c:when test="${empty medicalRecords}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="folder"/></jsp:include><h2>Chưa có hồ sơ phù hợp</h2><p>Hồ sơ các lần khám đã hoàn tất sẽ hiển thị tại đây.</p></div></c:when>
    <c:otherwise>
        <div class="patient-record-layout">
            <nav class="patient-card patient-history" aria-label="Lịch sử khám">
                <h2>Lịch sử khám</h2>
                <c:forEach var="item" items="${medicalRecords}">
                    <c:url var="recordLink" value="/patient/ho-so-benh-an"><c:param name="recordId" value="${item.recordId}"/><c:param name="startDate" value="${startDate}"/><c:param name="endDate" value="${endDate}"/></c:url>
                    <a class="${item.recordId == selectedRecord.recordId ? 'active' : ''}" href="<c:out value='${recordLink}'/>">
                        <small><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${item.appointment.schedule.workDate}"/></jsp:include></small>
                        <strong><c:out value="${item.appointment.schedule.doctor.specialty.name}"/></strong><small><c:out value="${item.appointment.schedule.doctor.fullName}"/></small>
                    </a>
                </c:forEach>
            </nav>
            <c:if test="${not empty selectedRecord}">
                <section class="patient-card">
                    <div class="patient-card-header"><div><h2>Chi tiết lần khám</h2><p><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${selectedRecord.appointment.schedule.workDate}"/></jsp:include></p></div><span class="patient-badge patient-badge-green">Đã hoàn tất</span></div>
                    <div class="patient-summary">
                        <div><small>Bác sĩ điều trị</small><strong><c:out value="${selectedRecord.appointment.schedule.doctor.fullName}"/></strong></div>
                        <div><small>Chuyên khoa</small><strong><c:out value="${selectedRecord.appointment.schedule.doctor.specialty.name}"/></strong></div>
                        <div><small>Mã lịch hẹn</small><strong><c:out value="${selectedRecord.appointment.appointmentCode}"/></strong></div>
                        <div><small>Bệnh nhân</small><strong><c:out value="${selectedRecord.appointment.patient.fullName}"/></strong></div>
                    </div>
                    <div class="patient-detail"><h3>Lý do khám</h3><p class="patient-text"><c:out value="${selectedRecord.appointment.reason}" default="Không ghi lý do"/></p></div>
                    <div class="patient-detail"><h3>Chẩn đoán</h3><p class="patient-text"><c:out value="${selectedRecord.diagnosis}" default="Chưa cập nhật"/></p></div>
                    <div class="patient-detail"><h3>Ghi chú lâm sàng</h3><p class="patient-text"><c:out value="${selectedRecord.clinicalNote}" default="Không có"/></p></div>
                    <div class="patient-detail"><h3>Hướng điều trị</h3><p class="patient-text"><c:out value="${selectedRecord.treatment}" default="Không có"/></p></div>
                    <div class="patient-detail"><h3>Lời dặn</h3><p class="patient-text"><c:out value="${selectedRecord.advice}" default="Không có"/></p></div>
                    <c:if test="${empty selectedRecord.encounter}"><div class="patient-detail"><h3>Đơn thuốc cũ</h3><p class="patient-text"><c:out value="${selectedRecord.prescription}" default="Không kê đơn"/></p></div></c:if>
                    <c:if test="${not empty selectedRecord.encounter && empty prescription}"><p class="patient-muted">Không kê đơn thuốc.</p></c:if>
                    <jsp:include page="/WEB-INF/includes/clinical-assessment.jsp"/>
                    <jsp:include page="/WEB-INF/includes/clinical-orders.jsp"/>
                    <jsp:include page="/WEB-INF/includes/clinical-prescription.jsp"/>
                    <div class="patient-actions"><a class="patient-button" href="${ctx}/patient/don-thuoc">Xem đơn thuốc</a><a class="patient-button" href="${ctx}/patient/ket-qua-can-lam-sang">Xem kết quả cận lâm sàng</a></div>
                </section>
            </c:if>
        </div>
    </c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
