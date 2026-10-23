<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Quản lý lịch làm việc"/>
    <jsp:param name="activeMenu" value="schedule"/>
</jsp:include>

<main class="doctor-page">
    <h1>Quản lý lịch làm việc</h1>
    <%--Chọn tuần làm việc--%>
    <div class="schedule-toolbar">
        <div class="toolbar-group">
            <button type="button" id="today-button">Hôm nay</button>
            <button type="button" id="previous-week" aria-label="Tuần trước"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-left.png" alt="" aria-hidden="true"></button>
            <button type="button" id="next-week" aria-label="Tuần sau"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></button>
            <label for="schedule-date" class="visually-hidden">Chọn ngày trong tuần</label>
            <input type="date" id="schedule-date">
            <strong id="week-label" aria-live="polite">—</strong>
        </div>
        <div class="toolbar-group">
            <a class="btn" href="${pageContext.request.contextPath}/doctor/leave-requests">Gửi yêu cầu nghỉ</a>
        </div>
    </div>

    <section class="doctor-card table-scroll" aria-label="Lịch làm việc trong tuần">
        <table class="schedule-table">
            <thead><tr><th scope="col">Ca khám</th>
                <th scope="col">Thứ Hai<span class="day-date" data-day="0">—</span></th>
                <th scope="col">Thứ Ba<span class="day-date" data-day="1">—</span></th>
                <th scope="col">Thứ Tư<span class="day-date" data-day="2">—</span></th>
                <th scope="col">Thứ Năm<span class="day-date" data-day="3">—</span></th>
                <th scope="col">Thứ Sáu<span class="day-date" data-day="4">—</span></th>
                <th scope="col">Thứ Bảy<span class="day-date" data-day="5">—</span></th>
                <th scope="col">Chủ Nhật<span class="day-date" data-day="6">—</span></th>
            </tr></thead>
            <tbody id="schedule-body">
                <tr>
                    <th scope="row">Ca khám</th>
                    <td data-calendar-day="0"></td>
                    <td data-calendar-day="1"></td>
                    <td data-calendar-day="2"></td>
                    <td data-calendar-day="3"></td>
                    <td data-calendar-day="4"></td>
                    <td data-calendar-day="5"></td>
                    <td data-calendar-day="6"></td>
                </tr>
            </tbody>
        </table>
        <p id="schedule-empty" hidden>Chưa có ca làm việc trong tuần này.</p>
    </section>

    <%--Nguồn dữ liệu ẩn để JavaScript dựng thời khóa biểu; không hiển thị thành bảng riêng.--%>
    <div hidden aria-hidden="true">
        <table>
            <tbody id="saved-schedules" data-demo="${requestScope.schedules == null}">
                <c:forEach items="${requestScope.schedules}" var="schedule">
                    <tr data-date="<c:out value='${schedule.workDate}'/>" data-status="${schedule.status}">
                        <td><c:out value="${schedule.scheduleId}"/></td>
                        <td><c:out value="${doctorId}"/></td>
                        <td><c:out value="${schedule.workDate}"/></td>
                        <td><c:out value="${schedule.startTime}"/></td>
                        <td><c:out value="${schedule.endTime}"/></td>
                        <td><c:out value="${schedule.room}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty requestScope.schedules}">
                    <tr><td colspan="6">Chưa có ca làm việc.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <c:if test="${showSlotCapacity}">
    <c:if test="${param.capacityUpdated == '1'}">
        <p class="success-message">Đã cập nhật giới hạn bệnh nhân dùng chung cho mọi khung giờ.</p>
    </c:if>
    <c:if test="${not empty requestScope.capacityError}">
        <p class="form-error"><c:out value="${requestScope.capacityError}"/></p>
    </c:if>
    <section class="doctor-card capacity-card" aria-labelledby="capacity-title">
        <h2 id="capacity-title">Giới hạn bệnh nhân cho mỗi khung giờ</h2>
        <p id="capacity-description">Số bệnh nhân tối đa bác sĩ có thể thăm khám trong một khung giờ.</p>
        <form method="post" action="${pageContext.request.contextPath}/doctor/slot-capacity" class="capacity-form">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.clinicalCsrf}'/>">
            <div class="capacity-field">
                <label for="shared-slot-capacity">Số bệnh nhân tối đa</label>
                <input id="shared-slot-capacity" type="number" name="maxPatients" min="1"
                       aria-describedby="capacity-description"
                       value="<c:out value='${requestScope.slotCapacity}'/>" required/>
            </div>
            <button type="submit" class="save-button">Lưu giới hạn</button>
        </form>
    </section>
    </c:if>

</main>

<%--Footer--%>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp">
    <jsp:param name="persistedSchedule" value="false"/>
</jsp:include>
