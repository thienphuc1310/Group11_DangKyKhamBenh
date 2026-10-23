<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Quản lý lịch làm việc - phiên bản cũ"/>
    <jsp:param name="activeMenu" value="schedule"/>
</jsp:include>

<main class="doctor-page">
    <h1>Quản lý lịch làm việc</h1>
    <p>Xem và quản lý lịch làm việc của bạn.</p>

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
            <button type="button" class="save-button" data-open="shift-dialog">+ Thêm ca khám</button>
            <button type="button" data-open="leave-dialog">Đánh dấu nghỉ</button>
        </div>
    </div>

    <section class="doctor-card table-scroll" aria-label="Lịch làm việc trong tuần">
        <table class="schedule-table">
            <thead>
            <tr>
                <th scope="col">Ca khám</th>
                <th scope="col">Thứ Hai<span class="day-date" data-day="0">—</span></th>
                <th scope="col">Thứ Ba<span class="day-date" data-day="1">—</span></th>
                <th scope="col">Thứ Tư<span class="day-date" data-day="2">—</span></th>
                <th scope="col">Thứ Năm<span class="day-date" data-day="3">—</span></th>
                <th scope="col">Thứ Sáu<span class="day-date" data-day="4">—</span></th>
                <th scope="col">Thứ Bảy<span class="day-date" data-day="5">—</span></th>
                <th scope="col">Chủ Nhật<span class="day-date" data-day="6">—</span></th>
            </tr>
            </thead>
            <tbody id="schedule-body">
            <tr>
                <th scope="row">Ca khám</th>
                <td data-calendar-day="0"></td><td data-calendar-day="1"></td>
                <td data-calendar-day="2"></td><td data-calendar-day="3"></td>
                <td data-calendar-day="4"></td><td data-calendar-day="5"></td>
                <td data-calendar-day="6"></td>
            </tr>
            </tbody>
        </table>
        <p id="schedule-empty" hidden>Chưa có ca làm việc trong tuần này.</p>
    </section>
    <p class="schedule-legend"><span></span> Ca khám</p>

    <section class="doctor-card table-scroll" aria-label="Danh sách ca khám">
        <table>
            <thead>
            <tr><th>ID</th><th>Mã bác sĩ</th><th>Ngày làm việc</th><th>Bắt đầu</th><th>Kết thúc</th><th>Phòng khám</th></tr>
            </thead>
            <tbody id="saved-schedules" data-demo="${requestScope.schedules == null}">
            <c:forEach items="${requestScope.schedules}" var="schedule">
                <tr data-date="<c:out value='${schedule.workDate}'/>">
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
    </section>

    <dialog id="shift-dialog">
        <form class="dialog-form">
            <h2>Thông tin ca khám</h2>
            <div class="form-group"><label for="shift-date">Ngày khám *</label><input type="date" id="shift-date" name="shiftDate" required></div>
            <div class="form-group"><label for="start-time">Giờ bắt đầu *</label><input type="time" id="start-time" name="startTime" required></div>
            <div class="form-group"><label for="end-time">Giờ kết thúc *</label><input type="time" id="end-time" name="endTime" required></div>
            <div class="form-group"><label for="room">Phòng khám *</label><input type="text" id="room" name="room" maxlength="100" required></div>
            <div class="form-actions"><button type="button" data-close>Đóng</button><button type="button" class="save-button" disabled>Lưu ca khám</button></div>
        </form>
    </dialog>

    <dialog id="leave-dialog">
        <form class="dialog-form">
            <h2>Đánh dấu ngày nghỉ</h2>
            <div class="form-group"><label for="leave-date">Ngày nghỉ *</label><input type="date" id="leave-date" name="leaveDate" required></div>
            <div class="form-group"><label for="leave-reason">Lý do nghỉ</label><textarea id="leave-reason" name="leaveReason" rows="3" placeholder="Nhập lý do nghỉ"></textarea></div>
            <div class="form-actions"><button type="button" data-close>Đóng</button><button type="button" class="save-button" disabled>Xác nhận nghỉ</button></div>
        </form>
    </dialog>
</main>

<jsp:include page="/WEB-INF/includes/footer-doctor.jsp">
    <jsp:param name="persistedSchedule" value="false"/>
</jsp:include>
