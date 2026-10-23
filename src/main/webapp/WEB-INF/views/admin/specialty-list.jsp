<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Chuyên Khoa</title>
</head>
<body>
    <div class="main-layout">
        <jsp:include page="/WEB-INF/includes/sidebar.jsp" />

        <div class="content-area">
            <h2>DANH SÁCH CHUYÊN KHOA</h2>
            <p class="content-subtitle">Chỉ đổi loại chuyên khoa chưa có nhân sự hoặc dữ liệu khám, xét nghiệm.</p>

            <c:if test="${not empty requestScope.error}">
                <p class="alert alert-fail"><c:out value="${requestScope.error}"/></p>
            </c:if>
            <c:if test="${requestScope.updated}">
                <p class="alert alert-ok">Đã cập nhật loại chuyên khoa.</p>
            </c:if>
            <c:if test="${requestScope.created}">
                <p class="alert alert-ok" role="status">Đã tạo chuyên khoa mới.</p>
            </c:if>

            <c:if test="${sessionScope.role == 'ADMIN'}">
                <section id="specialty-create" class="admin-card specialty-create-card">
                    <h3>Tạo chuyên khoa</h3>
                    <form class="admin-form" method="post" action="${pageContext.request.contextPath}/admin/specialties">
                        <input type="hidden" name="action" value="create">
                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                        <div class="schedule-form-grid">
                            <div class="form-group">
                                <label for="specialty-name">Tên chuyên khoa</label>
                                <input id="specialty-name" name="name" maxlength="255"
                                       value="<c:out value='${param.action == "create" ? param.name : ""}'/>" required>
                            </div>
                            <div class="form-group">
                                <label for="specialty-type">Loại chuyên khoa</label>
                                <select id="specialty-type" name="specialtyType" required>
                                    <option value="">-- Chọn loại chuyên khoa --</option>
                                    <option value="CLINICAL" ${param.action == 'create' && param.specialtyType == 'CLINICAL' ? 'selected' : ''}>Lâm sàng</option>
                                    <option value="PARACLINICAL" ${param.action == 'create' && param.specialtyType == 'PARACLINICAL' ? 'selected' : ''}>Cận lâm sàng</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="specialty-description">Mô tả</label>
                            <textarea id="specialty-description" name="description" rows="3"><c:out value="${param.action == 'create' ? param.description : ''}"/></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary">Tạo chuyên khoa</button>
                    </form>
                </section>
            </c:if>

            <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/specialties">
                <input type="search" name="q" value="<c:out value='${keyword}'/>"
                       placeholder="Tìm theo mã hoặc tên chuyên khoa..." autocomplete="off">
                <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                <a class="btn btn-outline" href="${pageContext.request.contextPath}/admin/specialties">Xóa lọc</a>
            </form>
            <div class="admin-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Mã Chuyên Khoa</th>
                        <th>Tên Chuyên Khoa</th>
                        <th>Mô Tả</th>
                        <th>Loại</th>
                        <th>Phân loại</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="s" items="${requestScope.specialties}">
                        <tr>
                            <td><c:out value="${s.specialtyId}"/></td>
                            <td><c:out value="${s.name}"/></td>
                            <td><c:out value="${s.description}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${s.specialtyType == 'PARACLINICAL'}">
                                        <span class="badge badge-orange">Cận lâm sàng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-green">Lâm sàng</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${sessionScope.role == 'ADMIN'}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/specialties"
                                              style="display:flex; gap:6px; align-items:center;">
                                            <input type="hidden" name="specialtyId" value="${s.specialtyId}">
                                            <input type="hidden" name="action" value="updateType">
                                            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                            <select name="specialtyType">
                                                <option value="CLINICAL" ${s.specialtyType == 'CLINICAL' ? 'selected' : ''}>Lâm sàng</option>
                                                <option value="PARACLINICAL" ${s.specialtyType == 'PARACLINICAL' ? 'selected' : ''}>Cận lâm sàng</option>
                                            </select>
                                            <button type="submit" class="btn btn-blue btn-sm">Lưu</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty specialties}">
                        <tr>
                            <td colspan="5" style="text-align:center;">Chưa có dữ liệu chuyên khoa.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
            </div>
        </div>
    </div>
</body>
</html>
