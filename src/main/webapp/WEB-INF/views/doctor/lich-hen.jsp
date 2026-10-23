<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Danh sách lịch hẹn"/>
    <jsp:param name="activeMenu" value="appointments"/>
</jsp:include>

<main class="doctor-page">
    <h1>Danh sách lịch hẹn</h1>

    <c:if test="${paraclinical}">
        <section class="doctor-card">
            <div class="filter-form">
                <div class="form-group search-field">
                    <label for="patient-search">Tìm bệnh nhân chờ thực hiện</label>
                    <input type="search" id="patient-search" placeholder="Tên, mã bệnh nhân, điện thoại hoặc dịch vụ..." autocomplete="off">
                </div>
                <div class="record-filter-actions"><a class="btn btn-outline" href="${pageContext.request.contextPath}/doctor/lich-hen">Cập nhật danh sách</a></div>
            </div>
            <div class="table-scroll"><table>
                <thead><tr><th>Mã phiếu</th><th>Thời gian chỉ định</th><th>Dịch vụ</th><th>Bệnh nhân</th><th>Bác sĩ chỉ định</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody id="appointment-list">
                    <c:forEach var="item" items="${items}">
                        <tr class="appointment-row">
                            <td><c:out value="${item.order.orderCode}"/></td>
                            <td><c:out value="${item.order.orderedAt}"/></td>
                            <td><c:out value="${item.serviceNameSnapshot}"/></td>
                            <td><strong><c:out value="${item.order.encounter.patient.fullName}"/></strong><br>
                                <small><c:out value="${item.order.encounter.patient.patientCode}"/> — <c:out value="${item.order.encounter.patient.phone}"/></small></td>
                            <td><c:out value="${item.order.encounter.doctor.fullName}"/></td>
                            <td><c:choose>
                                <c:when test="${item.status == 'WAITING_ACCEPTANCE'}"><span class="status waiting">Chờ tiếp nhận</span></c:when>
                                <c:when test="${item.status == 'ACCEPTED'}"><span class="status waiting">Đã tiếp nhận</span></c:when>
                                <c:otherwise><span class="status care-assessing">Đang thực hiện</span></c:otherwise>
                            </c:choose></td>
                            <td><c:choose>
                                <c:when test="${item.status == 'WAITING_ACCEPTANCE'}">
                                    <form method="post" action="${pageContext.request.contextPath}/doctor/lich-hen">
                                        <input type="hidden" name="itemId" value="${item.orderItemId}">
                                        <input type="hidden" name="action" value="accept">
                                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                        <button class="save-button" type="submit">Tiếp nhận</button>
                                    </form>
                                </c:when>
                                <c:otherwise><a class="btn btn-outline" href="${pageContext.request.contextPath}/diagnostics/result?itemId=${item.orderItemId}">Mở chi tiết</a></c:otherwise>
                            </c:choose></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty items}"><tr><td colspan="7" class="empty-state">Chưa có chỉ định đã thanh toán phù hợp.</td></tr></c:if>
                </tbody>
            </table></div>
            <p class="table-note" id="appointment-count" aria-live="polite">${empty items ? 0 : items.size()} dịch vụ.</p>
        </section>
    </c:if>

    <c:if test="${not paraclinical}">
    <c:if test="${param.completed == '1'}">
        <div class="success-message" role="status">
            Đã lưu kết luận buổi khám.
        </div>
    </c:if>

    <section class="doctor-card">
        <div class="filter-form">
            <form method="get" action="${pageContext.request.contextPath}/doctor/lich-hen" class="form-group">
                <label for="appointment-date">Ngày khám</label>
                <input type="date" id="appointment-date" name="workDate"
                       value="<c:out value='${selectedDate}'/>" required>
                <button type="submit">Xem lịch</button>
            </form>
            <div class="form-group search-field">
                <label for="patient-search">Tìm bệnh nhân trong ngày</label>
                <input type="search" id="patient-search"
                       placeholder="Tìm theo tên hoặc số điện thoại..." autocomplete="off">
            </div>
        </div>
        <div class="table-scroll">
            <table>
                <thead>
                    <tr>
                        <th>Số thứ tự</th>
                        <th>Khung giờ</th>
                        <th>Mã lịch hẹn</th>
                        <th>Bệnh nhân</th>
                        <th>Lý do khám</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="appointment-list">
                    <c:forEach var="ticket" items="${tickets}">
                        <c:set var="encounter" value="${ticket.encounter}"/>
                        <c:set var="appointment" value="${encounter.appointment}"/>
                        <tr class="appointment-row">
                            <td><c:out value="${ticket.queueNumber}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty appointment.slot}">
                                        <c:out value="${appointment.slot.startTime}"/> –
                                        <c:out value="${appointment.slot.endTime}"/>
                                    </c:when>
                                    <c:otherwise>Chưa có khung giờ</c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${appointment.appointmentCode}"/></td>
                            <td>
                                <strong><c:out value="${appointment.patient.user.fullName}"/></strong><br>
                                <small><c:out value="${appointment.patient.user.phone}"/></small>
                            </td>
                            <td><c:out value="${appointment.reason}" default="Không ghi lý do"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${encounter.status == 'READY_FOR_REVIEW'}">Đủ kết quả, chờ khám tiếp</c:when>
                                    <c:when test="${encounter.status == 'IN_CONSULTATION'}">Đang khám</c:when>
                                    <c:otherwise>Chờ bác sĩ</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/doctor/chi-tiet-buoi-kham?encounterId=${encounter.encounterId}">
                                    Xem chi tiết
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty tickets}">
                        <tr><td colspan="7">Chưa có bệnh nhân đủ điều kiện trong ngày đã chọn.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        <p class="table-note" id="appointment-count" aria-live="polite">
            <c:choose>
                <c:when test="${empty tickets}">Chưa có bệnh nhân đã hoàn tất đánh giá ban đầu.</c:when>
                <c:otherwise>Hiển thị ${tickets.size()} lượt khám.</c:otherwise>
            </c:choose>
        </p>
    </section>
    </c:if>
</main>

<%--Footer--%>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
