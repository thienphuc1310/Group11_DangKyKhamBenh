<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:choose><c:when test="${not empty param.value}">${fn:substring(param.value, 8, 10)}/${fn:substring(param.value, 5, 7)}/${fn:substring(param.value, 0, 4)}<c:if test="${param.time == 'true'}"> · ${fn:substring(param.value, 11, 16)}</c:if></c:when><c:otherwise>—</c:otherwise></c:choose>
