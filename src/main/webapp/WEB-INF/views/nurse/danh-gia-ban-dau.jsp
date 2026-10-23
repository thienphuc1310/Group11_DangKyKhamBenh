<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<c:url var="listUrl" value="/nurse/assessment">
    <c:param name="workDate" value="${selectedDate}"/>
</c:url>

<jsp:include page="/WEB-INF/includes/header-clinical.jsp">
    <jsp:param name="pageTitle" value="Phiếu đánh giá ban đầu"/>
    <jsp:param name="activeMenu" value="assessment"/>
</jsp:include>

<main class="doctor-page">
    <a class="back-link" href="${listUrl}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Danh sách bệnh nhân</a>
    <h1>Phiếu đánh giá ban đầu</h1>

    <section class="doctor-card patient-summary" aria-label="Thông tin bệnh nhân">
        <span class="patient-avatar" aria-hidden="true">BN</span>
        <div>
            <h2><c:out value="${encounter.appointment.patient.user.fullName}"/></h2>
            <p><c:out value="${encounter.appointment.patient.patientCode}"/></p>
            <span><c:out value="${encounter.appointment.patient.user.phone}" default="Chưa cập nhật điện thoại"/></span>
        </div>
        <div><small>Mã lượt khám</small><span><c:out value="${encounter.encounterCode}"/></span></div>
        <div><small>Bác sĩ phụ trách</small><span><c:out value="${encounter.doctor.user.fullName}"/></span></div>
        <span class="status ${encounter.status == 'ASSESSING' ? 'care-assessing' : 'waiting'}">
            ${encounter.status == 'ASSESSING' ? 'Đang đánh giá' : 'Chờ đánh giá'}
        </span>
    </section>

    <section class="doctor-card care-card-body clinical-section">
    <h2>Thông tin đánh giá ban đầu</h2>
    <c:choose>
        <c:when test="${empty assessment}">
            <p class="muted">
                Nhận bệnh nhân trước khi nhập thông tin đánh giá.
            </p>

            <form method="post" action="${ctx}/nurse/assessment">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                <input type="hidden" name="encounterId"
                       value="${encounter.encounterId}">
                <input type="hidden" name="workDate"
                       value="${selectedDate}">

                <button class="save-button" type="submit" name="action" value="begin">
                    Nhận bệnh nhân
                </button>
            </form>
        </c:when>

        <c:otherwise>
            <c:if test="${param.saved == '1'}">
                <div class="success-message" role="status">Đã lưu nháp phiếu đánh giá.</div>
            </c:if>

            <form method="post" action="${ctx}/nurse/assessment">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                <input type="hidden" name="encounterId"
                       value="${encounter.encounterId}">
                <input type="hidden" name="workDate"
                       value="${selectedDate}">

                <div class="care-assessment-grid">
                    <div class="form-group care-wide">
                        <label for="chiefComplaint">Triệu chứng / lý do khám</label>
                        <textarea id="chiefComplaint"
                                  name="chiefComplaint"><c:out value="${assessment.chiefComplaint}"/></textarea>
                    </div>

                    <div class="form-group">
                        <label for="height">Chiều cao (cm)</label>
                        <input id="height" type="number" step="any"
                               name="height"
                               value="<c:out value='${assessment.height}'/>">
                    </div>

                    <div class="form-group">
                        <label for="weight">Cân nặng (kg)</label>
                        <input id="weight" type="number" step="any"
                               name="weight"
                               value="<c:out value='${assessment.weight}'/>">
                    </div>

                    <div class="form-group">
                        <label for="temperature">Nhiệt độ (°C)</label>
                        <input id="temperature" type="number" step="any"
                               name="temperature"
                               value="<c:out value='${assessment.temperature}'/>">
                    </div>

                    <div class="form-group">
                        <label for="heartRate">Nhịp tim (lần/phút)</label>
                        <input id="heartRate" type="number" step="1"
                               name="heartRate"
                               value="<c:out value='${assessment.heartRate}'/>">
                    </div>

                    <div class="form-group">
                        <label for="systolicPressure">
                            Huyết áp tâm thu (mmHg)
                        </label>
                        <input id="systolicPressure" type="number" step="1"
                               name="systolicPressure"
                               value="<c:out value='${assessment.systolicPressure}'/>">
                    </div>

                    <div class="form-group">
                        <label for="diastolicPressure">
                            Huyết áp tâm trương (mmHg)
                        </label>
                        <input id="diastolicPressure" type="number" step="1"
                               name="diastolicPressure"
                               value="<c:out value='${assessment.diastolicPressure}'/>">
                    </div>

                    <div class="form-group">
                        <label for="respiratoryRate">Nhịp thở (lần/phút)</label>
                        <input id="respiratoryRate" type="number" step="1"
                               name="respiratoryRate"
                               value="<c:out value='${assessment.respiratoryRate}'/>">
                    </div>

                    <div class="form-group">
                        <label for="spo2">SpO2 (%)</label>
                        <input id="spo2" type="number" step="any"
                               name="spo2"
                               value="<c:out value='${assessment.spo2}'/>">
                    </div>

                    <div class="form-group care-wide">
                        <label for="note">Ghi chú</label>
                        <textarea id="note" name="note"><c:out value="${assessment.note}"/></textarea>
                    </div>
                </div>


                <div class="form-actions">
                    <button type="submit" name="action" value="save">
                        Lưu nháp
                    </button>

                    <button class="save-button" type="submit"
                            name="action" value="complete"
                            onclick="return confirm('Hoàn tất và chuyển sang chờ bác sĩ?');">
                        Hoàn tất đánh giá
                    </button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
    </section>
</main>
<jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
