<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Duyệt nghỉ bác sĩ - UCare</title></head><body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"><jsp:param name="activeMenu" value="doctor-leaves"/></jsp:include>
    <main class="content-area">
        <h2>ĐƠN NGHỈ BÁC SĨ</h2>
        <c:if test="${param.saved == '1'}"><p class="alert alert-success" role="status">Đã cập nhật đơn nghỉ.</p></c:if>
        <div class="admin-card" style="overflow-x:auto"><table class="data-table">
            <thead><tr><th>Bác sĩ</th><th>Chuyên khoa</th><th>Ngày / giờ</th><th>Lý do</th><th>Trạng thái</th><th>Người thay</th><th>Duyệt</th></tr></thead>
            <tbody><c:forEach var="leave" items="${leaves}"><tr>
                <td><c:out value="${leave.schedule.doctor.fullName}"/></td><td><c:out value="${leave.schedule.doctor.specialty.name}"/></td>
                <td>${leave.schedule.workDate}<br>${leave.schedule.startTime} – ${leave.schedule.endTime}</td>
                <td style="max-width:280px;overflow-wrap:anywhere"><c:out value="${leave.reason}"/></td>
                <td><jsp:include page="/WEB-INF/includes/leave-status.jsp"><jsp:param name="status" value="${leave.status}"/></jsp:include></td>
                <td><c:out value="${leave.replacementSchedule.doctor.fullName}" default="—"/></td>
                <td><c:if test="${leave.status == 'PENDING_APPROVAL'}">
                    <form method="post" action="${ctx}/admin/doctor-leaves" style="display:flex;gap:8px">
                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="leaveId" value="${leave.leaveRequestId}">
                        <button class="btn btn-primary btn-sm" name="action" value="approve">Duyệt</button>
                        <button class="btn btn-outline btn-sm" name="action" value="reject">Từ chối</button>
                    </form>
                </c:if></td>
            </tr></c:forEach><c:if test="${empty leaves}"><tr><td colspan="7">Chưa có đơn nghỉ.</td></tr></c:if></tbody>
        </table></div>
    </main>
</div></body></html>
