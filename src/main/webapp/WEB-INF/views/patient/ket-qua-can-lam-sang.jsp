<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Kết quả cận lâm sàng"/><jsp:param name="activeMenu" value="results"/></jsp:include>
<div class="patient-heading"><div><h1>Kết quả cận lâm sàng</h1><p>Tra cứu kết quả xét nghiệm và chẩn đoán hình ảnh.</p></div></div>
<div data-patient-filter-area>
    <div class="patient-tabs"><button type="button" class="active" data-filter-value="all" aria-pressed="true">Tất cả</button><button type="button" data-filter-value="LABORATORY" aria-pressed="false">Xét nghiệm</button><button type="button" data-filter-value="IMAGING" aria-pressed="false">Chẩn đoán hình ảnh</button></div>
    <c:forEach var="result" items="${results}" varStatus="row">
        <details class="patient-card patient-result" data-patient-item data-status="${result.orderItem.serviceTypeSnapshot}" ${row.first ? 'open' : ''}>
            <summary class="patient-card-header"><div><h2><c:out value="${result.orderItem.serviceNameSnapshot}"/></h2><span class="patient-muted">Ngày có kết quả: <jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${result.releasedAt}"/><jsp:param name="time" value="true"/></jsp:include></span></div><span class="patient-badge patient-badge-green">Đã có kết quả</span></summary>
            <div class="patient-summary"><div><small>Bác sĩ khám</small><strong><c:out value="${result.orderItem.order.encounter.doctor.fullName}"/></strong></div><div><small>Mã lịch hẹn</small><strong><c:out value="${result.orderItem.order.encounter.appointment.appointmentCode}"/></strong></div></div>
            <div class="patient-detail"><h3>Nội dung kết quả</h3><p class="patient-text"><c:out value="${result.findings}" default="Không có"/></p></div>
            <div class="patient-detail"><h3>Kết luận</h3><p class="patient-text"><c:out value="${result.conclusion}" default="Không có"/></p></div>
            <div class="patient-detail"><h3>Tệp đính kèm</h3>
                <c:set var="hasFile" value="${false}"/>
                <c:forEach var="file" items="${resultFiles}"><c:if test="${file.result.resultId == result.resultId}">
                    <c:set var="hasFile" value="${true}"/>
                    <a class="patient-file" href="${ctx}/results/file?fileId=${file.fileId}" target="_blank" rel="noopener"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="document"/></jsp:include><span><c:out value="${file.originalName}"/></span>Mở tệp <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-up-right.png" alt="" aria-hidden="true"></a>
                </c:if></c:forEach>
                <c:if test="${!hasFile}"><p class="patient-muted">Không có tệp đính kèm.</p></c:if>
            </div>
        </details>
    </c:forEach>
    <c:if test="${empty results}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="lab"/></jsp:include><h2>Chưa có kết quả</h2><p>Kết quả đã được phát hành sẽ hiển thị tại đây.</p></div></c:if>
    <p class="patient-card patient-empty" data-filter-empty hidden>Không có kết quả phù hợp.</p>
</div>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
