<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/includes/header.jsp"><jsp:param name="bodyClass" value="patient-site patient-public"/><jsp:param name="pageTitle" value="Bác sĩ | UCare"/></jsp:include>
<link rel="stylesheet" href="${ctx}/css/bacsi.css?v=20261023-7">

<main class="doctors-page">
    <div class="doctors-container">
        <div class="public-heading"><span class="public-eyebrow">ĐỘI NGŨ UCARE</span><h1>Danh sách bác sĩ</h1><p>Tìm bác sĩ theo tên và chuyên khoa để lựa chọn lịch khám phù hợp.</p></div>

        <%-- Form GET: bộ lọc nằm trên URL nên F5 / chia sẻ link vẫn giữ kết quả --%>
        <form id="doctor-filter-form" class="doctors-filter" method="get" action="${ctx}/bacsi">
            <div class="doctors-search">
                <label class="doctors-sr-only" for="doctor-search">Tìm theo tên bác sĩ</label>
                <input type="search" id="doctor-search" name="q" autocomplete="off"
                       placeholder="Tìm theo tên bác sĩ..." value="<c:out value='${param.q}'/>">
            </div>

            <label class="doctors-sr-only" for="specialty-filter">Chọn chuyên khoa</label>
            <select id="specialty-filter" name="specialtyId">
                <option value="">--Theo chuyên khoa--</option>
                <c:forEach var="s" items="${specialties}">
                    <c:if test="${s.status}">
                        <option value="${s.specialtyId}" ${s.specialtyId == selectedSpecialtyId ? 'selected' : ''}>
                            <c:out value="${s.name}"/>
                        </option>
                    </c:if>
                </c:forEach>
            </select>

            <label class="doctors-sr-only" for="gender-filter">Chọn giới tính</label>
            <select id="gender-filter" name="gender">
                <option value="">--Theo giới tính--</option>
                <option value="nam" ${param.gender == 'nam' ? 'selected' : ''}>Nam</option>
                <option value="nu"  ${param.gender == 'nu'  ? 'selected' : ''}>Nữ</option>
            </select>

            <button class="patient-button patient-button-primary" type="submit">Tìm kiếm</button>
        </form>

        <p class="doctors-count">Tìm thấy <strong>${fn:length(doctors)}</strong> bác sĩ</p>

        <section class="doctors-grid" aria-label="Danh sách bác sĩ">
            <c:forEach var="d" items="${doctors}">
                <div class="doctor-card">
                    <div class="doctor-info">
                        <span class="doctor-photo doctor-avatar"><c:out value="${fn:substring(d.fullName, 0, 1)}"/></span>
                        <div class="doctor-text">
                            <p class="doctor-degree"><c:out value="${empty d.qualification ? 'BS.' : d.qualification}"/></p>
                            <p class="doctor-name"><c:out value="${d.fullName}"/></p>
                            <p class="doctor-specialty"><c:out value="${d.specialty.name}"/></p>
                        </div>
                    </div>
                    <details class="public-doctor-detail">
                        <summary>Xem hồ sơ</summary>
                        <p>Kinh nghiệm: <c:out value="${d.experienceYears}" default="Chưa cập nhật"/><c:if test="${not empty d.experienceYears}"> năm</c:if></p>
                        <p><c:out value="${d.description}" default="Thông tin đang được cập nhật."/></p>
                    </details>
                </div>
            </c:forEach>

            <c:if test="${empty doctors}">
                <p class="doctors-empty">Không tìm thấy bác sĩ phù hợp.</p>
            </c:if>
        </section>
    </div>
</main>

<script>
    (function () {
        var form = document.getElementById('doctor-filter-form');
        var search = document.getElementById('doctor-search');

        // Đổi chuyên khoa / giới tính -> lọc ngay
        form.querySelectorAll('select').forEach(function (sel) {
            sel.addEventListener('change', function () { form.submit(); });
        });

        // Bấm dấu X trong ô tìm kiếm -> xoá bộ lọc tên
        search.addEventListener('search', function () {
            if (!search.value) form.submit();
        });
        // Gõ tên rồi nhấn Enter -> form tự gửi (mặc định của trình duyệt)
    })();
</script>

<jsp:include page="/WEB-INF/includes/footer-user.jsp"/>
