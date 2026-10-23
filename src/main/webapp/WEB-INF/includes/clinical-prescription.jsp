<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty prescription}">
    <section class="doctor-card card clinical-section clinical-summary-card" aria-label="Đơn thuốc">
        <header class="clinical-summary-heading">
            <h2>Đơn thuốc</h2>
            <p>Mã đơn: <c:out value="${prescription.prescriptionCode}"/></p>
        </header>
        <div class="table-scroll"><table>
            <thead><tr><th>Thuốc</th><th>Số lượng</th><th>Liều dùng</th><th>Tần suất</th><th>Đường dùng</th><th>Số ngày</th><th>Hướng dẫn</th></tr></thead>
            <tbody><c:forEach var="line" items="${prescription.items}"><tr>
                <td><c:out value="${line.medicineNameSnapshot}"/></td>
                <td><c:out value="${line.quantity}"/> <c:out value="${line.unitSnapshot}"/></td>
                <td><c:out value="${line.dosage}"/></td><td><c:out value="${line.frequency}"/></td>
                <td><c:out value="${line.route}"/></td><td><c:out value="${line.durationDays}"/></td>
                <td><c:out value="${line.instruction}" default="Không có"/></td>
            </tr></c:forEach></tbody>
        </table></div>
        <div class="clinical-summary-note"><strong>Lời dặn</strong><p><c:out value="${prescription.advice}" default="Không có"/></p></div>
    </section>
</c:if>
