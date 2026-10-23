<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx"     value="${pageContext.request.contextPath}"/>
<c:set var="back"    value="?${pageContext.request.queryString}"/>
<c:set var="isAdmin" value="${sessionScope.role == 'ADMIN'}"/>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Bệnh Nhân</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />
    <div class="content-area">

    <c:choose>
    <%-- ================= TRANG CHI TIẾT ================= --%>
    <c:when test="${not empty patient}">
        <a class="btn btn-outline btn-sm" href="javascript:history.back()"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại</a>
        <h2>CHI TIẾT BỆNH NHÂN</h2>

        <div class="admin-card detail-card">
            <div class="detail-grid">
                <span>Mã bệnh nhân</span>      <span>${patient.patientId}</span>
                <span>Mã hồ sơ</span>          <span><c:out value="${patient.patientCode}"/></span>
                <span>Họ và tên</span>         <span><c:out value="${patient.fullName}"/></span>
                <span>Ngày sinh</span>         <span><c:out value="${patient.dateOfBirth}" default="—"/></span>
                <span>Giới tính</span>         <span><c:out value="${patient.gender}" default="—"/></span>
                <span>Số điện thoại</span>     <span><c:out value="${patient.phone}" default="—"/></span>
                <span>Email</span>             <span><c:out value="${patient.email}" default="—"/></span>
                <span>Địa chỉ</span>           <span><c:out value="${patient.address}" default="—"/></span>
                <span>Tên đăng nhập</span>     <span><c:out value="${account.username}" default="Chưa có tài khoản"/></span>
                <span>Trạng thái</span>
                <span>
                    <c:choose>
                        <c:when test="${empty account}">—</c:when>
                        <c:when test="${account.status == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                        <c:otherwise><span class="badge badge-red">Ngừng hoạt động</span></c:otherwise>
                    </c:choose>
                </span>
                <span>Đăng nhập gần nhất</span>
                <span>${empty account.lastLoginAt ? 'Chưa đăng nhập' : fn:substring(fn:replace(account.lastLoginAt, 'T', ' '), 0, 16)}</span>
            </div>
        </div>
    </c:when>

    <%-- ================= TRANG DANH SÁCH ================= --%>
    <c:otherwise>
        <h2>DANH SÁCH BỆNH NHÂN</h2>

        <form id="admin-patient-filter" class="filter-bar" method="get" action="${ctx}/admin/users">
            <input type="search" name="q" autocomplete="off"
                   placeholder="Tìm theo mã, tên, số điện thoại hoặc email..."
                   value="<c:out value='${param.q}'/>">
            <select name="gender">
                <option value="">-- Tất cả giới tính --</option>
                <option value="nam" ${param.gender == 'nam' ? 'selected' : ''}>Nam</option>
                <option value="nu"  ${param.gender == 'nu'  ? 'selected' : ''}>Nữ</option>
            </select>
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            <a class="btn btn-outline" href="${ctx}/admin/users">Xoá lọc</a>
        </form>
        <script>
            document.querySelectorAll('#admin-patient-filter select').forEach(function (sel) {
                sel.addEventListener('change', function () { this.form.submit(); });
            });
        </script>

        <c:choose>
        <c:when test="${not searched}">
            <div class="admin-card empty-hint">Nhập thông tin hoặc chọn giới tính rồi bấm Tìm kiếm (để trống để xem tất cả).</div>
        </c:when>
        <c:otherwise>
        <div class="admin-card">
        <table class="data-table">
            <thead>
                <tr><th>Mã BN</th><th>Họ và Tên</th><th>Giới Tính</th><th>Số Điện Thoại</th><th>Thao Tác</th></tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${patients}">
                <c:set var="st" value="${statusMap[p.userId]}"/>
                <tr>
                    <td>${p.patientId}</td>
                    <td><c:out value="${p.fullName}"/></td>
                    <td><c:out value="${p.gender}" default="—"/></td>
                    <td><c:out value="${p.phone}" default="—"/></td>
                    <td class="row-actions">
                        <c:if test="${isAdmin and not empty st}">
                            <form method="post" action="${ctx}/admin/users">
                                <input type="hidden" name="userId" value="${p.userId}">
                                <input type="hidden" name="back" value="<c:out value='${back}'/>">
                                <button class="btn btn-sm btn-blue" onclick="return confirm('Xác nhận thay đổi trạng thái tài khoản?')">
                                    ${st == 'ACTIVE' ? 'Khoá' : 'Mở khoá'}
                                </button>
                            </form>
                        </c:if>
                        <a class="btn btn-sm btn-red" href="${ctx}/admin/users?id=${p.patientId}">Xem chi tiết</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty patients}">
                <tr><td colspan="5" style="text-align:center;">Không tìm thấy bệnh nhân phù hợp.</td></tr>
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
