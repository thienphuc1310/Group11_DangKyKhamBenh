<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/includes/header-clinical.jsp"><jsp:param name="pageTitle" value="Cấp thuốc"/></jsp:include>
<main class="doctor-page"><a href="${pageContext.request.contextPath}/pharmacy/prescriptions"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Danh sách đơn</a>
    <h1>Cấp thuốc</h1><p>Bệnh nhân: <c:out value="${prescription.encounter.patient.fullName}"/> — <c:out value="${prescription.encounter.patient.patientCode}"/></p>
    <jsp:include page="/WEB-INF/includes/clinical-prescription.jsp"/>
    <form method="post" action="${pageContext.request.contextPath}/pharmacy/dispense" class="clinical-actions">
        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
        <input type="hidden" name="prescriptionId" value="${prescription.prescriptionId}">
        <button class="save-button" type="submit">Xác nhận đã cấp đủ thuốc</button>
    </form>
</main><jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
