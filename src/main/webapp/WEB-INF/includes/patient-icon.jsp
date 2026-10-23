<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="iconName" value="document"/>
<c:if test="${param.name == 'user' || param.name == 'calendar' || param.name == 'bell' || param.name == 'folder' || param.name == 'lab' || param.name == 'card' || param.name == 'logout' || param.name == 'arrow' || param.name == 'clock' || param.name == 'location' || param.name == 'check'}">
    <c:set var="iconName" value="${param.name}"/>
</c:if>
<span class="patient-icon" aria-hidden="true">
    <img class="patient-icon-normal" src="${pageContext.request.contextPath}/images/icons/patient/${iconName}.png" width="96" height="96" alt="">
    <img class="patient-icon-active" src="${pageContext.request.contextPath}/images/icons/patient/${iconName}-active.png" width="96" height="96" alt="">
    <img class="patient-icon-white" src="${pageContext.request.contextPath}/images/icons/patient/${iconName}-white.png" width="96" height="96" alt="">
</span>
