<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân lịch bác sĩ - UCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css?v=20261023-7">
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"/>

    <main class="content-area">
        <h1>Phân lịch làm việc cho bác sĩ</h1>

        <c:if test="${not empty requestScope.error}">
            <p class="alert alert-fail"><c:out value="${requestScope.error}"/></p>
        </c:if>
        <c:if test="${not empty param.created}">
            <p class="alert alert-ok">
                Đã tạo <c:out value="${param.created}"/> ca làm việc;
                bỏ qua <c:out value="${param.skipped}"/> ca bị trùng.
            </p>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/doctor-schedules"
              class="admin-form schedule-assignment-form">
            <div class="form-group">
                <label for="doctorId">Bác sĩ</label>
                <select id="doctorId" name="doctorId" required>
                    <option value="">-- Chọn bác sĩ --</option>
                    <c:forEach var="doctor" items="${requestScope.doctors}">
                        <option value="${doctor.doctorId}">
                            <c:out value="${doctor.doctorCode}"/> –
                            <c:out value="${doctor.fullName}"/> –
                            <c:out value="${doctor.specialty.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="schedule-form-grid">
                <div class="form-group">
                    <label for="fromDate">Từ ngày</label>
                    <input id="fromDate" type="date" name="fromDate" required>
                </div>
                <div class="form-group">
                    <label for="toDate">Đến ngày</label>
                    <input id="toDate" type="date" name="toDate" required>
                </div>
                <div class="form-group">
                    <label for="scheduleRoom">Phòng khám</label>
                    <input id="scheduleRoom" type="text" name="room" maxlength="100" required>
                </div>
            </div>

            <fieldset class="schedule-choice-group">
                <legend>Ngày làm việc trong tuần</legend>
                <label><input type="checkbox" name="weekdays" value="MONDAY"> Thứ Hai</label>
                <label><input type="checkbox" name="weekdays" value="TUESDAY"> Thứ Ba</label>
                <label><input type="checkbox" name="weekdays" value="WEDNESDAY"> Thứ Tư</label>
                <label><input type="checkbox" name="weekdays" value="THURSDAY"> Thứ Năm</label>
                <label><input type="checkbox" name="weekdays" value="FRIDAY"> Thứ Sáu</label>
                <label><input type="checkbox" name="weekdays" value="SATURDAY"> Thứ Bảy</label>
            </fieldset>

            <fieldset class="schedule-choice-group">
                <legend>Ca làm việc</legend>
                <label><input type="checkbox" name="shifts" value="MORNING"> Ca sáng: 07:00–11:30</label>
                <label><input type="checkbox" name="shifts" value="AFTERNOON"> Ca chiều: 13:00–16:00</label>
            </fieldset>

            <button type="submit" class="btn btn-primary">Tạo lịch làm việc</button>
        </form>

        <section class="admin-card">
            <div class="card-head">
                <div>
                    <h2>Danh sách lịch</h2>
                </div>
            </div>
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Bác sĩ</th>
                    <th>Chuyên khoa</th>
                    <th>Ngày</th>
                    <th>Bắt đầu</th>
                    <th>Kết thúc</th>
                    <th>Phòng</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="schedule" items="${requestScope.schedules}">
                    <tr>
                        <td><c:out value="${schedule.scheduleId}"/></td>
                        <td><c:out value="${schedule.doctor.fullName}"/></td>
                        <td><c:out value="${schedule.doctor.specialty.name}"/></td>
                        <td><c:out value="${schedule.workDate}"/></td>
                        <td><c:out value="${schedule.startTime}"/></td>
                        <td><c:out value="${schedule.endTime}"/></td>
                        <td><c:out value="${schedule.room}"/></td>
                        <td><c:choose>
                            <c:when test="${schedule.status == 'ON_LEAVE'}">Đã nghỉ · Chờ người thay</c:when>
                            <c:when test="${schedule.status == 'REPLACED'}">Đã bàn giao</c:when>
                            <c:otherwise>Đang làm việc</c:otherwise>
                        </c:choose></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty requestScope.schedules}">
                    <tr><td colspan="8" class="empty">Chưa có lịch làm việc.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</div>
</body>
</html>
