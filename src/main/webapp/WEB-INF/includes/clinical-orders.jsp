<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty orders}">
    <section class="doctor-card card clinical-section clinical-card-body">
        <h2>Chỉ định và kết quả cận lâm sàng</h2>
        <c:forEach var="order" items="${orders}">
            <h3><c:out value="${order.orderCode}"/></h3>
            <p>Chỉ định: <c:out value="${order.clinicalIndication}" default="Không ghi"/></p>
            <c:set var="waitingForDiagnosticPayment" value="${false}"/>
            <c:forEach var="line" items="${order.items}">
                <c:if test="${line.status == 'WAITING_PAYMENT'}"><c:set var="waitingForDiagnosticPayment" value="${true}"/></c:if>
            </c:forEach>
            <c:if test="${waitingForDiagnosticPayment}">
                <div class="diagnostic-payment-note" role="status">
                    <p>Chờ thanh toán cận lâm sàng.</p>
                    <c:if test="${sessionScope.role == 'PATIENT'}"><a href="${pageContext.request.contextPath}/thanhtoan">Đến trang thanh toán</a></c:if>
                </div>
            </c:if>
            <c:forEach var="line" items="${order.items}">
                <article class="clinical-result">
                    <h4><c:out value="${line.serviceNameSnapshot}"/></h4>
                    <c:choose>
                        <c:when test="${line.result.status == 'RELEASED'}">
                            <p class="clinical-success"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/success.png" alt="" aria-hidden="true"> Đã có kết quả</p>
                            <p class="clinical-text">Kết quả: <c:out value="${line.result.findings}"/></p>
                            <p class="clinical-text">Kết luận: <c:out value="${line.result.conclusion}"/></p>
                            <c:forEach var="file" items="${resultFiles}">
                                <c:if test="${file.result.resultId == line.result.resultId}">
                                    <a class="clinical-file" href="${pageContext.request.contextPath}/results/file?fileId=${file.fileId}" target="_blank" rel="noopener">Xem tệp: <c:out value="${file.originalName}"/></a>
                                </c:if>
                            </c:forEach>
                        </c:when>
                        <c:when test="${line.status == 'WAITING_PAYMENT'}"><p>Chờ bệnh nhân thanh toán</p></c:when>
                        <c:when test="${line.status == 'WAITING_ACCEPTANCE'}"><p>Đã thanh toán, chờ tiếp nhận</p></c:when>
                        <c:when test="${line.status == 'CANCELLED'}"><p>Đã hủy</p></c:when>
                        <c:otherwise><p>Đang thực hiện, chưa có kết quả phát hành</p></c:otherwise>
                    </c:choose>
                    <c:if test="${not empty line.expectedResultAt}"><p>Dự kiến có kết quả: <c:out value="${line.expectedResultAt}"/></p></c:if>
                </article>
            </c:forEach>
        </c:forEach>
    </section>
</c:if>
