<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-patient.jsp"><jsp:param name="pageTitle" value="Thanh toán"/><jsp:param name="activeMenu" value="payments"/></jsp:include>
<c:if test="${not empty paymentError}"><p class="patient-notice patient-notice-error" role="alert"><c:out value="${paymentError}"/></p></c:if>
<c:choose>
    <c:when test="${not empty selectedPayment}">
        <c:set var="p" value="${selectedPayment}"/>
        <div class="patient-heading"><div><h1>${p.status == 'PENDING' ? 'Xác nhận thanh toán' : 'Chi tiết thanh toán'}</h1><p>Mã giao dịch: <c:out value="${p.transactionCode}"/></p></div><a class="patient-button" href="${ctx}/thanhtoan"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Lịch sử thanh toán</a></div>
        <div class="patient-payment-layout">
            <div>
                <section class="patient-card"><h2>Thông tin lịch khám</h2>
                    <div class="patient-info-row"><span>Mã lịch hẹn</span><strong><c:out value="${p.appointment.appointmentCode}"/></strong></div>
                    <div class="patient-info-row"><span>Bác sĩ</span><strong><c:out value="${p.appointment.schedule.doctor.fullName}"/></strong></div>
                    <div class="patient-info-row"><span>Chuyên khoa</span><span><c:out value="${p.appointment.schedule.doctor.specialty.name}"/></span></div>
                    <div class="patient-info-row"><span>Ngày khám</span><span><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${p.appointment.schedule.workDate}"/></jsp:include></span></div>
                    <div class="patient-info-row"><span>Giờ khám</span><span>${not empty p.appointment.slot ? p.appointment.slot.startTime : p.appointment.schedule.startTime} – ${not empty p.appointment.slot ? p.appointment.slot.endTime : p.appointment.schedule.endTime}</span></div>
                    <c:if test="${not empty p.appointment.reason}"><div class="patient-info-row"><span>Lý do khám</span><span><c:out value="${p.appointment.reason}"/></span></div></c:if>
                </section>

                <c:choose>
                    <c:when test="${p.status == 'PENDING'}">
                        <section class="patient-card"><h2>Phương thức thanh toán</h2>
                            <form method="post" action="${ctx}/thanhtoan">
                                <input type="hidden" name="paymentId" value="${p.paymentId}"><input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                <label class="patient-method"><input type="radio" name="method" value="BANK_TRANSFER" checked required>Chuyển khoản ngân hàng</label>
                                <label class="patient-method"><input type="radio" name="method" value="VNPAY">VNPay</label>
                                <label class="patient-method"><input type="radio" name="method" value="MOMO">MoMo</label>
                                <div class="patient-form-footer"><button type="submit" class="patient-button patient-button-primary">Thanh toán <fmt:formatNumber value="${p.amount}" pattern="#,##0"/> đ</button></div>
                            </form>
                        </section>
                    </c:when>
                    <c:otherwise><section class="patient-card"><h2>Thông tin giao dịch</h2>
                        <div class="patient-info-row"><span>Trạng thái</span><jsp:include page="/WEB-INF/includes/patient-payment-status.jsp"><jsp:param name="status" value="${p.status}"/></jsp:include></div>
                        <div class="patient-info-row"><span>Phương thức</span><span>${p.method == 'BANK_TRANSFER' ? 'Chuyển khoản ngân hàng' : (p.method == 'CASH' ? 'Tiền mặt' : (p.method == 'VNPAY' ? 'VNPay' : 'MoMo'))}</span></div>
                        <div class="patient-info-row"><span>Ngày thanh toán</span><span><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${p.paidAt}"/><jsp:param name="time" value="true"/></jsp:include></span></div>
                    </section></c:otherwise>
                </c:choose>
            </div>
            <aside class="patient-card"><h2>Chi tiết thanh toán</h2>
                <c:if test="${not empty p.invoice}"><p class="patient-muted"><c:out value="${p.invoice.invoiceCode}"/></p></c:if>
                <p><span class="patient-badge">${p.invoice.invoiceType == 'DIAGNOSTIC' ? 'Cận lâm sàng' : (p.invoice.invoiceType == 'MEDICINE' ? 'Thuốc' : 'Phí khám')}</span></p>
                <c:forEach var="line" items="${p.invoice.items}"><div class="patient-info-row"><span><c:out value="${line.descriptionSnapshot}"/> × ${line.quantity}</span><strong><fmt:formatNumber value="${line.lineTotal}" pattern="#,##0"/> đ</strong></div></c:forEach>
                <div class="patient-detail"><h3>Tổng thanh toán</h3><div class="patient-payment-total"><fmt:formatNumber value="${p.amount}" pattern="#,##0"/> đ</div></div>
                <jsp:include page="/WEB-INF/includes/patient-payment-status.jsp"><jsp:param name="status" value="${p.status}"/></jsp:include>
            </aside>
        </div>
    </c:when>
    <c:otherwise>
        <div class="patient-heading"><div><h1>Thanh toán</h1>
        <p>Theo dõi các khoản phí và lịch sử giao dịch của bạn.</p>
        </div>
        </div>

        <div data-patient-filter-area>
            <div class="patient-tabs"><button type="button" class="active" data-filter-value="all" aria-pressed="true">Tất cả</button><button type="button" data-filter-value="PENDING" aria-pressed="false">Chờ thanh toán</button><button type="button" data-filter-value="PAID" aria-pressed="false">Đã thanh toán</button></div>
            <c:choose><c:when test="${empty payments}"><div class="patient-card patient-empty"><jsp:include page="/WEB-INF/includes/patient-icon.jsp"><jsp:param name="name" value="card"/></jsp:include><h2>Chưa có giao dịch</h2><a class="patient-button" href="${ctx}/patient/lich-kham-cua-toi">Xem lịch khám</a></div></c:when>
            <c:otherwise><div class="patient-card patient-table-scroll"><table class="patient-table">
                <thead><tr><th>Giao dịch</th><th>Nội dung</th><th>Lịch khám</th><th>Số tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody><c:forEach var="item" items="${payments}"><tr data-patient-item data-status="${item.status}">
                    <td><strong><c:out value="${item.transactionCode}"/></strong></td>
                    <td>${item.invoice.invoiceType == 'DIAGNOSTIC' ? 'Cận lâm sàng' : (item.invoice.invoiceType == 'MEDICINE' ? 'Thuốc' : 'Phí khám')}<small><c:out value="${item.appointment.schedule.doctor.specialty.name}"/></small></td>
                    <td><jsp:include page="/WEB-INF/includes/patient-date.jsp"><jsp:param name="value" value="${item.appointment.schedule.workDate}"/></jsp:include><small><c:out value="${item.appointment.schedule.doctor.fullName}"/></small></td>
                    <td><strong><fmt:formatNumber value="${item.amount}" pattern="#,##0"/> đ</strong></td>
                    <td><jsp:include page="/WEB-INF/includes/patient-payment-status.jsp"><jsp:param name="status" value="${item.status}"/></jsp:include></td>
                    <td><a class="patient-button ${item.status == 'PENDING' ? 'patient-button-primary' : ''}" href="${ctx}/thanhtoan?paymentId=${item.paymentId}">${item.status == 'PENDING' ? 'Thanh toán' : 'Xem chi tiết'}</a></td>
                </tr></c:forEach></tbody>
            </table></div></c:otherwise></c:choose>
            <p class="patient-card patient-empty" data-filter-empty hidden>Không có giao dịch phù hợp.</p>
        </div>
    </c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
