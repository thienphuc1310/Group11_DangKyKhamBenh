<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx"  value="${pageContext.request.contextPath}"/>
<c:set var="back" value="?${pageContext.request.queryString}"/>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Nhân viên</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"><jsp:param name="activeMenu" value="staff"/></jsp:include>
    <div class="content-area">

    <c:choose>
    <%-- ================= TRANG CHI TIẾT ================= --%>
    <c:when test="${not empty staff}">
        <a class="btn btn-outline btn-sm" href="javascript:history.back()"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại</a>
        <h2>CHI TIẾT NHÂN VIÊN</h2>

        <div class="admin-card detail-card">
            <div class="detail-grid">
                <span>Mã nhân viên</span>      <span><c:out value="${not empty staffProfiles[staff.user.userId] ? staffProfiles[staff.user.userId].staffCode : staff.accountId}"/></span>
                <span>Họ và tên</span>         <span><c:out value="${staff.user.fullName}"/></span>
                <span>Tên đăng nhập</span>     <span><c:out value="${staff.username}"/></span>
                <span>Vai trò</span>           <span>${staff.role == 'ADMIN' ? 'Quản trị viên' : staffProfiles[staff.user.userId].jobType == 'RECEPTIONIST' ? 'Tiếp tân' : staffProfiles[staff.user.userId].jobType == 'CASHIER' ? 'Thu ngân' : 'Nhân viên'}</span>
                <span>Email</span>             <span><c:out value="${staff.user.email}" default="—"/></span>
                <span>Số điện thoại</span>     <span><c:out value="${staff.user.phone}" default="—"/></span>
                <span>Trạng thái</span>
                <span>
                    <c:choose>
                        <c:when test="${staff.status == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                        <c:otherwise><span class="badge badge-red">Ngừng hoạt động</span></c:otherwise>
                    </c:choose>
                </span>
                <span>Ngày tạo</span>          <span>${fn:substring(fn:replace(staff.createdAt, 'T', ' '), 0, 16)}</span>
                <span>Đăng nhập gần nhất</span>
                <span>${empty staff.lastLoginAt ? 'Chưa đăng nhập' : fn:substring(fn:replace(staff.lastLoginAt, 'T', ' '), 0, 16)}</span>
            </div>
        </div>
    </c:when>

    <%-- ================= TRANG DANH SÁCH ================= --%>
    <c:otherwise>
        <h2>DANH SÁCH NHÂN VIÊN</h2>

        <form class="filter-bar" method="get" action="${ctx}/admin/admins">
            <input type="search" name="q" autocomplete="off" placeholder="Tìm theo mã hoặc tên nhân viên..."
                   value="<c:out value='${param.q}'/>">
            <select name="role">
                <option value="">-- Tất cả vai trò --</option>
                <option value="staff" ${param.role == 'staff' ? 'selected' : ''}>Nhân viên</option>
                <option value="receptionist" ${param.role == 'receptionist' ? 'selected' : ''}>Tiếp tân</option>
                <option value="admin" ${param.role == 'admin' ? 'selected' : ''}>Quản trị viên</option>
            </select>
            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="active"   ${param.status == 'active'   ? 'selected' : ''}>Đang hoạt động</option>
                <option value="inactive" ${param.status == 'inactive' ? 'selected' : ''}>Ngừng hoạt động</option>
            </select>
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            <a class="btn btn-outline" href="${ctx}/admin/admins">Xoá lọc</a>
        </form>

        <div class="admin-card">
        <table class="data-table">
            <thead>
                <tr><th>Mã NV</th><th>Họ và Tên</th><th>Vai Trò</th><th>Trạng Thái</th><th>Thao Tác</th></tr>
            </thead>
            <tbody>
            <c:forEach var="a" items="${staffList}">
                <tr>
                    <td><c:out value="${not empty staffProfiles[a.user.userId] ? staffProfiles[a.user.userId].staffCode : a.accountId}"/></td>
                    <td><c:out value="${a.user.fullName}"/></td>
                    <td>${a.role == 'ADMIN' ? 'Quản trị viên' : staffProfiles[a.user.userId].jobType == 'RECEPTIONIST' ? 'Tiếp tân' : staffProfiles[a.user.userId].jobType == 'CASHIER' ? 'Thu ngân' : 'Nhân viên'}</td>
                    <td>
                        <c:choose>
                            <c:when test="${a.status == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                            <c:otherwise><span class="badge badge-red">Ngừng hoạt động</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="row-actions">
                        <c:if test="${a.user.userId != sessionScope.userId}">
                            <form method="post" action="${ctx}/admin/admins">
                                <input type="hidden" name="userId" value="${a.user.userId}">
                                <input type="hidden" name="back" value="<c:out value='${back}'/>">
                                <button class="btn btn-sm btn-blue" onclick="return confirm('Xác nhận thay đổi trạng thái tài khoản?')">
                                    ${a.status == 'ACTIVE' ? 'Khoá' : 'Mở khoá'}
                                </button>
                            </form>
                        </c:if>
                        <a class="btn btn-sm btn-red" href="${ctx}/admin/admins?id=${a.user.userId}">Xem chi tiết</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty staffList}">
                <tr><td colspan="5" style="text-align:center;">Không tìm thấy nhân viên phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
        </div>
    </c:otherwise>
    </c:choose>

    </div>
</div>
</body>
</html>
