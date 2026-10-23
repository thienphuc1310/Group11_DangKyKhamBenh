<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý giá dịch vụ - UCare</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css?v=20261023-7">
</head>
<body>
<div class="main-layout">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp"/>

    <main class="content-area">
        <h1>Quản lý giá dịch vụ</h1>

        <c:if test="${not empty requestScope.error}">
            <p class="alert alert-fail"><c:out value="${requestScope.error}"/></p>
        </c:if>
        <c:if test="${requestScope.created}">
            <p class="alert alert-ok">Đã tạo dịch vụ và giá thành công.</p>
        </c:if>
        <c:if test="${requestScope.updated}">
            <p class="alert alert-ok">Đã cập nhật giá dịch vụ thành công.</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/price"
              class="admin-form schedule-assignment-form">
            <input type="hidden" name="action" value="create">

            <div class="schedule-form-grid">
                <div class="form-group">
                    <label for="serviceCode">Mã dịch vụ</label>
                    <input id="serviceCode" name="serviceCode" type="text" maxlength="100"
                           value="<c:out value='${param.serviceCode}'/>"
                           placeholder="Ví dụ: DANG_KY_KHAM_BENH" required>
                </div>

                <div class="form-group">
                    <label for="serviceName">Tên dịch vụ</label>
                    <input id="serviceName" name="serviceName" type="text" maxlength="255"
                           value="<c:out value='${param.serviceName}'/>"
                           placeholder="Ví dụ: Khám bệnh" required>
                </div>

                <div class="form-group">
                    <label for="amount">Giá dịch vụ (VNĐ)</label>
                    <input id="amount" name="amount" type="number" min="1" step="1"
                           value="<c:out value='${param.amount}'/>"
                           placeholder="Ví dụ: 250000" required>
                </div>
            </div>

            <button type="submit" class="btn btn-primary">Tạo dịch vụ</button>
        </form>

        <section class="admin-card">
            <div class="card-head">
                <div>
                    <h2>Danh sách dịch vụ</h2>
                </div>
            </div>

            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Mã dịch vụ</th>
                    <th>Tên dịch vụ</th>
                    <th>Giá</th>
                    <th>Cập nhật lúc</th>
                    <th>Người cập nhật</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="price" items="${requestScope.servicePrices}">
                    <tr>
                        <td><c:out value="${price.priceId}"/></td>
                        <td><c:out value="${price.serviceCode}"/></td>
                        <td><c:out value="${price.serviceName}"/></td>
                        <td>
                            <fmt:formatNumber value="${price.amount}" type="number"
                                              groupingUsed="true" maxFractionDigits="0"/> đ
                        </td>
                        <td><c:out value="${price.updatedAt}"/></td>
                        <td><c:out value="${price.updatedBy.fullName}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty requestScope.servicePrices}">
                    <tr>
                        <td colspan="6" class="empty">Chưa có dịch vụ nào trong bảng giá.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</div>
</body>
</html>
