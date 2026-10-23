<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/includes/header-clinical.jsp">
    <jsp:param name="pageTitle" value="Tiếp nhận bệnh nhân"/>
    <jsp:param name="activeMenu" value="reception"/>
</jsp:include>
<main class="doctor-page">

    <header class="page-heading">
        <div>
            <h1>Tiếp nhận bệnh nhân</h1>
        </div>

    </header>

    <%-- Chỉ là thông báo giao diện sau redirect, không dùng để xác minh dữ liệu. --%>
    <c:if test="${param.received == '1'}">
        <div class="success-message" role="status">
            Yêu cầu tiếp nhận đã xử lý thành công.
            Lượt khám đã được ghi nhận và có số thứ tự.
        </div>
    </c:if>

    <section class="doctor-card" aria-label="Lịch khám chờ tiếp nhận">
        <form method="get" action="${ctx}/staff/reception"
              class="filter-form care-filter">

            <div class="form-group">
                <label for="workDate">Ngày khám</label>
                <input type="date"
                       id="workDate"
                       name="workDate"
                       value="<c:out value='${selectedDate}'/>"
                       required>
            </div>

            <div class="form-group search-field">
                <label for="keyword">Tìm bệnh nhân</label>
                <input type="search"
                       id="keyword"
                       name="q"
                       value="<c:out value='${keyword}'/>"
                       placeholder="Mã lịch, mã bệnh nhân, tên hoặc điện thoại">
            </div>

            <div class="record-filter-actions">
                <button type="submit" class="save-button">Tìm kiếm</button>
                <a href="${ctx}/staff/reception" class="btn btn-outline">Về hôm nay</a>
            </div>
        </form>

        <div class="table-scroll">
            <table class="care-table">
                <caption>
                    Lịch đã thanh toán và chưa tiếp nhận —
                    <c:out value="${selectedDate}"/>
                </caption>

                <thead>
                <tr>
                    <th scope="col">Mã lịch</th>
                    <th scope="col">Bệnh nhân</th>
                    <th scope="col">Bác sĩ / Phòng</th>
                    <th scope="col">Giờ khám</th>
                    <th scope="col">Thanh toán</th>
                    <th scope="col">Thao tác</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="a" items="${appointments}">
                    <tr>
                        <td>
                            <c:out value="${a.appointmentCode}"/>
                        </td>

                        <td>
                            <strong>
                                <c:out value="${a.patient.user.fullName}"/>
                            </strong>

                            <div class="muted">
                                Mã BN:
                                <c:out value="${a.patient.patientCode}"/>
                            </div>

                            <div class="muted">
                                Điện thoại:
                                <c:out value="${a.patient.user.phone}"
                                       default="Chưa có"/>
                            </div>
                        </td>

                        <td>
                            <c:out value="${a.schedule.doctor.user.fullName}"/>

                            <div class="muted">
                                Phòng:
                                <c:out value="${a.schedule.room}"/>
                            </div>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty a.slot}">
                                    <c:out value="${a.slot.startTime}"/>
                                    –
                                    <c:out value="${a.slot.endTime}"/>
                                </c:when>

                                <c:otherwise>
                                    <c:out value="${a.schedule.startTime}"/>
                                    –
                                    <c:out value="${a.schedule.endTime}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <span class="status completed">Đã trả phí khám</span>
                        </td>

                        <td>
                            <form method="post"
                                  action="${ctx}/staff/reception"
                                  onsubmit="return confirmArrival(this);">

                                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">

                                <input type="hidden"
                                       name="appointmentId"
                                       value="${a.appointmentId}">

                                <button type="submit" class="save-button" ${selectedDate != today ? 'disabled' : ''}>
                                    Xác nhận đã đến
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty appointments}">
                    <tr>
                        <td colspan="6" class="empty-state">
                            Không có lịch đủ điều kiện tiếp nhận
                            theo ngày và từ khóa đã chọn.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

</main>

<script>
    function confirmArrival(form) {
        if (!window.confirm(
                "Xác nhận bệnh nhân đã đến và cấp số thứ tự?")) {
            return false;
        }

        const button = form.querySelector('button[type="submit"]');
        button.disabled = true;
        button.textContent = "Đang tiếp nhận...";

        return true;
    }
</script>
<jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
