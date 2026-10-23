<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-doctor.jsp"><jsp:param name="pageTitle" value="Chỉ định cận lâm sàng"/><jsp:param name="activeMenu" value="appointments"/></jsp:include>

<main class="doctor-page clinical-workspace">
    <a class="back-link" href="${ctx}/doctor/chi-tiet-buoi-kham?encounterId=${encounter.encounterId}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Về buổi khám</a>
    <div class="page-heading">
        <div><h1>Chỉ định cận lâm sàng</h1></div>
        <div class="appointment-info"><small>Ngày thực hiện</small><strong><c:out value="${serviceDate}"/></strong></div>
    </div>
    <section class="doctor-card clinical-card-body clinical-patient-strip" aria-label="Bệnh nhân được chỉ định">
        <div><small>Bệnh nhân</small><h2><c:out value="${encounter.patient.fullName}"/></h2></div>
        <div><small>Mã bệnh nhân</small><strong><c:out value="${encounter.patient.patientCode}"/></strong></div>
        <div><small>Mã lịch hẹn</small><strong><c:out value="${encounter.appointment.appointmentCode}"/></strong></div>
    </section>
    <c:if test="${not empty formError}"><p class="form-error" role="alert"><c:out value="${formError}"/></p></c:if>
    <c:if test="${encounter.status == 'IN_CONSULTATION'}">



        <form id="diagnostic-order-form" method="post" action="${ctx}/doctor/chi-dinh" class="doctor-card diagnostic-order-form">
            <input type="hidden" name="encounterId" value="${encounter.encounterId}">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">

            <div class="clinical-card-body">
                <h2>Lập phiếu chỉ định</h2>
                <div class="form-group">
                    <label for="indication">Lý do chỉ định / yêu cầu thực hiện</label>
                    <textarea id="indication" name="clinicalIndication" rows="3" placeholder="Nhập triệu chứng hoặc nội dung cần kiểm tra"><c:out value="${param.clinicalIndication}"/></textarea>
                </div>
            </div>

            <%-- tìm danh sách dịch vụ theo tên --%>
            <div class="form-group">
                <label for="service-search">Nhập tên dịch vụ</label>
                <input id="service-search" type="search"placeholder="Nhập tên dịch vụ cần thực hiện"autocomplete="off" aria-controls="diagnostic-service-rows"></div>

            <div class="clinical-form-actions">
                <button id="search-services" type="button" class="save-button">Tìm kiếm</button>
                <button id="reset-service-search" type="button" class="btn btn-outline">Xóa lọc</button></div>

            <div class="diagnostic-list-toolbar">
                <h3>Danh sách dịch vụ</h3>
                <div class="clinical-form-actions">
                    <button id="select-all-services" class="btn btn-outline" type="button" hidden>Chọn tất cả khả dụng</button>
                    <button id="clear-services" class="btn btn-outline" type="button" hidden>Bỏ chọn tất cả</button>
                </div>
            </div>
            <div class="table-scroll">
                <table class="diagnostic-service-table">
                    <thead><tr><th scope="col">Chọn</th><th scope="col">Dịch vụ</th><th scope="col">Loại</th><th scope="col">Khoa thực hiện</th><th scope="col">Giá</th><th scope="col">Phút</th><th scope="col">Khả dụng hôm nay</th></tr></thead>
                    <tbody id="diagnostic-service-rows">
                    <c:forEach var="service" items="${services}">
                        <c:set var="available" value="${serviceAvailability[service.serviceId]}"/>
                        <tr class="${available ? '' : 'clinical-unavailable'}">
                            <td><input id="service-${service.serviceId}" type="checkbox" name="serviceIds" value="${service.serviceId}"
                                       data-price="${service.price.amount}" aria-describedby="availability-${service.serviceId}" ${not available ? 'disabled' : ''}
                                       ${available && selectedServices[service.serviceId] ? 'checked' : ''}></td>
                            <td><label for="service-${service.serviceId}"><strong class="diagnostic-service-name"><c:out value="${service.serviceName}"/></strong></label><small><c:out value="${service.serviceCode}"/></small></td>
                            <td>${service.serviceType == 'LABORATORY' ? 'Xét nghiệm' : 'Chẩn đoán hình ảnh'}</td>
                            <td><c:out value="${service.specialty.name}"/></td>
                            <td class="clinical-money"><fmt:formatNumber value="${service.price.amount}" groupingUsed="true" maxFractionDigits="2"/> đ</td>
                            <td><c:out value="${service.estimatedDurationMinutes}"/></td>
                            <td id="availability-${service.serviceId}">
                                <c:choose>
                                    <c:when test="${available}"><span class="status completed">Có thể chỉ định</span></c:when>
                                    <c:otherwise>
                                        <span class="status care-assessing">Chưa có lịch nhân sự</span>
                                        <small>${service.serviceType == 'IMAGING' ? 'Chưa có bác sĩ' : 'Chưa có chuyên viên xét nghiệm'} cùng khoa làm việc ngày <c:out value="${serviceDate}"/>.</small>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty services}"><tr><td colspan="7" class="empty-state">Chưa có dịch vụ cận lâm sàng khả dụng.</td></tr></c:if>
                    <tr id="service-search-empty" hidden><td colspan="7" class="empty-state" role="status">Không tìm thấy dịch vụ phù hợp.</td></tr>
                    </tbody>
                </table>
            </div>

            <div id="diagnostic-selection" class="diagnostic-selection" aria-live="polite" hidden>
                <div class="diagnostic-selection-heading">
                    <strong>Đã chọn <span id="selected-service-count">0</span> dịch vụ</strong>
                    <strong>Tổng tạm tính: <span id="selected-service-total">0</span> đ</strong>
                </div>
                <ul id="selected-service-list" class="diagnostic-selected-list"></ul>
                <p id="diagnostic-selection-hint" class="clinical-help" hidden></p>
            </div>
            <div class="clinical-card-body clinical-form-footer">
                <div class="clinical-form-actions">
                    <a class="btn btn-outline" href="${ctx}/doctor/chi-tiet-buoi-kham?encounterId=${encounter.encounterId}">Quay lại</a>
                    <button id="confirm-diagnostic-order" class="save-button" type="submit" ${availableServiceCount == 0 ? 'disabled' : ''}>Xác nhận chỉ định</button>
                </div>
            </div>
        </form>

    </c:if>
    <jsp:include page="/WEB-INF/includes/clinical-orders.jsp"/>
</main>
<script src="${ctx}/js/diagnostic-order.js?v=20261019-3" defer></script>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
