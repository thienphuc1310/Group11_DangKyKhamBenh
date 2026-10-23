<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="isNurse" value="${directoryRole == 'NURSE'}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isNurse ? 'Quản lý điều dưỡng' : 'Quản lý chuyên viên'} - UCare</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"><jsp:param name="activeMenu" value="${isNurse ? 'nurses' : 'technicians'}"/></jsp:include>
    <main class="content-area">
        <c:choose>
        <c:when test="${not empty person}">
            <a class="btn btn-outline btn-sm" href="${ctx}${directoryPath}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Quay lại danh sách</a>
            <h2>${isNurse ? 'CHI TIẾT ĐIỀU DƯỠNG' : 'CHI TIẾT CHUYÊN VIÊN'}</h2>
            <section class="admin-card detail-card">
                <div class="detail-grid">
                    <span>${isNurse ? 'Mã điều dưỡng' : 'Mã chuyên viên'}</span><span><c:out value="${isNurse ? person.nurseCode : person.technicianCode}"/></span>
                    <span>Họ và tên</span><span><c:out value="${person.fullName}"/></span>
                    <span>${isNurse ? 'Nhóm nghề' : 'Chuyên khoa'}</span>
                    <span><c:choose><c:when test="${isNurse}">${person.professionType == 'MIDWIFE' ? 'Hộ sinh' : 'Điều dưỡng'}</c:when><c:otherwise><c:out value="${person.specialty.name}"/></c:otherwise></c:choose></span>
                    <span>Ngày sinh</span><span><c:out value="${person.user.dateOfBirth}" default="—"/></span>
                    <span>Giới tính</span><span><c:out value="${person.user.gender}" default="—"/></span>
                    <span>Số điện thoại</span><span><c:out value="${person.phone}" default="—"/></span>
                    <span>Email</span><span><c:out value="${person.email}" default="—"/></span>
                    <span>Địa chỉ</span><span><c:out value="${person.user.address}" default="—"/></span>
                    <span>Tên đăng nhập</span><span><c:out value="${account.username}" default="—"/></span>
                    <span>Trạng thái tài khoản</span>
                    <span><c:choose>
                        <c:when test="${account.status == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                        <c:when test="${not empty account}"><span class="badge badge-red">Ngừng hoạt động</span></c:when>
                        <c:otherwise>Chưa có tài khoản</c:otherwise>
                    </c:choose></span>
                </div>
            </section>
        </c:when>
        <c:otherwise>
            <h2><c:out value="${directoryTitle}"/></h2>
            <form class="filter-bar" method="get" action="${ctx}${directoryPath}">
                <input type="search" name="q" value="<c:out value='${param.q}'/>" autocomplete="off" placeholder="Tìm theo mã hoặc họ tên...">
                <c:choose>
                    <c:when test="${isNurse}">
                        <select name="profession" aria-label="Nhóm nghề">
                            <option value="">-- Tất cả nhóm nghề --</option>
                            <option value="NURSE" ${param.profession == 'NURSE' ? 'selected' : ''}>Điều dưỡng</option>
                            <option value="MIDWIFE" ${param.profession == 'MIDWIFE' ? 'selected' : ''}>Hộ sinh</option>
                        </select>
                    </c:when>
                    <c:otherwise>
                        <select name="specialtyId" aria-label="Chuyên khoa">
                            <option value="">-- Tất cả chuyên khoa --</option>
                            <c:forEach var="sp" items="${specialties}"><c:if test="${sp.specialtyType == 'PARACLINICAL'}">
                                <option value="${sp.specialtyId}" ${sp.specialtyId == selectedSpecialtyId ? 'selected' : ''}><c:out value="${sp.name}"/></option>
                            </c:if></c:forEach>
                        </select>
                    </c:otherwise>
                </c:choose>
                <button class="btn btn-primary" type="submit">Tìm kiếm</button>
                <a class="btn btn-outline" href="${ctx}${directoryPath}">Xóa lọc</a>
            </form>
            <div class="admin-card">
                <table class="data-table">
                    <thead><tr><th>Mã</th><th>Họ và tên</th><th>${isNurse ? 'Nhóm nghề' : 'Chuyên khoa'}</th><th>Số điện thoại</th><th>Email</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                    <tbody>
                        <c:forEach var="p" items="${people}">
                            <tr>
                                <td><c:out value="${isNurse ? p.nurseCode : p.technicianCode}"/></td>
                                <td><c:out value="${p.fullName}"/></td>
                                <td><c:choose><c:when test="${isNurse}">${p.professionType == 'MIDWIFE' ? 'Hộ sinh' : 'Điều dưỡng'}</c:when><c:otherwise><c:out value="${p.specialty.name}"/></c:otherwise></c:choose></td>
                                <td><c:out value="${p.phone}" default="—"/></td>
                                <td><c:out value="${p.email}" default="—"/></td>
                                <td><c:choose>
                                    <c:when test="${statusMap[p.userId] == 'ACTIVE'}"><span class="badge badge-green">Đang hoạt động</span></c:when>
                                    <c:when test="${not empty statusMap[p.userId]}"><span class="badge badge-red">Ngừng hoạt động</span></c:when>
                                    <c:otherwise>Chưa có tài khoản</c:otherwise>
                                </c:choose></td>
                                <td><a class="btn btn-sm btn-blue" href="${ctx}${directoryPath}?id=${isNurse ? p.nurseId : p.technicianId}">Xem chi tiết</a></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty people}"><tr><td colspan="7" class="empty-hint">Không tìm thấy dữ liệu phù hợp.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>
