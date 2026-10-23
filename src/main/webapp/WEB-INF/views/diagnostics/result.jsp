<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:choose>
    <c:when test="${sessionScope.role == 'DOCTOR'}">
        <jsp:include page="/WEB-INF/includes/header-doctor.jsp"><jsp:param name="pageTitle" value="Thực hiện cận lâm sàng"/><jsp:param name="activeMenu" value="appointments"/></jsp:include>
    </c:when>
    <c:otherwise><jsp:include page="/WEB-INF/includes/header-clinical.jsp"><jsp:param name="pageTitle" value="Thực hiện cận lâm sàng"/></jsp:include></c:otherwise>
</c:choose>
<main class="doctor-page clinical-workspace diagnostic-result-page">
    <a class="back-link" href="${ctx}${sessionScope.role == 'DOCTOR' ? '/doctor/lich-hen' : '/diagnostics/worklist'}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> ${sessionScope.role == 'DOCTOR' ? 'Danh sách lịch hẹn' : 'Danh sách chỉ định'}</a>
    <div class="page-heading">
        <div>
            <h1><c:out value="${item.serviceNameSnapshot}"/></h1>
        </div>
        <span class="status ${item.status == 'COMPLETED' ? 'completed' : 'waiting'}">
            <c:choose>
                <c:when test="${item.status == 'WAITING_PAYMENT'}">Chờ thanh toán</c:when>
                <c:when test="${item.status == 'WAITING_ACCEPTANCE'}">Chờ tiếp nhận</c:when>
                <c:when test="${item.status == 'ACCEPTED'}">Đã tiếp nhận</c:when>
                <c:when test="${item.status == 'SAMPLE_COLLECTED'}">Đã lấy mẫu</c:when>
                <c:when test="${item.status == 'IN_PROGRESS'}">Đang thực hiện</c:when>
                <c:when test="${item.status == 'COMPLETED'}">Đã có kết quả</c:when>
                <c:when test="${item.status == 'CANCELLED'}">Đã hủy</c:when>
                <c:otherwise>Bản nháp</c:otherwise>
            </c:choose>
        </span>
    </div>
    <section class="doctor-card clinical-card-body" aria-label="Thông tin bệnh nhân và chỉ định">
        <div class="clinical-patient-strip diagnostic-patient-heading">
            <span class="patient-avatar" aria-hidden="true">BN</span>
            <div><small>Bệnh nhân</small><h2><c:out value="${item.order.encounter.patient.fullName}"/></h2></div>
            <div><small>Mã bệnh nhân</small><strong><c:out value="${item.order.encounter.patient.patientCode}"/></strong></div>
            <div><small>Mã phiếu chỉ định</small><strong><c:out value="${item.order.orderCode}"/></strong></div>
        </div>
        <div class="clinical-assessment-grid diagnostic-request-details">
            <div><small>Bác sĩ chỉ định</small><p><c:out value="${item.order.encounter.doctor.fullName}"/></p></div>
            <div><small>Khoa thực hiện</small><p><c:out value="${item.performingSpecialty.name}"/></p></div>
            <div><small>Dự kiến có kết quả</small>
                <p><c:choose>
                    <c:when test="${not empty item.expectedResultAt}"><time datetime="<c:out value='${item.expectedResultAt}'/>"><c:out value="${fn:replace(fn:substring(item.expectedResultAt, 0, 16), 'T', ' ')}"/></time></c:when>
                    <c:otherwise>Chưa bắt đầu</c:otherwise>
                </c:choose></p>
            </div>
            <div class="clinical-wide"><small>Yêu cầu / lý do chỉ định</small><p class="clinical-text"><c:out value="${item.order.clinicalIndication}" default="Không ghi yêu cầu"/></p></div>
        </div>
    </section>
    <c:if test="${not empty formError}"><p class="form-error" role="alert"><c:out value="${formError}"/></p></c:if>
    <c:if test="${item.requiresSpecimen}">
        <section class="doctor-card clinical-card-body"><h2>Mẫu xét nghiệm</h2>
            <c:if test="${item.status == 'ACCEPTED'}"><form method="post" action="${ctx}/diagnostics/specimen" class="clinical-actions">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="itemId" value="${item.orderItemId}">
                <input type="hidden" name="action" value="collect"><label for="specimen-type">Loại mẫu</label>
                <input id="specimen-type" name="specimenType" maxlength="100" required placeholder="Ví dụ: Máu"><button class="save-button" type="submit">Xác nhận đã lấy mẫu</button>
            </form></c:if>
            <c:forEach var="link" items="${specimenLinks}">
                <p><c:out value="${link.specimen.specimenCode}"/> — <c:out value="${link.specimen.specimenType}"/> — <c:out value="${link.specimen.status}"/></p>
                <c:if test="${item.status == 'SAMPLE_COLLECTED' && link.specimen.status != 'REJECTED'}">
                    <form method="post" action="${ctx}/diagnostics/specimen" class="clinical-actions">
                        <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="itemId" value="${item.orderItemId}">
                        <input type="hidden" name="specimenId" value="${link.specimen.specimenId}">
                        <c:if test="${link.specimen.status == 'COLLECTED'}"><button type="submit" name="action" value="receive">Đã nhận mẫu tại nơi phân tích</button></c:if>
                        <input name="reason" maxlength="1000" placeholder="Lý do nếu từ chối mẫu"><button type="submit" name="action" value="reject">Từ chối, yêu cầu lấy lại</button>
                    </form>
                </c:if>
            </c:forEach>
        </section>
    </c:if>
    <c:if test="${item.status == 'ACCEPTED' && not item.requiresSpecimen || item.status == 'SAMPLE_COLLECTED'}">
        <form method="post" action="${ctx}${sessionScope.role == 'DOCTOR' ? '/doctor/lich-hen' : '/diagnostics/worklist'}" class="doctor-card clinical-card-body">
            <h2>Thực hiện dịch vụ</h2>
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="itemId" value="${item.orderItemId}">
            <input type="hidden" name="action" value="start"><button class="save-button" type="submit">Bắt đầu thực hiện / phân tích</button>
        </form>
    </c:if>
    <c:if test="${item.status == 'IN_PROGRESS'}">
        <form method="post" action="${ctx}/diagnostics/result" class="doctor-card clinical-card-body clinical-exam-form diagnostic-result-form">
            <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="itemId" value="${item.orderItemId}">
            <h2>Nội dung kết quả</h2>
            <div class="clinical-exam-grid diagnostic-result-fields">
                <div class="form-group"><label for="findings">${item.serviceTypeSnapshot == 'IMAGING' ? 'Kết quả / mô tả hình ảnh' : 'Nội dung kết quả xét nghiệm'}</label>
                    <textarea id="findings" name="findings" rows="8" placeholder="Nhập nội dung kết quả thực hiện..."><c:out value="${param.findings != null ? param.findings : result.findings}"/></textarea></div>
                <div class="form-group"><label for="conclusion">Kết luận</label>
                    <textarea id="conclusion" name="conclusion" rows="8" placeholder="Nhập kết luận chuyên môn..."><c:out value="${param.conclusion != null ? param.conclusion : result.conclusion}"/></textarea></div>
            </div>
            <div class="clinical-form-footer">
                <div class="clinical-form-actions">
                    <button class="btn btn-outline" type="submit" name="action" value="save">Lưu nháp kết quả</button>
                    <button class="save-button" type="submit" name="action" value="release">Xác nhận có kết quả</button>
                </div>
                <p class="clinical-help">Kết quả đã xác nhận không thể chỉnh sửa.</p>
            </div>
        </form>
        <c:choose>
        <c:when test="${not empty result}">
            <form method="post" action="${ctx}/results/file" enctype="multipart/form-data" class="doctor-card clinical-card-body">
                <input type="hidden" name="csrf" value="${sessionScope.clinicalCsrf}"><input type="hidden" name="itemId" value="${item.orderItemId}">
                <input type="hidden" name="resultId" value="${result.resultId}">
                <h2>Đính kèm tệp kết quả</h2>
                <p id="result-file-help" class="clinical-help">PNG, JPEG hoặc PDF. Tối đa 10 MB/tệp.</p>
                <div class="diagnostic-upload-row">
                    <div class="form-group"><label for="result-file">Chọn ảnh / tài liệu kết quả</label>
                        <input id="result-file" type="file" name="resultFile" accept="image/png,image/jpeg,application/pdf" aria-describedby="result-file-help" required></div>
                    <button class="save-button" type="submit">Tải tệp kết quả</button>
                </div>
            </form>
        </c:when>
        <c:otherwise>
            <section class="doctor-card clinical-card-body">
                <h2>Đính kèm tệp kết quả</h2>
                <p class="clinical-help diagnostic-empty-note">Cần lưu nháp trước khi tải tệp.</p>
            </section>
        </c:otherwise>
        </c:choose>
    </c:if>
    <c:if test="${result.status == 'RELEASED'}"><section class="doctor-card clinical-card-body">
        <h2 class="clinical-success"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/success.png" alt="" aria-hidden="true"> Kết quả đã phát hành</h2>
        <div class="clinical-exam-grid clinical-record-grid">
            <div><small>Kết quả / mô tả</small><p class="clinical-text"><c:out value="${result.findings}" default="Không ghi"/></p></div>
            <div><small>Kết luận</small><p class="clinical-text"><c:out value="${result.conclusion}" default="Không ghi"/></p></div>
        </div>
    </section></c:if>
    <section class="doctor-card clinical-card-body">
        <div class="diagnostic-attachment-heading"><h2>Tệp đã đính kèm</h2><span class="muted">${fn:length(resultFiles)} tệp</span></div>
        <c:choose>
            <c:when test="${not empty resultFiles}">
                <ul class="diagnostic-file-list">
                    <c:forEach var="file" items="${resultFiles}">
                        <li><a class="clinical-file diagnostic-file-link" href="${ctx}/results/file?fileId=${file.fileId}" target="_blank" rel="noopener">
                            <span class="diagnostic-file-type" aria-hidden="true">${file.contentType == 'application/pdf' ? 'PDF' : 'ẢNH'}</span>
                            <span class="diagnostic-file-name"><c:out value="${file.originalName}"/><small>Mở tệp trong thẻ mới</small></span>
                        </a></li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise><p class="clinical-help diagnostic-empty-note">Chưa đính kèm tệp.</p></c:otherwise>
        </c:choose>
    </section>
</main>
<c:choose>
    <c:when test="${sessionScope.role == 'DOCTOR'}"><jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/></c:when>
    <c:otherwise><jsp:include page="/WEB-INF/includes/footer-clinical.jsp"/></c:otherwise>
</c:choose>
