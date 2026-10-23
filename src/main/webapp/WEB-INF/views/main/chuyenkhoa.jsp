<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--Header--%>
<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-public"/><jsp:param name="pageTitle" value="Chuyên khoa | UCare"/></jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chuyenkhoa.css?v=20261023-7">

<main class="specialties-page">
    <div class="specialties-container">
        <div class="public-heading"><span class="public-eyebrow">CHUYÊN KHOA UCARE</span><h1>Chuyên khoa</h1><p>Tìm hiểu các chuyên khoa và đội ngũ bác sĩ của chúng tôi.</p></div>

        <!-- Danh sách chuyên khoa -->
        <section class="specialties-grid" aria-label="Danh sách chuyên khoa">
            <c:forEach var="specialty" items="${specialties}">
                <div class="specialty-card">
                    <img class="specialty-icon"
                         src="${pageContext.request.contextPath}/images/khoa-cap-cuu.png" alt="">
                    <h2>Khoa <c:out value="${specialty.name}"/></h2>
                    <p class="public-specialty-description"><c:out value="${specialty.description}" default="Thông tin đang được cập nhật."/></p>
                    <c:url var="specialtyDoctorsUrl" value="/bacsi"><c:param name="specialtyId" value="${specialty.specialtyId}"/></c:url>
                    <a class="specialty-detail" href="<c:out value='${specialtyDoctorsUrl}'/>">Xem bác sĩ <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></a>
                </div>
            </c:forEach>
            <c:if test="${empty specialties}">
                <p class="public-empty">Hiện chưa có chuyên khoa nào.</p>
            </c:if>
        </section>
    </div>
</main>

<%--footer--%>
<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
