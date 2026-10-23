<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="patientName" value="${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : sessionScope.fullName}"/>
<c:set var="patientCtx" value="${pageContext.request.contextPath}"/>
<aside class="patient-sidebar">
    <div class="patient-sidebar-profile">
        <span class="patient-avatar"><c:out value="${not empty patientName ? fn:toUpperCase(fn:substring(patientName, 0, 1)) : 'BN'}"/></span>
        <div><strong><c:out value="${patientName}" default="Tài khoản của tôi"/></strong><small>Tài khoản bệnh nhân</small></div>
    </div>
    <nav class="patient-nav" aria-label="Tài khoản bệnh nhân">
        <a class="${param.activeMenu == 'profile' ? 'active' : ''}" href="${patientCtx}/patient/profile"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="user"/></jsp:include>Thông tin cá nhân</a>
        <a class="${param.activeMenu == 'appointments' ? 'active' : ''}" href="${patientCtx}/patient/lich-kham-cua-toi"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="calendar"/></jsp:include>Lịch khám của tôi</a>
        <a class="${param.activeMenu == 'records' ? 'active' : ''}" href="${patientCtx}/patient/ho-so-benh-an"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="folder"/></jsp:include>Hồ sơ bệnh án</a>
        <a class="${param.activeMenu == 'results' ? 'active' : ''}" href="${patientCtx}/patient/ket-qua-can-lam-sang"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="lab"/></jsp:include>Kết quả cận lâm sàng</a>
        <a class="${param.activeMenu == 'prescriptions' ? 'active' : ''}" href="${patientCtx}/patient/don-thuoc"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="document"/></jsp:include>Đơn thuốc</a>
        <a class="${param.activeMenu == 'payments' ? 'active' : ''}" href="${patientCtx}/thanhtoan"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="card"/></jsp:include>Thanh toán</a>
    </nav>
    <form class="patient-sidebar-logout" method="post" action="${patientCtx}/logout"><button type="submit"><jsp:include page="patient-icon.jsp"><jsp:param name="name" value="logout"/></jsp:include>Đăng xuất</button></form>
</aside>
