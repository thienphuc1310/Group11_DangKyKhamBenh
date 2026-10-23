<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Chi tiết buổi khám"/>
    <jsp:param name="activeMenu" value="appointments"/>
</jsp:include>

<main class="doctor-page clinical-workspace">
    <a class="back-link" href="${ctx}/doctor/lich-hen?workDate=${appointment.schedule.workDate}">
        <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại danh sách lịch hẹn
    </a>

    <div class="page-heading">
        <div><h1>Chi tiết buổi khám</h1></div>
        <div class="appointment-info">
            <small>Giờ hẹn</small>
            <c:choose>
                <c:when test="${not empty appointment.slot}">
                    <span><c:out value="${appointment.slot.startTime}"/> – <c:out value="${appointment.slot.endTime}"/></span>
                </c:when>
                <c:otherwise>
                    <span><c:out value="${appointment.schedule.startTime}"/> – <c:out value="${appointment.schedule.endTime}"/></span>
                </c:otherwise>
            </c:choose>
            <span class="status">
                <c:choose>
                    <c:when test="${encounter.status == 'WAITING_DOCTOR'}">Chờ bác sĩ</c:when>
                    <c:when test="${encounter.status == 'IN_CONSULTATION'}">Đang khám</c:when>
                    <c:when test="${encounter.status == 'WAITING_RESULTS'}">Chờ kết quả cận lâm sàng</c:when>
                    <c:when test="${encounter.status == 'READY_FOR_REVIEW'}">Đủ kết quả, chờ khám tiếp</c:when>
                    <c:when test="${encounter.status == 'WAITING_CHECKOUT'}">Đã kết luận, chờ thanh toán/cấp thuốc</c:when>
                    <c:when test="${encounter.status == 'COMPLETED'}">Đã hoàn tất lượt khám</c:when>
                    <c:otherwise><c:out value="${encounter.status}"/></c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <section class="doctor-card patient-summary" aria-label="Thông tin bệnh nhân">
        <span class="patient-avatar" aria-hidden="true">BN</span>
        <div>
            <h2><c:out value="${appointment.patient.user.fullName}"/></h2>
            <p><c:out value="${appointment.patient.patientCode}"/></p>
            <span><c:out value="${appointment.patient.user.phone}"/></span>
        </div>
        <div><small>Ngày sinh</small><span><c:out value="${appointment.patient.user.dateOfBirth}" default="Chưa cập nhật"/></span></div>
        <div><small>Giới tính</small><span><c:out value="${appointment.patient.user.gender}" default="Chưa cập nhật"/></span></div>
        <div><small>Địa chỉ</small><span><c:out value="${appointment.patient.user.address}" default="Chưa cập nhật"/></span></div>
    </section>

    <section class="doctor-card patient-history" aria-label="Thông tin lịch hẹn">
        <h2>Thông tin lịch hẹn</h2>
        <p><strong>Mã lịch hẹn:</strong> <c:out value="${appointment.appointmentCode}"/></p>
        <p><strong>Ngày khám:</strong> <c:out value="${appointment.schedule.workDate}"/></p>
        <p><strong>Lý do khám:</strong>
            <c:choose>
                <c:when test="${not empty appointment.reason}"><c:out value="${appointment.reason}"/></c:when>
                <c:otherwise>Không ghi lý do</c:otherwise>
            </c:choose>
        </p>
    </section>

    <c:if test="${not empty examError}">
        <div class="form-error" role="alert"><c:out value="${examError}"/></div>
    </c:if>

    <section class="doctor-card clinical-card-body">
        <h2>Đánh giá ban đầu của điều dưỡng</h2>
        <div class="clinical-assessment-grid">
            <div class="clinical-wide"><small>Triệu chứng / lý do khám</small><p class="clinical-text"><c:out value="${assessment.chiefComplaint}" default="Chưa cập nhật"/></p></div>
            <div><small>Nhiệt độ</small><p><c:out value="${assessment.temperature}" default="—"/> °C</p></div>
            <div><small>Huyết áp</small><p><c:out value="${assessment.systolicPressure}" default="—"/> / <c:out value="${assessment.diastolicPressure}" default="—"/> mmHg</p></div>
            <div><small>Nhịp tim</small><p><c:out value="${assessment.heartRate}" default="—"/> lần/phút</p></div>
            <div class="clinical-wide"><small>Ghi chú điều dưỡng</small><p class="clinical-text"><c:out value="${assessment.note}" default="Không có ghi chú"/></p></div>
        </div>
    </section>

    <c:if test="${encounter.status == 'WAITING_DOCTOR' || encounter.status == 'READY_FOR_REVIEW'}">
        <form method="post" action="${pageContext.request.contextPath}/doctor/chi-tiet-buoi-kham" class="clinical-actions">
            <input type="hidden" name="encounterId" value="${encounter.encounterId}">
            <input type="hidden" name="action" value="start">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <button type="submit" class="save-button">Bắt đầu / khám tiếp</button>
        </form>
    </c:if>
    <c:if test="${encounter.status == 'IN_CONSULTATION'}">
        <div class="clinical-actions">
            <a class="btn btn-outline" href="${ctx}/doctor/chi-dinh?encounterId=${encounter.encounterId}">Chỉ định cận lâm sàng</a>

            <a class="btn btn-outline" href="${ctx}/doctor/don-thuoc?encounterId=${encounter.encounterId}">Kê đơn thuốc</a>
        </div>
        <form method="post" action="${ctx}/doctor/chi-tiet-buoi-kham" class="doctor-card clinical-card-body clinical-exam-form">
            <input type="hidden" name="encounterId" value="${encounter.encounterId}">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <h2>Nội dung khám</h2>
            <div class="clinical-exam-grid">
            <div class="form-group"><label for="diagnosis">Chẩn đoán</label>
                <textarea id="diagnosis" name="diagnosis" rows="4"><c:out value="${param.diagnosis != null ? param.diagnosis : medicalRecord.diagnosis}"/></textarea></div>
            <div class="form-group"><label for="clinicalNote">Ghi chú lâm sàng</label>
                <textarea id="clinicalNote" name="clinicalNote" rows="4"><c:out value="${param.clinicalNote != null ? param.clinicalNote : medicalRecord.clinicalNote}"/></textarea></div>
            <div class="form-group"><label for="treatment">Hướng điều trị</label>
                <textarea id="treatment" name="treatment" rows="4"><c:out value="${param.treatment != null ? param.treatment : medicalRecord.treatment}"/></textarea></div>
            <div class="form-group"><label for="advice">Lời dặn</label>
                <textarea id="advice" name="advice" rows="3"><c:out value="${param.advice != null ? param.advice : medicalRecord.advice}"/></textarea></div>
            </div>
            <div class="clinical-form-footer">
            <div class="clinical-form-actions">
                <button class="btn btn-outline" name="action" value="save" type="submit">Lưu nháp</button>
                <button class="save-button" name="action" value="finish" type="submit">Xác nhận kết luận</button>
            </div>
            <p class="clinical-help">Cần đủ kết quả cận lâm sàng trước khi kết luận.</p>
            </div>
        </form>
    </c:if>
    <c:if test="${not empty medicalRecord && encounter.status != 'IN_CONSULTATION'}">
        <section class="doctor-card clinical-card-body">
            <h2>${medicalRecord.status == 'FINALIZED' ? 'Kết luận đã xác nhận' : 'Nội dung khám đã lưu nháp'}</h2>
            <div class="clinical-exam-grid clinical-record-grid">
                <div><small>Chẩn đoán</small><p class="clinical-text"><c:out value="${medicalRecord.diagnosis}"/></p></div>
                <div><small>Ghi chú lâm sàng</small><p class="clinical-text"><c:out value="${medicalRecord.clinicalNote}"/></p></div>
                <div><small>Hướng điều trị</small><p class="clinical-text"><c:out value="${medicalRecord.treatment}"/></p></div>
                <div><small>Lời dặn</small><p class="clinical-text"><c:out value="${medicalRecord.advice}"/></p></div>
            </div>
        </section>
    </c:if>
    <jsp:include page="/WEB-INF/includes/clinical-orders.jsp"/>
    <jsp:include page="/WEB-INF/includes/clinical-prescription.jsp"/>
</main>

<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
