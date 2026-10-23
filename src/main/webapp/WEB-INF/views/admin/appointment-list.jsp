<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx"  value="${pageContext.request.contextPath}"/>
<c:set var="back" value="?${pageContext.request.queryString}"/>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Lịch Khám</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />
    <div class="content-area">

    <c:choose>
    <%-- ================= TRANG CHI TIẾT ================= --%>
    <c:when test="${not empty appointment}">
        <c:set var="ap" value="${appointment}"/>
        <a class="btn btn-outline btn-sm" href="javascript:history.back()"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại</a>
        <h2>CHI TIẾT LỊCH KHÁM</h2>

        <div class="detail-2col">
            <%-- Ô 1: Lịch hẹn --%>
            <div class="dd-box">
                <h3>Thông tin lịch hẹn</h3>
                <div class="detail-grid">
                    <span>Mã lịch hẹn</span> <span><strong><c:out value="${ap.appointmentCode}"/></strong></span>
                    <span>Ngày khám</span>   <span>${ap.schedule.workDate}</span>
                    <span>Giờ khám</span>
                    <span>${not empty ap.slot ? ap.slot.startTime : ap.schedule.startTime} – ${not empty ap.slot ? ap.slot.endTime : ap.schedule.endTime}</span>
                    <span>Phòng</span>       <span><c:out value="${ap.schedule.room}" default="—"/></span>
                    <span>Lý do khám</span>  <span><c:out value="${ap.reason}" default="—"/></span>
                    <span>Phí khám</span>    <span><fmt:formatNumber value="${ap.fee}" type="number" groupingUsed="true"/> đ</span>
                    <span>Trạng thái</span>
                    <span>
                        <c:choose>
                            <c:when test="${ap.status == 'PENDING'}"><span class="badge badge-orange">Chờ xác nhận</span></c:when>
                            <c:when test="${ap.status == 'CONFIRMED'}"><span class="badge badge-green">Đã xác nhận</span></c:when>
                            <c:when test="${ap.status == 'COMPLETED'}"><span class="badge badge-gray">Đã khám</span></c:when>
                            <c:otherwise><span class="badge badge-red">Đã huỷ</span></c:otherwise>
                        </c:choose>
                    </span>
                    <span>Ngày đặt</span>    <span>${fn:substring(fn:replace(ap.createdAt, 'T', ' '), 0, 16)}</span>
                </div>
            </div>

            <%-- Ô 2: Bệnh nhân --%>
            <div class="dd-box">
                <h3>Bệnh nhân</h3>
                <div class="detail-grid">
                    <span>Họ và tên</span>     <span><c:out value="${ap.patient.fullName}"/></span>
                    <span>Giới tính</span>     <span><c:out value="${ap.patient.gender}" default="—"/></span>
                    <span>Số điện thoại</span> <span><c:out value="${ap.patient.phone}" default="—"/></span>
                    <span>Email</span>         <span><c:out value="${ap.patient.email}" default="—"/></span>
                </div>
                <div class="detail-actions">
                    <a class="btn btn-sm btn-red" href="${ctx}/admin/users?id=${ap.patient.patientId}">Xem hồ sơ bệnh nhân</a>
                </div>
            </div>

            <%-- Ô 3: Bác sĩ --%>
            <div class="dd-box">
                <h3>Bác sĩ phụ trách</h3>
                <div class="detail-grid">
                    <span>Họ và tên</span>   <span><c:out value="${ap.schedule.doctor.fullName}"/></span>
                    <span>Chuyên khoa</span> <span><c:out value="${ap.schedule.doctor.specialty.name}" default="—"/></span>
                    <span>Học vị</span>      <span><c:out value="${ap.schedule.doctor.qualification}" default="—"/></span>
                </div>
                <div class="detail-actions">
                    <a class="btn btn-sm btn-red" href="${ctx}/admin/doctors?id=${ap.schedule.doctor.doctorId}">Xem hồ sơ bác sĩ</a>
                </div>
            </div>

            <%-- Ô 4: Thanh toán --%>
            <div class="dd-box">
                <h3>Thanh toán</h3>
                <c:choose>
                    <c:when test="${empty payment}">
                        <p style="color:#777;">Chưa có giao dịch thanh toán.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="detail-grid">
                            <span>Mã giao dịch</span> <span><c:out value="${payment.transactionCode}"/></span>
                            <span>Số tiền</span>      <span><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true"/> đ</span>
                            <span>Phương thức</span>
                            <span>
                                <c:choose>
                                    <c:when test="${payment.method == 'CASH'}">Tiền mặt</c:when>
                                    <c:when test="${payment.method == 'BANK_TRANSFER'}">Chuyển khoản</c:when>
                                    <c:otherwise>${payment.method}</c:otherwise>
                                </c:choose>
                            </span>
                            <span>Trạng thái</span>
                            <span>
                                <c:choose>
                                    <c:when test="${payment.status == 'PAID'}"><span class="badge badge-green">Đã thanh toán</span></c:when>
                                    <c:when test="${payment.status == 'REFUNDED'}"><span class="badge badge-gray">Đã hoàn tiền</span></c:when>
                                    <c:when test="${payment.status == 'FAILED'}"><span class="badge badge-red">Thất bại</span></c:when>
                                    <c:otherwise><span class="badge badge-orange">Chờ thanh toán</span></c:otherwise>
                                </c:choose>
                            </span>
                            <span>Thanh toán lúc</span>
                            <span>${empty payment.paidAt ? '—' : fn:substring(fn:replace(payment.paidAt, 'T', ' '), 0, 16)}</span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:when>

    <%-- ================= TRANG DANH SÁCH ================= --%>
    <c:otherwise>
        <h2>DANH SÁCH LỊCH KHÁM</h2>

        <form id="admin-appointment-filter" class="filter-bar" method="get" action="${ctx}/admin/appointments">
            <input type="search" name="q" autocomplete="off"
                   placeholder="Tìm theo mã lịch, tên / SĐT bệnh nhân, tên bác sĩ..."
                   value="<c:out value='${param.q}'/>">
            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="PENDING"   ${param.status == 'PENDING'   ? 'selected' : ''}>Chờ xác nhận</option>
                <option value="CONFIRMED" ${param.status == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                <option value="COMPLETED" ${param.status == 'COMPLETED' ? 'selected' : ''}>Đã khám</option>
                <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã huỷ</option>
            </select>
            <select name="specialtyId">
                <option value="">-- Tất cả chuyên khoa --</option>
                <c:forEach var="s" items="${specialties}">
                    <option value="${s.specialtyId}" ${s.specialtyId == selectedSpecialtyId ? 'selected' : ''}>
                        <c:out value="${s.name}"/>
                    </option>
                </c:forEach>
            </select>
            <input type="date" name="date" value="<c:out value='${param.date}'/>" style="flex:0; min-width:160px;">
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            <a class="btn btn-outline" href="${ctx}/admin/appointments">Xoá lọc</a>
        </form>
        <script>
            document.querySelectorAll('#admin-appointment-filter select, #admin-appointment-filter input[type=date]')
                .forEach(function (el) { el.addEventListener('change', function () { this.form.submit(); }); });
        </script>

        <c:choose>
        <c:when test="${not searched}">
            <div class="admin-card empty-hint">Nhập thông tin hoặc chọn bộ lọc rồi bấm Tìm kiếm (để trống để xem tất cả).</div>
        </c:when>
        <c:otherwise>
        <div class="admin-card">
        <table class="data-table">
            <thead>
                <tr><th>Mã Lịch</th><th>Bệnh Nhân</th><th>Bác Sĩ</th><th>Ngày Khám</th><th>Giờ</th><th>Trạng Thái</th><th>Thao Tác</th></tr>
            </thead>
            <tbody>
            <c:forEach var="ap" items="${appointments}">
                <tr>
                    <td><c:out value="${ap.appointmentCode}"/></td>
                    <td><c:out value="${ap.patient.fullName}"/></td>
                    <td><c:out value="${ap.schedule.doctor.fullName}"/></td>
                    <td>${ap.schedule.workDate}</td>
                    <td>${not empty ap.slot ? ap.slot.startTime : ap.schedule.startTime}</td>
                    <td>
                        <c:choose>
                            <c:when test="${ap.status == 'PENDING'}"><span class="badge badge-orange">Chờ xác nhận</span></c:when>
                            <c:when test="${ap.status == 'CONFIRMED'}"><span class="badge badge-green">Đã xác nhận</span></c:when>
                            <c:when test="${ap.status == 'COMPLETED'}"><span class="badge badge-gray">Đã khám</span></c:when>
                            <c:otherwise><span class="badge badge-red">Đã huỷ</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="row-actions">
                        <%-- Toan: Admin/staff chỉ xác nhận lịch PENDING; bác sĩ mới hoàn tất buổi khám. --%>
                        <c:if test="${ap.status == 'PENDING'}">
                            <form method="post" action="${ctx}/admin/appointments">
                                <input type="hidden" name="appointmentId" value="${ap.appointmentId}">
                                <input type="hidden" name="back" value="<c:out value='${back}'/>">
                                <input type="hidden" name="status" value="CONFIRMED">
                                <button class="btn btn-sm btn-blue">Xác nhận</button>
                            </form>
                        </c:if>
                        <c:if test="${ap.status == 'PENDING' or ap.status == 'CONFIRMED'}">
                            <form method="post" action="${ctx}/admin/appointments">
                                <input type="hidden" name="appointmentId" value="${ap.appointmentId}">
                                <input type="hidden" name="back" value="<c:out value='${back}'/>">
                                <input type="hidden" name="status" value="CANCELLED">
                                <button class="btn btn-sm btn-outline" onclick="return confirm('Huỷ lịch hẹn này?')">Huỷ</button>
                            </form>
                        </c:if>
                        <a class="btn btn-sm btn-red" href="${ctx}/admin/appointments?id=${ap.appointmentId}">Xem chi tiết</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty appointments}">
                <tr><td colspan="7" style="text-align:center;">Không tìm thấy lịch khám phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
        </div>
        </c:otherwise>
        </c:choose>
    </c:otherwise>
    </c:choose>

    </div>
</div>
</body>
</html>
