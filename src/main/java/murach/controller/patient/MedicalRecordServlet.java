package murach.controller.patient;

import murach.DAO.MedicalRecordDB;
import murach.DAO.*;
import murach.business.Account;
import murach.business.Prescription;
import murach.controller.clinical.ClinicalWeb;
import murach.business.MedicalRecord;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MedicalRecordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final MedicalRecordDB dao = new MedicalRecordDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return;
            Long patientId = ClinicalWeb.profileId(request, "patientId");
            if (patientId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // 1. LẤY THAM SỐ TỪ FORM LỌC
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            LocalDate startDate = null;
            LocalDate endDate = null;

            try {
                if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                    startDate = LocalDate.parse(startDateStr);
                }
                if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                    endDate = LocalDate.parse(endDateStr);
                }
            } catch (DateTimeParseException e) {
                // Bỏ qua lỗi parse ngày
            }

            // 2. GỌI DB ĐỂ LẤY DANH SÁCH ĐÃ LỌC
            List<MedicalRecord> items = dao.findByPatientIdAndDateRange(patientId, startDate, endDate);

            // 3. XỬ LÝ SỰ KIỆN CLICK CHỌN HỒ SƠ
            String recordIdStr = request.getParameter("recordId");
            MedicalRecord selected = items.isEmpty() ? null : items.get(0); // Mặc định lấy cái đầu tiên

            if (recordIdStr != null && !recordIdStr.trim().isEmpty()) {
                try {
                    Long recordId = Long.valueOf(recordIdStr);
                    selected = items.stream()
                            .filter(r -> r.getRecordId().equals(recordId))
                            .findFirst()
                            .orElse(selected);
                } catch (NumberFormatException e) {
                    // Bỏ qua
                }
            }

            // 4. GỬI DỮ LIỆU SANG JSP
            request.setAttribute("medicalRecords", items);
            request.setAttribute("selectedRecord", selected);
            if (selected != null && selected.getEncounter() != null)
            {
                Long encounterId = selected.getEncounter().getEncounterId();
                request.setAttribute("assessment", dao.findAssessmentForPatient(encounterId, patientId));
                request.setAttribute("orders", new DiagnosticOrderDB().findByEncounterForPatient(encounterId, patientId));
                request.setAttribute("resultFiles", new ResultFileDB().findReleasedByPatientId(patientId));
                for (Prescription prescription : new PrescriptionDB().findConfirmedByPatientId(patientId))
                    if (encounterId.equals(prescription.getEncounter().getEncounterId()))
                        request.setAttribute("prescription", prescription);
            }
            request.setAttribute("startDate", startDateStr); // Để giữ text trên ô input
            request.setAttribute("endDate", endDateStr);

            request.getRequestDispatcher("/WEB-INF/views/patient/ho-so-benh-an.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            ClinicalWeb.fail(response, e);
        }
    }
}
