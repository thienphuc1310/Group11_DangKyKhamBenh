<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-clinical.jsp"><jsp:param name="pageTitle" value="Danh sách chỉ định"/></jsp:include>
<main class="doctor-page"><h1>Danh sách chỉ định cận lâm sàng</h1>
    <a class="btn btn-outline" href="${ctx}/diagnostics/worklist">Cập nhật danh sách</a>
    <section class="doctor-card clinical-section"><div class="table-scroll"><table>
        <thead><tr><th>Bệnh nhân</th><th>Dịch vụ</th><th>Bác sĩ chỉ định</th><th>Trạng thái</th><th>Dự kiến</th><th>Thao tác</th></tr></thead>
        <tbody><c:forEach var="item" items="${items}"><tr>
            <td><strong><c:out value="${item.order.encounter.patient.fullName}"/></strong><br><c:out value="${item.order.encounter.patient.patientCode}"/></td>
            <td><c:out value="${item.serviceNameSnapshot}"/></td>
            <td><c:out value="${item.order.encounter.doctor.fullName}"/></td>
            <td><c:out value="${item.status}"/></td><td><c:out value="${item.expectedResultAt}" default="Chưa bắt đầu"/></td>
            <td><c:choose>
                <c:when test="${item.status == 'WAITING_ACCEPTANCE'}"><form method="post" action="${ctx}/diagnostics/worklist">
                    <input type="hidden" name="itemId" value="${item.orderItemId}"><input type="hidden" name="action" value="accept">
                    <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><button class="save-button" type="submit">Tiếp nhận</button>
                </form></c:when>
                <c:otherwise><a href="${ctx}/diagnostics/result?itemId=${item.orderItemId}">Mở chi tiết</a></c:otherwise>
            </c:choose></td>
        </tr></c:forEach><c:if test="${empty items}"><tr><td colspan="6">Chưa có chỉ định phù hợp.</td></tr></c:if></tbody>
    </table></div></section>
</main><jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
