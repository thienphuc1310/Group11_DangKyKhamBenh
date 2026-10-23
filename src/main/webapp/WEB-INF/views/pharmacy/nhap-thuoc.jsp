<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nhập thuốc - UCare</title>
    <link rel="stylesheet" href="${ctx}/css/admin/admin.css?v=20261023-7">
    <link rel="stylesheet" href="${ctx}/css/pharmacy.css?v=20261023-7">
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar-pharmacy.jsp">
        <jsp:param name="activePage" value="medicines"/>
    </jsp:include>

<main class="content-area medicine-page">
    <h1>QUẢN LÝ NHẬP THUỐC</h1>

    <c:if test="${not empty medicineError}">
        <div class="alert alert-fail" role="alert">
            <c:out value="${medicineError}" />
        </div>
    </c:if>

    <c:if test="${param.created == '1' || param.received == '1' || param.updated == '1'}">
        <div class="alert alert-ok" role="status">
            Đã ghi nhận thay đổi thuốc/kho thành công.
        </div>
    </c:if>

    <section class="admin-card medicine-create-card" aria-labelledby="medicine-create-title">
        <h2 id="medicine-create-title">Thêm thuốc mới và nhập tồn ban đầu</h2>

        <form method="post" action="${ctx}/pharmacy/medicines" class="admin-form medicine-create-form">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
            <input type="hidden" name="formToken" value="${createToken}">
            <input type="hidden" name="action" value="create">

            <div class="medicine-grid">
                <div class="form-group">
                    <label for="medicineName">Tên thuốc</label>
                    <input id="medicineName" type="text" name="medicineName" maxlength="255" required>
                </div>

                <div class="form-group">
                    <label for="quantity">Số viên nhập</label>
                    <input id="quantity" type="number" name="quantity" min="1" step="1" required>
                </div>

                <div class="form-group">
                    <label for="price">Giá bán/viên (VNĐ)</label>
                    <input id="price" type="number" name="price"
                           min="0.01" step="0.01" required>
                </div>

                <div class="form-group medicine-description">
                    <label for="description">Công dụng</label>
                    <textarea id="description" name="description" maxlength="4000" rows="3"></textarea>
                </div>

                <div class="form-group">
                    <label for="receivedDate">Ngày nhập</label>
                    <input id="receivedDate" type="date" name="receivedDate"
                           value="${today}" max="${today}" required>
                </div>
            </div>

            <button class="btn btn-primary" type="submit">Thêm thuốc và nhập kho</button>
        </form>
    </section>

    <section class="admin-card" aria-labelledby="medicine-list-title">
        <div class="card-head"><h2 id="medicine-list-title">Danh sách thuốc</h2></div>

        <form class="filter-bar medicine-search" method="get" action="${ctx}/pharmacy/medicines">
            <label class="medicine-visually-hidden" for="medicine-search">Tìm theo tên hoặc mã thuốc</label>
            <input id="medicine-search" type="search" name="q" value="<c:out value='${keyword}'/>"
                   placeholder="Tìm theo tên hoặc mã thuốc">
            <button class="btn btn-primary" type="submit">Tìm kiếm</button>
            <a class="btn btn-outline" href="${ctx}/pharmacy/medicines">Xóa lọc</a>
        </form>

        <div class="medicine-table-wrap">
            <table class="data-table medicine-table">
                <thead>
                    <tr>
                        <th>Mã thuốc</th>
                        <th>Tên thuốc</th>
                        <th>Đơn vị</th>
                        <th>Giá/viên</th>
                        <th>Tồn kho</th>
                        <th>Đã giữ</th>
                        <th>Khả dụng</th>
                        <th>Chỉnh thông tin</th>
                        <th>Nhập thêm</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="m" items="${medicines}">
                        <tr>
                            <td class="medicine-code"><c:out value="${m.medicineCode}" /></td>
                            <td class="medicine-name">
                                <strong><c:out value="${m.medicineName}" /></strong>
                                <span class="medicine-note"><c:out value="${m.description}" /></span>
                            </td>
                            <td><c:out value="${m.unit}" /></td>
                            <td><fmt:formatNumber value="${m.price}" groupingUsed="true" maxFractionDigits="2"/> đ</td>
                            <td><c:out value="${m.quantityOnHand}" /></td>
                            <td><c:out value="${m.quantityReserved}" /></td>
                            <td><span class="badge ${m.availableQuantity > 0 ? 'badge-green' : 'badge-gray'}"><c:out value="${m.availableQuantity}" /></span></td>
                            <td>
                                <details class="medicine-editor">
                                    <summary class="btn btn-outline btn-sm">Sửa thông tin</summary>
                                    <form method="post" action="${ctx}/pharmacy/medicines" class="admin-form medicine-inline-form">
                                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                        <input type="hidden" name="formToken" value="${updateToken}">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="medicineId" value="${m.medicineId}">

                                        <div class="form-group">
                                            <label for="edit-name-${m.medicineId}">Tên thuốc</label>
                                            <input id="edit-name-${m.medicineId}" type="text" name="medicineName" maxlength="255"
                                                   value="<c:out value='${m.medicineName}'/>" required>
                                        </div>

                                        <div class="form-group">
                                            <label for="edit-description-${m.medicineId}">Công dụng</label>
                                            <textarea id="edit-description-${m.medicineId}" name="description" maxlength="4000"><c:out value="${m.description}" /></textarea>
                                        </div>

                                        <div class="form-group">
                                            <label for="edit-price-${m.medicineId}">Giá bán/viên (VNĐ)</label>
                                            <input id="edit-price-${m.medicineId}" type="number" name="price" min="0.01" step="0.01"
                                                   value="${m.price}" required>
                                        </div>

                                        <button class="btn btn-primary btn-sm" type="submit">Lưu thông tin</button>
                                    </form>
                                </details>
                                <c:url var="historyUrl" value="/pharmacy/medicines">
                                    <c:param name="medicineId" value="${m.medicineId}"/>
                                    <c:param name="q" value="${keyword}"/>
                                </c:url>
                                <a class="medicine-history-link" href="${historyUrl}#stock-history">Lịch sử kho</a>
                            </td>
                            <td>
                                <c:if test="${m.status}">
                                    <details class="medicine-editor">
                                    <summary class="btn btn-blue btn-sm">Nhập thêm</summary>
                                    <form class="admin-form medicine-inline-form" method="post" action="${ctx}/pharmacy/stock">
                                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}">
                                        <input type="hidden" name="formToken" value="${receiveToken}">
                                        <input type="hidden" name="medicineId" value="${m.medicineId}">
                                        <div class="form-group">
                                            <label for="receive-quantity-${m.medicineId}">Số viên nhập thêm</label>
                                            <input id="receive-quantity-${m.medicineId}" type="number" name="quantity"
                                                   min="1" step="1" placeholder="Số viên" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="receive-date-${m.medicineId}">Ngày nhập</label>
                                            <input id="receive-date-${m.medicineId}" type="date" name="receivedDate"
                                                   value="${today}" max="${today}" required>
                                        </div>
                                        <button class="btn btn-primary btn-sm" type="submit">Xác nhận nhập</button>
                                    </form>
                                    </details>
                                </c:if>
                                <c:if test="${!m.status}">
                                    <span class="badge badge-gray">Đã ngừng sử dụng</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty medicines}">
                        <tr>
                            <td colspan="9" class="empty-hint">Không có thuốc phù hợp.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>
    <c:if test="${not empty selectedMedicine}">
        <section id="stock-history" class="admin-card medicine-history">
            <div class="card-head"><h2>Lịch sử kho: <c:out value="${selectedMedicine.medicineName}"/></h2></div>
            <p class="medicine-note medicine-history-note">Thời gian dưới đây là lúc hệ thống ghi nhận thao tác, không phải ngày nhập khai báo trên form.</p>
            <div class="medicine-table-wrap">
                <table class="data-table medicine-history-table">
                    <thead><tr><th>Thời gian ghi nhận</th><th>Loại</th><th>Số viên</th><th>Người thực hiện</th><th>Ghi chú</th></tr></thead>
                    <tbody>
                        <c:forEach var="movement" items="${stockHistory}">
                            <tr>
                                <td><c:out value="${movement.createdAt}"/></td>
                                <td><c:choose>
                                    <c:when test="${movement.movementType == 'INBOUND'}">Nhập kho</c:when>
                                    <c:when test="${movement.movementType == 'RESERVE'}">Giữ cho đơn thuốc</c:when>
                                    <c:when test="${movement.movementType == 'RELEASE'}">Giải phóng lượng giữ</c:when>
                                    <c:when test="${movement.movementType == 'DISPENSE'}">Cấp phát thuốc</c:when>
                                    <c:otherwise><c:out value="${movement.movementType}"/></c:otherwise>
                                </c:choose></td>
                                <td><c:out value="${movement.quantity}"/></td>
                                <td><c:out value="${movement.performedBy.fullName}"/></td>
                                <td><c:out value="${movement.note}"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty stockHistory}"><tr><td colspan="5" class="empty-hint">Chưa có lịch sử kho.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </c:if>
</main>
</div>
</body>
</html>
