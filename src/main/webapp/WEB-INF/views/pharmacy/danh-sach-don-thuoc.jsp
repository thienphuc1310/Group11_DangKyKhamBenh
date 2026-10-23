<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/includes/header-clinical.jsp"><jsp:param name="pageTitle" value="Đơn chờ cấp thuốc"/></jsp:include>
<main class="doctor-page"><h1>Đơn chờ cấp thuốc</h1>
    <c:if test="${param.dispensed == '1'}"><p class="clinical-success" role="status">Đã cấp thuốc thành công.</p></c:if>
    <section class="doctor-card clinical-section"><div class="table-scroll"><table>
        <thead><tr><th>Mã đơn</th><th>Bệnh nhân</th><th>Bác sĩ</th><th>Ngày kê đơn</th><th>Thao tác</th></tr></thead>
        <tbody><c:forEach var="prescription" items="${prescriptions}"><tr>
            <td><c:out value="${prescription.prescriptionCode}"/></td><td><c:out value="${prescription.encounter.patient.fullName}"/></td>
            <td><c:out value="${prescription.doctor.fullName}"/></td><td><c:out value="${prescription.confirmedAt}"/></td>
            <td><a href="${pageContext.request.contextPath}/pharmacy/prescriptions?prescriptionId=${prescription.prescriptionId}">Xem và cấp thuốc</a></td>
        </tr></c:forEach><c:if test="${empty prescriptions}"><tr><td colspan="5">Chưa có đơn đủ điều kiện cấp thuốc.</td></tr></c:if></tbody>
    </table></div></section>
</main><jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
