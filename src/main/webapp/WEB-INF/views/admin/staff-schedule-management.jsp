<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Phân lịch làm việc - UCare</title></head><body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"/>
    <main class="content-area">
        <h1>Lịch làm việc <c:out value="${scheduleLabel}"/></h1>
        <p class="content-subtitle">Ca trùng giờ sẽ được bỏ qua.</p>
        <c:if test="${not empty param.created}"><p class="alert alert-ok">Đã thêm <c:out value="${param.created}"/> ca; bỏ qua <c:out value="${param.skipped}"/> ca trùng.</p></c:if>

        <form method="post" action="${ctx}${schedulePath}" class="admin-form schedule-assignment-form">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <div class="schedule-form-grid">
                <div class="form-group"><label for="profileId">Chọn <c:out value="${scheduleLabel}"/></label>
                    <select id="profileId" name="profileId" required>
                        <option value="">-- Chọn nhân sự --</option>
                        <c:forEach var="person" items="${people}">
                            <c:set var="personId" value="${scheduleRole == 'STAFF' ? person.staffId : scheduleRole == 'NURSE' ? person.nurseId : person.technicianId}"/>
                            <option value="${personId}" ${personId == selectedProfileId ? 'selected' : ''}>
                                <c:out value="${person.fullName}"/> (<c:out value="${scheduleRole == 'STAFF' ? person.staffCode : scheduleRole == 'NURSE' ? person.nurseCode : person.technicianCode}"/>)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group"><label for="fromDate">Từ ngày</label><input id="fromDate" type="date" name="fromDate" value="${fromDate}" required></div>
                <div class="form-group"><label for="toDate">Đến ngày</label><input id="toDate" type="date" name="toDate" value="${toDate}" required></div>
                <div class="form-group"><label for="room">${scheduleRole == 'STAFF' ? 'Quầy / nơi tiếp nhận' : 'Phòng làm việc'}</label><input id="room" name="room" maxlength="100" required></div>
            </div>
            <fieldset class="schedule-choice-group"><legend>Thứ trong tuần</legend>
                <label><input type="checkbox" name="weekdays" value="MONDAY" checked>Thứ Hai</label>
                <label><input type="checkbox" name="weekdays" value="TUESDAY" checked>Thứ Ba</label>
                <label><input type="checkbox" name="weekdays" value="WEDNESDAY" checked>Thứ Tư</label>
                <label><input type="checkbox" name="weekdays" value="THURSDAY" checked>Thứ Năm</label>
                <label><input type="checkbox" name="weekdays" value="FRIDAY" checked>Thứ Sáu</label>
                <label><input type="checkbox" name="weekdays" value="SATURDAY">Thứ Bảy</label>
                <label><input type="checkbox" name="weekdays" value="SUNDAY">Chủ Nhật</label>
            </fieldset>
            <fieldset class="schedule-choice-group"><legend>Ca làm việc</legend>
                <label><input type="checkbox" name="shifts" value="MORNING" checked>Sáng: 07:00 – 11:30</label>
                <label><input type="checkbox" name="shifts" value="AFTERNOON" checked>Chiều: 13:00 – 16:00</label>
            </fieldset>
            <button class="btn btn-primary" type="submit" ${empty people ? 'disabled' : ''}>Phân lịch</button>
            <c:if test="${empty people}"><p>Chưa có nhân sự có tài khoản đang hoạt động.</p></c:if>
        </form>

        <form method="get" action="${ctx}${schedulePath}" class="filter-bar">
            <input type="date" name="fromDate" value="${fromDate}" aria-label="Lọc từ ngày" required>
            <input type="date" name="toDate" value="${toDate}" aria-label="Lọc đến ngày" required>
            <select name="profileId" aria-label="Lọc nhân sự"><option value="">Tất cả nhân sự</option>
                <c:forEach var="person" items="${people}">
                    <c:set var="personId" value="${scheduleRole == 'STAFF' ? person.staffId : scheduleRole == 'NURSE' ? person.nurseId : person.technicianId}"/>
                    <option value="${personId}" ${personId == selectedProfileId ? 'selected' : ''}><c:out value="${person.fullName}"/></option>
                </c:forEach>
            </select>
            <button class="btn btn-primary" type="submit">Xem lịch</button>
        </form>
        <section class="admin-card"><table class="data-table">
            <thead><tr><th>Nhân sự</th><th>Ngày làm việc</th><th>Bắt đầu</th><th>Kết thúc</th><th>${scheduleRole == 'STAFF' ? 'Nơi tiếp nhận' : 'Phòng'}</th></tr></thead>
            <tbody><c:forEach var="schedule" items="${schedules}"><tr>
                <td><c:out value="${schedule.fullName}"/></td><td><c:out value="${schedule.workDate}"/></td>
                <td><c:out value="${schedule.startTime}"/></td><td><c:out value="${schedule.endTime}"/></td><td><c:out value="${schedule.room}"/></td>
            </tr></c:forEach><c:if test="${empty schedules}"><tr><td colspan="5">Chưa có lịch trong khoảng ngày đã chọn.</td></tr></c:if></tbody>
        </table></section>
    </main>
</div></body></html>
