<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Đơn thuốc"/><jsp:param name="activeMenu" value="prescriptions"/></jsp:include>
<div class="patient-heading"><div><h1>Đơn thuốc của tôi</h1><p>Xem lại thuốc và hướng dẫn dùng thuốc từ bác sĩ.</p></div><a class="patient-button" href="${ctx}/thanhtoan">Xem thanh toán</a></div>
<c:forEach var="prescription" items="${prescriptions}">
    <section class="patient-card">
        <div class="patient-card-header"><div><h2>Đơn thuốc <c:out value="${prescription.prescriptionCode}"/></h2><p>Ngày kê: <jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${prescription.confirmedAt}"/></jsp:include></p></div><span class="patient-badge patient-badge-green">Đã xác nhận</span></div>
        <div class="patient-summary"><div><small>Bác sĩ kê đơn</small><strong><c:out value="${prescription.doctor.fullName}"/></strong></div><div><small>Mã lịch hẹn</small><strong><c:out value="${prescription.encounter.appointment.appointmentCode}"/></strong></div></div>
        <div class="patient-table-scroll"><table class="patient-table">
            <thead><tr><th>Thuốc</th><th>Số lượng</th><th>Liều dùng</th><th>Tần suất</th><th>Đường dùng</th><th>Số ngày</th><th>Hướng dẫn</th></tr></thead>
            <tbody><c:forEach var="line" items="${prescription.items}"><tr><td><strong><c:out value="${line.medicineNameSnapshot}"/></strong></td><td>${line.quantity} <c:out value="${line.unitSnapshot}"/></td><td><c:out value="${line.dosage}"/></td><td><c:out value="${line.frequency}"/></td><td><c:out value="${line.route}"/></td><td>${line.durationDays}</td><td><c:out value="${line.instruction}" default="Không có"/></td></tr></c:forEach></tbody>
        </table></div>
        <div class="patient-detail"><h3>Lời dặn của bác sĩ</h3><p class="patient-text"><c:out value="${prescription.advice}" default="Không có"/></p></div>
    </section>
</c:forEach>
<c:if test="${empty prescriptions}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="document"/></jsp:include><h2>Chưa có đơn thuốc</h2><p>Đơn thuốc đã được bác sĩ xác nhận sẽ hiển thị tại đây.</p></div></c:if>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
