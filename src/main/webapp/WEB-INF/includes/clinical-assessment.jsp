<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty assessment}">
    <section class="doctor-card card clinical-section clinical-summary-card" aria-label="Đánh giá ban đầu">
        <header class="clinical-summary-heading"><h2>Đánh giá ban đầu</h2></header>
        <dl class="clinical-summary-grid">
            <div class="clinical-summary-wide"><dt>Triệu chứng</dt><dd><c:out value="${assessment.chiefComplaint}" default="Chưa ghi nhận"/></dd></div>
            <div><dt>Nhiệt độ</dt><dd><c:out value="${assessment.temperature}" default="—"/> °C</dd></div>
            <div><dt>Huyết áp</dt><dd><c:out value="${assessment.systolicPressure}" default="—"/> / <c:out value="${assessment.diastolicPressure}" default="—"/> mmHg</dd></div>
            <div><dt>Nhịp tim</dt><dd><c:out value="${assessment.heartRate}" default="—"/> lần/phút</dd></div>
            <div><dt>SpO₂</dt><dd><c:out value="${assessment.spo2}" default="—"/> %</dd></div>
            <div class="clinical-summary-wide"><dt>Ghi chú</dt><dd><c:out value="${assessment.note}" default="Không có"/></dd></div>
        </dl>
    </section>
</c:if>
