<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="${param.pageTitle}"/>
    <jsp:param name="bodyClass" value="patient-site"/>
</jsp:include>
<div class="patient-layout">
    <jsp:include page="sidebar-patient.jsp"><jsp:param name="activeMenu" value="${param.activeMenu}"/></jsp:include>
    <main class="patient-content">
        <div class="patient-breadcrumb"><a href="${pageContext.request.contextPath}/main">Trang chủ</a><span>/</span><span>Tài khoản</span></div>
