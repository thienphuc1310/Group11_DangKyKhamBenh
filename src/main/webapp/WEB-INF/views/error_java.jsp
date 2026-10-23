<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isErrorPage="true"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ui.css?v=20261023-7">
    <title>Lỗi hệ thống</title>
</head>
<body>
<h1>Lỗi hệ thống</h1>
<p>Ứng dụng không thể xử lý yêu cầu.</p>

<h2>Chi tiết</h2>
<p>Loại lỗi: <c:out value="${requestScope['javax.servlet.error.exception_type']}"/></p>
<p>Thông báo: <c:out value="${requestScope['javax.servlet.error.message']}"/></p>
<c:if test="${not empty requestScope['javax.servlet.error.exception'].cause}">
    <p>Nguyên nhân: <c:out value="${requestScope['javax.servlet.error.exception'].cause.message}"/></p>
</c:if>
<c:if test="${not empty requestScope['javax.servlet.error.exception'].cause.cause}">
    <p>Chi tiết nguyên nhân: <c:out value="${requestScope['javax.servlet.error.exception'].cause.cause.message}"/></p>
</c:if>
<c:if test="${not empty requestScope['javax.servlet.error.exception'].cause.cause.cause}">
    <p>Lỗi gốc: <c:out value="${requestScope['javax.servlet.error.exception'].cause.cause.cause.message}"/></p>
</c:if>
</body>
</html>
