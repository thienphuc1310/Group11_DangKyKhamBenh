<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dịch vụ cận lâm sàng - UCare</title>
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"/>

    <main class="content-area">
        <h1>Dịch vụ cận lâm sàng</h1>

        <c:if test="${not empty requestScope.error}">
            <p class="alert alert-fail"><c:out value="${requestScope.error}"/></p>
        </c:if>
        <c:if test="${requestScope.created}">
            <p class="alert alert-ok">Đã tạo dịch vụ cận lâm sàng.</p>
        </c:if>
        <c:if test="${requestScope.updated}">
            <p class="alert alert-ok">Đã cập nhật trạng thái dịch vụ.</p>
        </c:if>

        <form method="post" action="${ctx}/admin/diagnostic-services" class="admin-form schedule-assignment-form">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <input type="hidden" name="action" value="create">
            <div class="schedule-form-grid">
                <div class="form-group">
                    <label for="serviceCode">Mã dịch vụ</label>
                    <input id="serviceCode" name="serviceCode" type="text" maxlength="100"
                           value="<c:out value='${param.serviceCode}'/>" required>
                </div>

                <div class="form-group">
                    <label for="serviceName">Tên dịch vụ</label>
                    <input id="serviceName" name="serviceName" type="text" maxlength="255"
                           value="<c:out value='${param.serviceName}'/>" required>
                </div>

                <div class="form-group">
                    <label for="amount">Giá dịch vụ (VNĐ)</label>
                    <input id="amount" name="amount" type="number" min="0.01" step="0.01"
                           value="<c:out value='${param.amount}'/>" required>
                </div>

                <div class="form-group">
                    <label for="serviceType">Loại dịch vụ</label>
                    <select id="serviceType" name="serviceType" required>
                        <option value="LABORATORY" ${param.serviceType == 'LABORATORY' ? 'selected' : ''}>LABORATORY (Xét nghiệm)</option>
                        <option value="IMAGING" ${param.serviceType == 'IMAGING' ? 'selected' : ''}>IMAGING (Chẩn đoán hình ảnh)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="specialtyId">Khoa cận lâm sàng thực hiện</label>
                    <select id="specialtyId" name="specialtyId" required>
                        <option value="">-- Chọn khoa --</option>
                        <c:forEach var="sp" items="${specialties}">
                            <option value="${sp.specialtyId}" ${param.specialtyId == sp.specialtyId ? 'selected' : ''}>
                                <c:out value="${sp.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="estimatedDurationMinutes">Thời gian dự kiến (phút)</label>
                    <input id="estimatedDurationMinutes" name="estimatedDurationMinutes" type="number" min="1" step="1"
                           value="<c:out value='${empty param.estimatedDurationMinutes ? 15 : param.estimatedDurationMinutes}'/>" required>
                </div>

                <div class="form-group">
                    <label for="requiresSpecimen">
                        <input id="requiresSpecimen" name="requiresSpecimen" type="checkbox"
                               style="width:auto;" ${param.requiresSpecimen == 'on' ? 'checked' : ''}>
                        Cần lấy mẫu (chỉ dành cho xét nghiệm)
                    </label>
                </div>
            </div>

            <button type="submit" class="btn btn-primary">Tạo dịch vụ</button>
        </form>

        <section class="admin-card">
            <div class="card-head"><h2>Danh sách dịch vụ</h2></div>
            <table class="data-table">
                <thead>
                <tr>
                    <th>Mã</th><th>Tên</th><th>Loại</th><th>Khoa</th>
                    <th>Lấy mẫu</th><th>Phút</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${services}">
                    <tr>
                        <td><c:out value="${s.serviceCode}"/></td>
                        <td><c:out value="${s.serviceName}"/></td>
                        <td><c:out value="${s.serviceType}"/></td>
                        <td><c:out value="${s.specialty.name}"/></td>
                        <td>${s.requiresSpecimen ? 'Có' : 'Không'}</td>
                        <td><c:out value="${s.estimatedDurationMinutes}"/></td>
                        <td>
                            <fmt:formatNumber value="${s.price.amount}" type="number" groupingUsed="true" maxFractionDigits="2"/> đ
                            <c:if test="${not s.price.status}"><span class="badge badge-red">Giá đã tắt</span></c:if>
                        </td>
                        <td>
                            <span class="badge ${s.status ? 'badge-green' : 'badge-gray'}">${s.status ? 'Đang dùng' : 'Đã tắt'}</span>
                        </td>
                        <td>
                            <form method="post" action="${ctx}/admin/diagnostic-services">
                                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                <input type="hidden" name="action" value="toggle">
                                <input type="hidden" name="serviceId" value="${s.serviceId}">
                                <input type="hidden" name="status" value="${not s.status}">
                                <button type="submit" class="btn btn-sm ${s.status ? 'btn-red' : 'btn-blue'}">
                                    ${s.status ? 'Tắt' : 'Bật'}
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty services}">
                    <tr><td colspan="9" class="empty">Chưa có dịch vụ cận lâm sàng.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</div>

</body>
</html>
