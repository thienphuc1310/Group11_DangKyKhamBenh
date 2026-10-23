<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/WEB-INF/includes/header-patient.jsp">
    <jsp:param name="pageTitle" value="Thông tin cá nhân"/>
    <jsp:param name="activeMenu" value="profile"/>
</jsp:include>

    <div class="patient-heading"><div><h1>Thông tin cá nhân</h1><p>Quản lý thông tin hồ sơ của bạn.</p></div></div>
    <c:if test="${updated}"><p class="patient-notice patient-notice-success" role="status">Lưu thông tin thành công.</p></c:if>
    <c:if test="${not empty error}"><p class="patient-notice patient-notice-error" role="alert"><c:out value="${error}"/></p></c:if>
    <section class="patient-card" aria-labelledby="profile-name">
        <div class="patient-profile-summary">
            <span class="patient-avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(profile.fullName, 0, 1))}"/></span>
            <div><h2 id="profile-name"><c:out value="${profile.fullName}"/></h2><span class="patient-badge"><c:out value="${profile.patientCode}"/></span></div>
        </div>

        <form id="profile-form" method="post"
              action="${pageContext.request.contextPath}/patient/profile"
              data-editing="${not empty error}">

            <div class="patient-form-grid">
                <div class="patient-field"><label for="full-name">Họ và tên *</label><input type="text" id="full-name" name="fullName" value="<c:out value='${profile.fullName}'/>" required disabled></div>
                <div class="patient-field"><label for="birth-date">Ngày sinh</label><input type="date" id="birth-date" name="birthDate" value="<c:out value='${profile.dateOfBirth}'/>" disabled></div>
                <div class="patient-field"><label for="gender">Giới tính</label>

                    <select id="gender" name="gender" disabled>
                        <option value="">Chọn giới tính</option>
                        <option value="Nam" ${profile.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                        <option value="Nữ" ${profile.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>

                    </select>
                </div>
                <div class="patient-field"><label for="phone">Số điện thoại</label><input type="tel" id="phone" name="phone" value="<c:out value='${profile.phone}'/>" disabled></div>
                <div class="patient-field"><label for="email">Email</label><input type="email" id="email" name="email" value="<c:out value='${profile.email}'/>" disabled></div>
                <div class="patient-field patient-field-wide"><label for="address">Địa chỉ</label><input type="text" id="address" name="address" value="<c:out value='${profile.address}'/>" disabled></div>
            </div>

            <div class="patient-form-footer profile-view-actions">
                <button type="button" class="patient-button patient-button-primary" id="edit-profile">Thay đổi thông tin</button>
            </div>

            <div class="patient-form-footer profile-edit-actions" hidden>
                <button type="button" class="patient-button" id="cancel-profile">Huỷ thay đổi</button>
                <button type="submit" class="patient-button patient-button-primary">Lưu thay đổi</button>
            </div>
        </form>
    </section>

<script src="${pageContext.request.contextPath}/js/profile.js?v=20260921-1"></script>
<jsp:include page="/WEB-INF/includes/footer-patient.jsp"/>
