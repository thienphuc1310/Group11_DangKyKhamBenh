<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="pharmacistName" value="${not empty sessionScope.currentUser ? sessionScope.currentUser.fullName : sessionScope.fullName}"/>

<%-- Dùng cấu trúc và CSS sidebar admin, chỉ giữ các đường dẫn dành cho dược sĩ. --%>
<aside class="sidebar pharmacy-sidebar">
    <div class="sidebar-brand">
        <a class="sidebar-brand-link" href="${ctx}/pharmacy/medicines">
            <img class="ui-logo sidebar-logo" src="${pageContext.request.contextPath}/images/icons/ui/brand-mark.png" alt="" aria-hidden="true">
            <span class="sidebar-brand-text">
                <span class="sidebar-brand-title">UCare</span>
                <span class="sidebar-brand-subtitle">Quản lý thuốc</span>
            </span>
        </a>
    </div>
    <nav class="sidebar-nav" aria-label="Điều hướng dược sĩ">
        <ul>
            <li><a class="${param.activePage == 'medicines' ? 'active' : ''}"
                   href="${ctx}/pharmacy/medicines">Quản lý nhập thuốc</a></li>
            <li><a class="${param.activePage == 'prescriptions' ? 'active' : ''}"
                   href="${ctx}/pharmacy/prescriptions">Đơn chờ cấp thuốc</a></li>
        </ul>
    </nav>
    <div class="sidebar-footer">
        <c:if test="${not empty pharmacistName}">
            <div class="sidebar-user">
                <span class="sidebar-user-avatar"><c:out value="${fn:toUpperCase(fn:substring(pharmacistName, 0, 1))}"/></span>
                <span class="sidebar-user-text">
                    <span class="sidebar-user-name"><c:out value="${pharmacistName}"/></span>
                    <span class="sidebar-user-role">DƯỢC SĨ</span>
                </span>
            </div>
        </c:if>
        <form method="post" action="${ctx}/logout">
            <button class="sidebar-logout" type="submit">Đăng xuất</button>
        </form>
    </div>
</aside>
