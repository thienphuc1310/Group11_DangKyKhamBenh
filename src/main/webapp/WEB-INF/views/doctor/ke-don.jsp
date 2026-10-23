<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-doctor.jsp"><jsp:param name="pageTitle" value="Kê đơn thuốc"/><jsp:param name="activeMenu" value="appointments"/></jsp:include>
<main class="doctor-page clinical-workspace">
    <a class="back-link" href="${ctx}/doctor/chi-tiet-buoi-kham?encounterId=${encounter.encounterId}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Về buổi khám</a>
    <h1>Kê đơn thuốc</h1><p>Bệnh nhân: <c:out value="${encounter.patient.fullName}"/></p>
    <c:if test="${not empty formError}"><p class="form-error" role="alert"><c:out value="${formError}"/></p></c:if>
    <jsp:include page="/WEB-INF/includes/clinical-prescription.jsp"/>
    <c:if test="${encounter.status == 'IN_CONSULTATION' && (empty prescription || prescription.status == 'DRAFT')}">
        <form method="post" action="${ctx}/doctor/don-thuoc" class="doctor-card clinical-card-body">
            <input type="hidden" name="encounterId" value="${encounter.encounterId}">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <datalist id="prescription-medicines">
                <c:forEach var="medicine" items="${medicines}">
                    <option value="<c:out value='${medicine.medicineName}'/>" data-medicine-id="${medicine.medicineId}"
                            label="Còn ${medicine.availableQuantity} viên — ${medicine.price} đ/viên"></option>
                </c:forEach>
            </datalist>
            <div id="prescription-lines">
                <c:set var="lineCount" value="${not empty paramValues.medicineId ? fn:length(paramValues.medicineId) : 1}"/>
                <c:forEach begin="0" end="${(lineCount > 50 ? 50 : lineCount) - 1}" varStatus="row">
                <div class="prescription-line">
                    <c:set var="selectedMedicineName" value="${paramValues.medicineName[row.index]}"/>
                    <c:if test="${empty selectedMedicineName}">
                        <c:forEach var="medicine" items="${medicines}">
                            <c:if test="${paramValues.medicineId[row.index] == medicine.medicineId}"><c:set var="selectedMedicineName" value="${medicine.medicineName}"/></c:if>
                        </c:forEach>
                    </c:if>
                    <div class="form-group prescription-medicine-field">
                        <label for="medicine-search-${row.index}">Thuốc</label>
                        <input id="medicine-search-${row.index}" class="medicine-search" type="text" name="medicineName"
                               list="prescription-medicines" autocomplete="off" maxlength="255" placeholder="Nhập tên thuốc..."
                               value="<c:out value='${selectedMedicineName}'/>" aria-describedby="medicine-summary-${row.index}">
                        <input type="hidden" name="medicineId" value="<c:out value='${paramValues.medicineId[row.index]}'/>">
                        <span id="medicine-summary-${row.index}" class="medicine-summary" aria-live="polite" hidden></span>
                    </div>
                    <div class="form-group"><label>Số lượng viên</label><input name="quantity" type="number" min="1" value="<c:out value='${paramValues.quantity[row.index]}' default='1'/>" required></div>
                    <div class="form-group"><label>Liều dùng</label><input name="dosage" maxlength="255" value="<c:out value='${paramValues.dosage[row.index]}'/>" placeholder="Ví dụ: 1 viên/lần"></div>
                    <div class="form-group"><label>Tần suất</label><input name="frequency" maxlength="255" value="<c:out value='${paramValues.frequency[row.index]}'/>" placeholder="Ví dụ: 2 lần/ngày"></div>
                    <div class="form-group"><label>Đường dùng</label><input name="route" maxlength="100" value="<c:out value='${paramValues.route[row.index]}' default='Uống'/>" ></div>
                    <div class="form-group"><label>Số ngày</label><input name="durationDays" type="number" min="1" value="<c:out value='${paramValues.durationDays[row.index]}' default='1'/>" required></div>
                    <div class="form-group"><label>Hướng dẫn</label><input name="instruction" maxlength="1000" value="<c:out value='${paramValues.instruction[row.index]}'/>" ></div>
                    <button type="button" class="remove-line btn btn-outline">Xóa dòng</button>
                </div>
                </c:forEach>
            </div>
            <button type="button" id="add-prescription-line" class="btn btn-outline">Thêm thuốc</button>
            <div class="form-group prescription-advice"><label for="rx-advice">Lời dặn đơn thuốc</label><textarea id="rx-advice" name="advice" rows="3"><c:out value="${param.advice}"/></textarea></div>
            <button class="save-button" type="submit">Xác nhận đơn thuốc</button>
        </form>
    </c:if>
</main>
<script src="${ctx}/js/prescription.js?v=20261019-2"></script>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
