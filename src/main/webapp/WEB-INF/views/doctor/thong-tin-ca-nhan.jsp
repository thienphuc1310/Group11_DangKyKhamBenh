<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Thông tin cá nhân"/>
    <jsp:param name="activeMenu" value="profile"/>
</jsp:include>

<main class="doctor-page doctor-profile-page">
    <h1>Thông tin cá nhân</h1>

    <c:if test="${updated}"><div class="success-message">Đã lưu thông tin thành công.</div></c:if>
    <c:if test="${not empty error}"><div class="form-error"><c:out value="${error}"/></div></c:if>

    <section class="doctor-card doctor-profile-card">
        <div class="doctor-profile-summary">
            <span class="doctor-profile-avatar">BS</span>
            <div>
                <h2><c:out value="${profile.fullName}"/></h2>
                <p>Mã bác sĩ: <strong><c:out value="${profile.doctorCode}"/></strong></p>
            </div>
        </div>

        <form id="profile-form" method="post" action="${pageContext.request.contextPath}/doctor/profile"
              data-editing="${not empty error}">
            <div class="doctor-profile-grid">
                <div class="form-group">
                    <label for="fullName">Họ và tên *</label>
                    <input id="fullName" name="fullName" value="<c:out value='${profile.fullName}'/>" required disabled>
                </div>
                <div class="form-group">
                    <label for="birthDate">Ngày sinh</label>
                    <input type="date" id="birthDate" name="birthDate" value="<c:out value='${profile.user.dateOfBirth}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="gender">Giới tính</label>
                    <select id="gender" name="gender" disabled>
                        <option value="">Chọn giới tính</option>
                        <option value="Nam" ${profile.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                        <option value="Nữ" ${profile.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                        <option value="Khác" ${profile.gender == 'Khác' ? 'selected' : ''}>Khác</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="phone">Số điện thoại</label>
                    <input type="tel" id="phone" name="phone" value="<c:out value='${profile.phone}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" value="<c:out value='${profile.email}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="specialtyId">Chuyên khoa *</label>
                    <select id="specialtyId" name="specialtyId" required disabled>
                        <option value="">Chọn chuyên khoa</option>
                        <c:forEach var="specialty" items="${specialties}">
                            <option value="${specialty.specialtyId}"
                                    ${profile.specialty.specialtyId == specialty.specialtyId ? 'selected' : ''}>
                                <c:out value="${specialty.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group profile-wide">
                    <label for="address">Địa chỉ</label>
                    <input id="address" name="address" value="<c:out value='${profile.user.address}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="licenseNumber">Số giấy phép hành nghề</label>
                    <input id="licenseNumber" name="licenseNumber" value="<c:out value='${profile.licenseNumber}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="experienceYears">Số năm kinh nghiệm</label>
                    <input type="number" min="0" id="experienceYears" name="experienceYears"
                           value="<c:out value='${profile.experienceYears}'/>" disabled>
                </div>
                <div class="form-group profile-wide">
                    <label for="qualification">Trình độ chuyên môn</label>
                    <input id="qualification" name="qualification" value="<c:out value='${profile.qualification}'/>" disabled>
                </div>
                <div class="form-group profile-wide">
                    <label for="description">Giới thiệu chuyên môn</label>
                    <textarea id="description" name="description" rows="4" disabled><c:out value="${profile.description}"/></textarea>
                </div>
            </div>

            <div class="doctor-profile-actions profile-view-actions">
                <button type="button" class="save-button" id="edit-profile">Thay đổi thông tin</button>
            </div>
            <div class="doctor-profile-actions profile-edit-actions" hidden>
                <button type="button" id="cancel-profile">Huỷ thay đổi</button>
                <button type="submit" class="save-button">Lưu thay đổi</button>
            </div>
        </form>
    </section>
</main>

<script src="${pageContext.request.contextPath}/js/profile.js?v=20261008-2"></script>
<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
