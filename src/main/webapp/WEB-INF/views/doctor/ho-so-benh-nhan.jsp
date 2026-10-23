<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<jsp:include page="/WEB-INF/includes/header-doctor.jsp">
    <jsp:param name="pageTitle" value="Hồ sơ bệnh nhân"/>
    <jsp:param name="activeMenu" value="records"/>
</jsp:include>

<main class="doctor-page doctor-records-page">
    <div class="page-heading">
        <div>
            <h1>Hồ sơ bệnh nhân</h1>
        </div>
    </div>

    <section class="doctor-card patient-record-list">
        <form method="get" action="${ctx}/doctor/ho-so-benh-nhan" class="filter-form">
            <div class="form-group search-field">
                <label for="patient-record-search">Tìm bệnh nhân</label>
                <input id="patient-record-search" type="search" name="q"value="<c:out value='${keyword}'/>"placeholder="Nhập mã bệnh nhân, họ tên hoặc số điện thoại..." autocomplete="off">
            </div>
            <div class="record-filter-actions">
                <button type="submit" class="save-button">Tìm kiếm</button>
                <a class="btn btn-outline" href="${ctx}/doctor/ho-so-benh-nhan">Xóa lọc</a>
            </div>
        </form>

        <div class="table-scroll">
            <table>
                <thead>
                <tr>
                    <th scope="col">Mã bệnh nhân</th>
                    <th scope="col">Họ và tên</th>
                    <th scope="col">Ngày sinh</th>
                    <th scope="col">Giới tính</th>
                    <th scope="col">Điện thoại</th>
                    <th scope="col">${paraclinical ? 'Thực hiện gần nhất' : 'Lần khám gần nhất'}</th>
                    <th scope="col">${paraclinical ? 'Số lần thực hiện' : 'Số lần khám'}</th>
                    <th scope="col">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="patient" items="${patients}">
                    <c:url var="recordUrl" value="/doctor/ho-so-benh-nhan">
                        <c:param name="patientId" value="${patient.patientId}"/>
                        <c:if test="${not empty keyword}"><c:param name="q" value="${keyword}"/></c:if>
                    </c:url>
                    <tr>
                        <td><c:out value="${patient.patientCode}"/></td>
                        <td><strong><c:out value="${patient.fullName}"/></strong></td>
                        <td><c:out value="${patient.dateOfBirth}" default="Chưa cập nhật"/></td>
                        <td><c:out value="${patient.gender}" default="Chưa cập nhật"/></td>
                        <td><c:out value="${patient.phone}" default="Chưa cập nhật"/></td>
                        <td><c:out value="${latestDates[patient.patientId]}"/></td>
                        <td><c:out value="${recordCounts[patient.patientId]}"/></td>
                        <td><a class="record-detail-link" href="${recordUrl}">Xem hồ sơ</a></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty patients}">
                    <tr><td colspan="8">${paraclinical ? 'Không tìm thấy bệnh nhân đã hoàn tất chẩn đoán hình ảnh với bác sĩ này.' : 'Không tìm thấy bệnh nhân đã hoàn thành khám.'}</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

    <c:if test="${not empty selectedPatient}">
        <section class="doctor-card patient-summary record-patient-summary" aria-label="Thông tin bệnh nhân">
            <span class="patient-avatar" aria-hidden="true">BN</span>
            <div class="patient-main-info">
                <h2><c:out value="${selectedPatient.fullName}"/></h2>
                <p><c:out value="${selectedPatient.patientCode}"/></p>
                <span><c:out value="${selectedPatient.email}" default="Chưa cập nhật email"/></span>
            </div>
            <div><small>Ngày sinh</small><span><c:out value="${selectedPatient.dateOfBirth}" default="Chưa cập nhật"/></span></div>
            <div><small>Giới tính</small><span><c:out value="${selectedPatient.gender}" default="Chưa cập nhật"/></span></div>
            <div><small>Điện thoại</small><span><c:out value="${selectedPatient.phone}" default="Chưa cập nhật"/></span></div>
            <div><small>Địa chỉ</small><span><c:out value="${selectedPatient.address}" default="Chưa cập nhật"/></span></div>
        </section>

        <section class="record-history" aria-label="Lịch sử khám">
            <h2>${paraclinical ? 'Lịch sử thực hiện với bác sĩ' : 'Lịch sử khám với bác sĩ'}</h2>
            <c:choose>
            <c:when test="${paraclinical}">
                <c:set var="item" value="${selectedDiagnosticItem}"/>
                <article class="doctor-card medical-record-card">
                    <header class="medical-record-heading">
                        <div>
                            <strong><c:out value="${item.serviceNameSnapshot}"/></strong>
                            <span>Mã phiếu: <c:out value="${item.order.orderCode}"/></span>
                        </div>
                        <div><small>Hoàn tất lúc</small><c:out value="${item.completedAt}"/></div>
                    </header>
                    <div class="medical-record-grid">
                        <div><small>Bác sĩ chỉ định</small><p><c:out value="${item.order.encounter.doctor.fullName}"/></p></div>
                        <div><small>Yêu cầu thực hiện</small><p><c:out value="${item.order.clinicalIndication}" default="Không ghi"/></p></div>
                        <div class="record-prescription"><small>Kết quả / mô tả hình ảnh</small><p><c:out value="${item.result.findings}" default="Không ghi"/></p></div>
                        <div class="record-prescription"><small>Kết luận</small><p><c:out value="${item.result.conclusion}" default="Không ghi"/></p></div>
                        <div class="record-prescription"><small>Tệp kết quả</small>
                            <c:forEach var="file" items="${resultFiles}">
                                <a class="clinical-file" href="${ctx}/results/file?fileId=${file.fileId}" target="_blank" rel="noopener"><c:out value="${file.originalName}"/></a>
                            </c:forEach>
                            <c:if test="${empty resultFiles}"><p>Không có tệp đính kèm.</p></c:if>
                        </div>
                    </div>
                </article>
            </c:when>
            <c:otherwise>
            <c:set var="record" value="${selectedRecord}"/>
            <c:if test="${not empty record}">
                <article class="doctor-card medical-record-card">
                    <header class="medical-record-heading">
                        <div>
                            <strong>Ngày khám: <c:out value="${record.appointment.schedule.workDate}"/></strong>
                            <span>Mã lịch hẹn: <c:out value="${record.appointment.appointmentCode}"/></span>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${not empty record.appointment.slot}">
                                    <c:out value="${record.appointment.slot.startTime}"/> –
                                    <c:out value="${record.appointment.slot.endTime}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${record.appointment.schedule.startTime}"/> –
                                    <c:out value="${record.appointment.schedule.endTime}"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </header>
                    <div class="medical-record-grid">
                        <div><small>Lý do khám</small><p><c:out value="${record.appointment.reason}" default="Không ghi lý do"/></p></div>
                        <div><small>Chẩn đoán</small><p><c:out value="${record.diagnosis}"/></p></div>
                        <div><small>Ghi chú lâm sàng</small><p><c:out value="${record.clinicalNote}" default="Không có"/></p></div>
                        <div><small>Hướng điều trị</small><p><c:out value="${record.treatment}" default="Không có"/></p></div>
                        <div><small>Lời dặn</small><p><c:out value="${record.advice}" default="Không có"/></p></div>
                        <c:if test="${empty record.encounter}"><div class="record-prescription"><small>Đơn thuốc cũ</small><p><c:out value="${record.prescription}" default="Không kê đơn"/></p></div></c:if>
                        <c:if test="${not empty record.encounter && empty prescription}"><div><small>Đơn thuốc</small><p>Không kê đơn</p></div></c:if>
                    </div>
                </article>

                <jsp:include page="/WEB-INF/includes/clinical-assessment.jsp"/>
                <jsp:include page="/WEB-INF/includes/clinical-orders.jsp"/>
                <jsp:include page="/WEB-INF/includes/clinical-prescription.jsp"/>
            </c:if>
            </c:otherwise>
            </c:choose>
                <nav class="record-pagination" aria-label="Chuyển lần khám">
                    <c:choose>
                        <c:when test="${recordPage > 1}">
                            <c:url var="newerRecordUrl" value="/doctor/ho-so-benh-nhan">
                                <c:param name="patientId" value="${selectedPatient.patientId}"/>
                                <c:param name="recordPage" value="${recordPage - 1}"/>
                                <c:if test="${not empty keyword}"><c:param name="q" value="${keyword}"/></c:if>
                            </c:url>
                            <a class="btn btn-outline" href="${newerRecordUrl}"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Trang trước</a>
                        </c:when>
                        <c:otherwise><span class="btn btn-disabled" aria-disabled="true"><img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-left.png" alt="" aria-hidden="true"> Trang trước</span></c:otherwise>
                    </c:choose>

                    <strong>${paraclinical ? 'Lần thực hiện' : 'Lần khám'} <c:out value="${recordPage}"/> / <c:out value="${recordPageCount}"/></strong>

                    <c:choose>
                        <c:when test="${recordPage < recordPageCount}">
                            <c:url var="olderRecordUrl" value="/doctor/ho-so-benh-nhan">
                                <c:param name="patientId" value="${selectedPatient.patientId}"/>
                                <c:param name="recordPage" value="${recordPage + 1}"/>
                                <c:if test="${not empty keyword}"><c:param name="q" value="${keyword}"/></c:if>
                            </c:url>
                            <a class="btn btn-outline" href="${olderRecordUrl}">Trang sau <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></a>
                        </c:when>
                        <c:otherwise><span class="btn btn-disabled" aria-disabled="true">Trang sau <img class="ui-icon" src="${pageContext.request.contextPath}/images/icons/ui/arrow-right.png" alt="" aria-hidden="true"></span></c:otherwise>
                    </c:choose>
                </nav>
        </section>
    </c:if>
</main>

<jsp:include page="/WEB-INF/includes/footer-doctor.jsp"/>
