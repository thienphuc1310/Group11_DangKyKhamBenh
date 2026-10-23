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
    <title>Quản lý Bác Sĩ</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />
    <div class="content-area">

    <c:choose>
    <%-- ================= TRANG CHI TIẾT ================= --%>
    <c:when test="${not empty doctor}">
        <a class="btn btn-outline btn-sm" href="javascript:history.back()"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại</a>
        <h2>CHI TIẾT BÁC SĨ</h2>

        <div class="doctor-detail">

            <%-- Ô 1: Ảnh (tạm dùng chữ cái đầu của tên) --%>
            <div class="dd-box dd-photo">
                <div class="dd-avatar">${fn:substring(doctor.fullName, 0, 1)}</div>
            </div>

            <%-- Ô 2: Thông tin cá nhân --%>
            <div class="dd-box dd-info">
                <h3>Thông tin cá nhân</h3>
                <div class="detail-grid">
                    <span>Họ và tên</span>     <span><strong><c:out value="${doctor.fullName}"/></strong></span>
                    <span>Giới tính</span>     <span><c:out value="${doctor.gender}" default="—"/></span>
                    <span>Ngày sinh</span>
                    <span>
                        <c:out value="${doctor.user.dateOfBirth}" default="—"/>
                        <c:if test="${not empty age}"> (${age} tuổi)</c:if>
                    </span>
                    <span>Số điện thoại</span> <span><c:out value="${doctor.phone}" default="—"/></span>
                    <span>Email</span>         <span><c:out value="${doctor.email}" default="—"/></span>
                    <span>Địa chỉ</span>       <span><c:out value="${doctor.user.address}" default="—"/></span>
                </div>
            </div>

            <%-- Ô 3: Thông tin chuyên môn --%>
            <div class="dd-box dd-pro">
                <h3>Thông tin chuyên môn</h3>
                <div class="detail-grid">
                    <span>Mã bác sĩ</span>     <span>${doctor.doctorId} — <c:out value="${doctor.doctorCode}"/></span>
                    <span>Chuyên khoa</span>   <span><c:out value="${doctor.specialty.name}" default="Chưa phân công"/></span>
                    <span>Chức vị / Học vị</span> <span><c:out value="${doctor.qualification}" default="—"/></span>
                    <span>Kinh nghiệm</span>   <span>${empty doctor.experienceYears ? '—' : doctor.experienceYears} năm</span>
                    <span>Giấy phép hành nghề</span> <span><c:out value="${doctor.licenseNumber}" default="—"/></span>
                    <span>Giới thiệu</span>    <span><c:out value="${doctor.description}" default="—"/></span>
                </div>
            </div>

            <%-- Ô 4: Thông tin tài khoản --%>
            <div class="dd-box dd-account">
                <h3>Tài khoản</h3>
                <div class="detail-grid">
                    <span>Tên đăng nhập</span> <span><c:out value="${account.username}" default="Chưa có tài khoản"/></span>
                    <span>Trạng thái</span>
                    <span>
                        <c:choose>
                            <c:when test="${empty account}">—</c:when>
                            <c:when test="${account.status == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                            <c:otherwise><span class="badge badge-red">Ngừng hoạt động</span></c:otherwise>
                        </c:choose>
                    </span>
                    <span>Ngày tạo</span>
                    <span>${empty account.createdAt ? '—' : fn:substring(fn:replace(account.createdAt, 'T', ' '), 0, 16)}</span>
                    <span>Đăng nhập gần nhất</span>
                    <span>${empty account.lastLoginAt ? 'Chưa đăng nhập' : fn:substring(fn:replace(account.lastLoginAt, 'T', ' '), 0, 16)}</span>
                </div>
            </div>

        </div>
    </c:when>

    <%-- ================= TRANG DANH SÁCH ================= --%>
    <c:otherwise>
        <h2>DANH SÁCH BÁC SĨ</h2>

        <form id="admin-doctor-filter" class="filter-bar" method="get" action="${ctx}/admin/doctors">
            <input type="search" name="q" autocomplete="off" placeholder="Tìm theo tên hoặc mã bác sĩ..."
                   value="<c:out value='${param.q}'/>">
            <select name="specialtyId">
                <option value="">-- Tất cả chuyên khoa --</option>
                <c:forEach var="s" items="${specialties}">
                    <option value="${s.specialtyId}" ${s.specialtyId == selectedSpecialtyId ? 'selected' : ''}>
                        <c:out value="${s.name}"/>
                    </option>
                </c:forEach>
            </select>
            <select name="gender">
                <option value="">-- Tất cả giới tính --</option>
                <option value="nam" ${param.gender == 'nam' ? 'selected' : ''}>Nam</option>
                <option value="nu"  ${param.gender == 'nu'  ? 'selected' : ''}>Nữ</option>
            </select>
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            <a class="btn btn-outline" href="${ctx}/admin/doctors">Xoá lọc</a>
        </form>
        <script>
            document.querySelectorAll('#admin-doctor-filter select').forEach(function (sel) {
                sel.addEventListener('change', function () { this.form.submit(); });
            });
        </script>

        <c:choose>
        <c:when test="${not searched}">
            <div class="admin-card empty-hint">Nhập tên, mã hoặc chọn bộ lọc rồi bấm Tìm kiếm (để trống để xem tất cả).</div>
        </c:when>
        <c:otherwise>
        <div class="admin-card">
        <table class="data-table">
            <thead>
                <tr><th>Mã BS</th><th>Họ và Tên</th><th>Giới Tính</th><th>Chuyên Khoa</th><th>Thao Tác</th></tr>
            </thead>
            <tbody>
            <c:forEach var="d" items="${doctors}">
                <c:set var="st" value="${statusMap[d.userId]}"/>
                <tr>
                    <td>${d.doctorId}</td>
                    <td><c:out value="${d.fullName}"/></td>
                    <td><c:out value="${d.gender}"/></td>
                    <td><c:out value="${d.specialty != null ? d.specialty.name : 'Chưa phân công'}"/></td>
                    <td class="row-actions">
                        <c:if test="${isAdmin and not empty st}">
                            <form method="post" action="${ctx}/admin/doctors">
                                <input type="hidden" name="userId" value="${d.userId}">
                                <input type="hidden" name="back" value="<c:out value='${back}'/>">
                                <button class="btn btn-sm btn-blue" onclick="return confirm('Xác nhận thay đổi trạng thái tài khoản?')">
                                    ${st == 'ACTIVE' ? 'Khoá' : 'Mở khoá'}
                                </button>
                            </form>
                        </c:if>
                        <a class="btn btn-sm btn-red" href="${ctx}/admin/doctors?id=${d.doctorId}">Xem chi tiết</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty doctors}">
                <tr><td colspan="5" style="text-align:center;">Không tìm thấy bác sĩ phù hợp.</td></tr>
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
