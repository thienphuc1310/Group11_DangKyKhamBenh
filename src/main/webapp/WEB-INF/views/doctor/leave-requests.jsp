<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="${replacementPage ? 'Nhận ca thay' : 'Xin nghỉ theo ca'}"/>
    <jsp:param name="activeMenu" value="${replacementPage ? 'replacements' : 'leave'}"/>
</jsp:include>
<main class="doctor-page clinical-workspace">
    <h1>${replacementPage ? 'Nhận ca thay' : 'Xin nghỉ theo ca'}</h1>
    <c:if test="${param.saved == '1'}"><p class="success-message" role="status">${replacementPage ? 'Đã nhận ca và chuyển lịch hẹn.' : 'Đã gửi đơn nghỉ.'}</p></c:if>
    <form method="post" action="${ctx}/doctor/${replacementPage ? 'replacements' : 'leave-requests'}" class="doctor-card clinical-card-body">
        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
        <h2>${replacementPage ? 'Ca cần người thay' : 'Chọn ca nghỉ'}</h2>
        <p class="muted">Có thể chọn hai ca cùng ngày.</p>
        <div class="table-scroll"><table class="doctor-table">
            <thead><tr><th>Chọn</th><th>Ngày</th><th>Giờ làm</th><th>Phòng</th><c:if test="${replacementPage}"><th>Bác sĩ xin nghỉ</th></c:if></tr></thead>
            <tbody>
            <c:choose>
                <c:when test="${replacementPage}">
                    <c:forEach var="leave" items="${offers}"><tr>
                        <td><input type="checkbox" name="leaveId" value="${leave.leaveRequestId}" aria-label="Chọn ca ${leave.schedule.workDate} ${leave.schedule.startTime}"></td>
                        <td>${leave.schedule.workDate}</td><td>${leave.schedule.startTime} – ${leave.schedule.endTime}</td><td><c:out value="${leave.schedule.room}"/></td>
                        <td><c:out value="${leave.schedule.doctor.fullName}"/></td>
                    </tr></c:forEach>
                    <c:if test="${empty offers}"><tr><td colspan="5">Không có ca thay phù hợp.</td></tr></c:if>
                </c:when>
                <c:otherwise>
                    <c:forEach var="schedule" items="${schedules}"><tr>
                        <td><input type="checkbox" name="scheduleId" value="${schedule.scheduleId}" aria-label="Chọn ca ${schedule.workDate} ${schedule.startTime}"></td>
                        <td>${schedule.workDate}</td><td>${schedule.startTime} – ${schedule.endTime}</td><td><c:out value="${schedule.room}"/></td>
                    </tr></c:forEach>
                    <c:if test="${empty schedules}"><tr><td colspan="4">Không có ca đủ điều kiện xin nghỉ.</td></tr></c:if>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table></div>
        <c:if test="${!replacementPage && not empty schedules}">
            <div class="form-group"><label for="reason">Lý do nghỉ</label><textarea id="reason" name="reason" maxlength="1000" rows="3" required></textarea></div>
        </c:if>
        <c:if test="${(replacementPage && not empty offers) || (!replacementPage && not empty schedules)}">
            <div class="form-actions"><button type="submit" class="save-button">${replacementPage ? 'Xác nhận nhận ca' : 'Gửi đơn nghỉ'}</button></div>
        </c:if>
    </form>
    <section class="doctor-card clinical-card-body">
        <h2>Lịch sử đơn nghỉ và ca đã nhận</h2>
        <div class="table-scroll"><table class="doctor-table">
            <thead><tr><th>Ngày / giờ</th><th>Bác sĩ xin nghỉ</th><th>Lý do</th><th>Trạng thái</th><th>Bác sĩ thay</th></tr></thead>
            <tbody><c:forEach var="leave" items="${leaves}"><tr>
                <td>${leave.schedule.workDate}<br>${leave.schedule.startTime} – ${leave.schedule.endTime}</td>
                <td><c:out value="${leave.schedule.doctor.fullName}"/></td><td class="leave-reason"><c:out value="${leave.reason}"/></td>
                <td><jsp:include page="/WEB-INF/includes/leave-status.jsp"><jsp:param name="status" value="${leave.status}"/></jsp:include></td>
                <td><c:out value="${leave.replacementSchedule.doctor.fullName}" default="—"/></td>
            </tr></c:forEach>
            <c:if test="${empty leaves}"><tr><td colspan="5">Chưa có dữ liệu.</td></tr></c:if></tbody>
        </table></div>
    </section>
</main>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
