<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/includes/header-clinical.jsp">
    <jsp:param name="pageTitle" value="Đánh giá ban đầu"/>
    <jsp:param name="activeMenu" value="assessment"/>
</jsp:include>

<main class="doctor-page">
    <h1>Đánh giá ban đầu</h1>

    <c:if test="${param.completed == '1'}">
        <div class="success-message" role="status">
            Đã hoàn tất đánh giá và chuyển bệnh nhân sang chờ bác sĩ.
        </div>
    </c:if>

    <section class="doctor-card">
    <form method="get" action="${ctx}/nurse/assessment" class="filter-form care-filter">
        <div class="form-group">
            <label for="workDate">Ngày tiếp nhận</label>
            <input id="workDate" type="date" name="workDate" value="${selectedDate}" required>
        </div>
        <div class="record-filter-actions">
            <button class="save-button" type="submit">Xem danh sách</button>
            <a class="btn btn-outline" href="${ctx}/nurse/assessment">Về hôm nay</a>
        </div>
    </form>

    <div class="table-scroll">
        <table class="care-table">
            <thead>
            <tr>
                <th>Mã lượt</th>
                <th>Bệnh nhân</th>
                <th>Bác sĩ</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="e" items="${encounters}">
                <c:url var="detailUrl" value="/nurse/assessment">
                    <c:param name="encounterId" value="${e.encounterId}"/>
                    <c:param name="workDate" value="${selectedDate}"/>
                </c:url>

                <tr>
                    <td><c:out value="${e.encounterCode}"/></td>
                    <td><c:out value="${e.appointment.patient.user.fullName}"/></td>
                    <td><c:out value="${e.doctor.user.fullName}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${e.status == 'ASSESSING'}">
                                <span class="status care-assessing">Đang đánh giá</span>
                            </c:when>
                            <c:otherwise><span class="status waiting">Chờ đánh giá</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="btn btn-outline" href="${detailUrl}">
                            Mở phiếu
                        </a>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${empty encounters}">
                <tr>
                    <td colspan="5" class="empty-state">
                        Không có bệnh nhân phù hợp trong ngày đã chọn.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
    </section>
</main>
<jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/>
