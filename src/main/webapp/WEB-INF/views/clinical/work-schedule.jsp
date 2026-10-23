<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-clinical.jsp">
    <jsp:param name="pageTitle" value="Lịch làm việc"/>
    <jsp:param name="activeMenu" value="schedule"/>
</jsp:include>
<main class="doctor-page">
    <h1>Quản lý lịch làm việc</h1>

    <%-- Dùng chung kiểu thanh công cụ và bảng tuần với trang bác sĩ. --%>
    <div class="schedule-toolbar staff-schedule-toolbar">
        <div class="toolbar-group">
            <a class="btn btn-outline" href="${ctx}${schedulePath}">Hôm nay</a>
            <a class="btn btn-outline" href="${ctx}${schedulePath}?date=${previousDate}" aria-label="Tuần trước"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-left.png" alt="" aria-hidden="true"></a>
            <a class="btn btn-outline" href="${ctx}${schedulePath}?date=${nextDate}" aria-label="Tuần sau"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/chevron-right.png" alt="" aria-hidden="true"></a>
            <form method="get" action="${ctx}${schedulePath}" class="staff-schedule-date">
                <label for="staff-schedule-date" class="visually-hidden">Chọn ngày trong tuần</label>
                <input id="staff-schedule-date" type="date" name="date" value="${selectedDate}" required>
                <button type="submit">Xem tuần</button>
            </form>
            <strong aria-live="polite"><c:out value="${weekStart.dayOfMonth}"/>/<c:out value="${weekStart.monthValue}"/>/<c:out value="${weekStart.year}"/>
                – <c:out value="${weekEnd.dayOfMonth}"/>/<c:out value="${weekEnd.monthValue}"/>/<c:out value="${weekEnd.year}"/></strong>
        </div>
    </div>

    <section class="doctor-card table-scroll" aria-label="Lịch làm việc trong tuần">
        <table class="schedule-table staff-schedule-table">
            <thead>
                <tr>
                    <th scope="col">Ca làm việc</th>
                    <c:forEach var="day" items="${days}" varStatus="index">
                        <th scope="col">
                            <c:out value="${dayLabels[index.index]}"/>
                            <span class="day-date"><c:out value="${day.dayOfMonth}"/>/<c:out value="${day.monthValue}"/></span>
                        </th>
                    </c:forEach>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th scope="row">Ca làm việc</th>
                    <c:forEach var="day" items="${days}">
                        <td>
                            <c:forEach var="shift" items="${schedulesByDate[day]}">
                                <div class="shift-card">
                                    <span><c:out value="${shift.startTime}"/> – <c:out value="${shift.endTime}"/></span>
                                    <span>${schedulePath == '/staff/schedule' ? 'Nơi tiếp nhận' : 'Phòng'} <c:out value="${shift.room}"/></span>
                                </div>
                            </c:forEach>
                        </td>
                    </c:forEach>
                </tr>
            </tbody>
        </table>
        <c:if test="${not hasSchedules}">
            <p id="schedule-empty">Chưa có ca làm việc trong tuần này.</p>
        </c:if>
    </section>
</main>
<jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
