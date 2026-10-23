package murach.controller.doctor;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.MedicalRecordDB;
import murach.DAO.DiagnosticOrderDB;
import murach.DAO.ResultFileDB;
import murach.DAO.PrescriptionDB;
import murach.DAO.DoctorDB;
import murach.DAO.DiagnosticOrderItemDB;
import murach.business.Account;
import murach.business.Doctor;
import murach.business.Specialty;
import murach.business.DiagnosticOrderItem;
import murach.controller.clinical.ClinicalWeb;
import murach.business.MedicalRecord;
import murach.business.Patient;

public class DoctorPatientRecordServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicalRecordDB medicalRecordDB = new MedicalRecordDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !"DOCTOR".equals(session.getAttribute("role"))
                || !(session.getAttribute("doctorId") instanceof Number))
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }

        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            Long doctorId = ((Number) session.getAttribute("doctorId")).longValue();
            Doctor doctor = new DoctorDB().findById(doctorId);
            if (doctor == null || !userId.equals(doctor.getUserId()))
                throw new SecurityException("Hồ sơ không thuộc bác sĩ đang đăng nhập");
            boolean paraclinical = doctor.getSpecialty().getSpecialtyType() == Specialty.SpecialtyType.PARACLINICAL;
            request.setAttribute("paraclinical", paraclinical);
            if (paraclinical)
            {
                loadDiagnosticRecords(request, response, userId);
                return;
            }
            String keyword = request.getParameter("q");
            String normalizedKeyword = normalize(keyword);
            List<MedicalRecord> allRecords = medicalRecordDB.findCompletedByDoctorId(doctorId);

            Map<Long, Patient> uniquePatients = new LinkedHashMap<>();
            Map<Long, Integer> recordCounts = new LinkedHashMap<>();
            Map<Long, LocalDate> latestDates = new LinkedHashMap<>();

            for (MedicalRecord record : allRecords)
            {
                Patient patient = record.getAppointment().getPatient();
                if (!matches(patient, normalizedKeyword)) continue;

                Long patientId = patient.getPatientId();
                uniquePatients.putIfAbsent(patientId, patient);
                recordCounts.merge(patientId, 1, Integer::sum);
                latestDates.putIfAbsent(patientId,
                        record.getAppointment().getSchedule().getWorkDate());
            }

            Patient selectedPatient = null;
            List<MedicalRecord> selectedRecords = new ArrayList<>();
            MedicalRecord selectedRecord = null;
            int recordPage = 1;
            int recordPageCount = 0;
            String patientIdValue = request.getParameter("patientId");
            if (patientIdValue != null && !patientIdValue.trim().isEmpty())
            {
                Long selectedId = Long.valueOf(patientIdValue.trim());
                for (MedicalRecord record : allRecords)
                {
                    Patient patient = record.getAppointment().getPatient();
                    if (selectedId.equals(patient.getPatientId()))
                    {
                        if (selectedPatient == null) selectedPatient = patient;
                        selectedRecords.add(record);
                    }
                }
                if (selectedPatient == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy hồ sơ bệnh nhân thuộc bác sĩ đang đăng nhập");
                    return;
                }

                recordPageCount = selectedRecords.size();
                String recordPageValue = request.getParameter("recordPage");
                if (recordPageValue != null && !recordPageValue.trim().isEmpty())
                    recordPage = Integer.parseInt(recordPageValue.trim());
                if (recordPage < 1 || recordPage > recordPageCount)
                {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Trang lịch sử khám không hợp lệ");
                    return;
                }
                selectedRecord = selectedRecords.get(recordPage - 1);
            }

            request.setAttribute("patients", new ArrayList<>(uniquePatients.values()));
            request.setAttribute("recordCounts", recordCounts);
            request.setAttribute("latestDates", latestDates);
            request.setAttribute("selectedPatient", selectedPatient);
            request.setAttribute("selectedRecord", selectedRecord);
            request.setAttribute("recordPage", recordPage);
            request.setAttribute("recordPageCount", recordPageCount);
            request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
            if (selectedRecord != null && selectedRecord.getEncounter() != null)
            {
                Long encounterId = selectedRecord.getEncounter().getEncounterId();
                request.setAttribute("assessment", medicalRecordDB.findAssessmentForDoctor(encounterId, doctorId));
                request.setAttribute("orders", new DiagnosticOrderDB().findByEncounterForDoctor(encounterId, doctorId));
                request.setAttribute("resultFiles", new ResultFileDB().findForDoctor(encounterId, doctorId));
                request.setAttribute("prescription", new PrescriptionDB().findForDoctor(encounterId, doctorId));
            }
            request.getRequestDispatcher("/WEB-INF/views/doctor/ho-so-benh-nhan.jsp")
                    .forward(request, response);
        }
        catch (NumberFormatException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã bệnh nhân không hợp lệ");
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    // Bác sĩ cận lâm sàng xem hồ sơ và kết quả mình đã thực hiện, không mở rộng
    // quyền đọc toàn bộ bệnh án hoặc các phiếu đang thuộc bác sĩ khác.
    private void loadDiagnosticRecords(HttpServletRequest request, HttpServletResponse response, Long userId)
            throws ServletException, IOException
    {
        List<DiagnosticOrderItem> allItems = new DiagnosticOrderItemDB().findCompletedForDoctor(userId);
        String keyword = request.getParameter("q");
        Map<Long, Patient> patients = new LinkedHashMap<>();
        Map<Long, Integer> counts = new LinkedHashMap<>();
        Map<Long, LocalDate> dates = new LinkedHashMap<>();
        for (DiagnosticOrderItem item : allItems)
        {
            Patient patient = item.getOrder().getEncounter().getPatient();
            if (!matches(patient, normalize(keyword))) continue;
            Long id = patient.getPatientId();
            patients.putIfAbsent(id, patient);
            counts.merge(id, 1, Integer::sum);
            dates.putIfAbsent(id, item.getCompletedAt().toLocalDate());
        }

        Patient selectedPatient = null;
        DiagnosticOrderItem selectedItem = null;
        int page = 1, pageCount = 0;
        String patientIdValue = request.getParameter("patientId");
        if (patientIdValue != null && !patientIdValue.trim().isEmpty())
        {
            Long patientId = ClinicalWeb.id(request, "patientId");
            List<DiagnosticOrderItem> history = new ArrayList<>();
            for (DiagnosticOrderItem item : allItems)
            {
                if (!patientId.equals(item.getOrder().getEncounter().getPatient().getPatientId())) continue;
                history.add(item);
            }
            if (history.isEmpty())
            {
                response.sendError(404, "Không tìm thấy hồ sơ bệnh nhân đã thực hiện với bác sĩ này");
                return;
            }
            String pageValue = request.getParameter("recordPage");
            if (pageValue != null && !pageValue.trim().isEmpty()) page = Integer.parseInt(pageValue.trim());
            pageCount = history.size();
            if (page < 1 || page > pageCount)
            {
                response.sendError(400, "Trang lịch sử thực hiện không hợp lệ");
                return;
            }
            selectedItem = history.get(page - 1);
            selectedPatient = selectedItem.getOrder().getEncounter().getPatient();
            request.setAttribute("resultFiles", new ResultFileDB().findForPerformer(selectedItem.getOrderItemId(), userId));
        }
        request.setAttribute("patients", new ArrayList<>(patients.values()));
        request.setAttribute("recordCounts", counts); request.setAttribute("latestDates", dates);
        request.setAttribute("selectedPatient", selectedPatient); request.setAttribute("selectedDiagnosticItem", selectedItem);
        request.setAttribute("recordPage", page); request.setAttribute("recordPageCount", pageCount);
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.getRequestDispatcher("/WEB-INF/views/doctor/ho-so-benh-nhan.jsp").forward(request, response);
    }

    private boolean matches(Patient patient, String keyword)
    {
        if (keyword.isEmpty()) return true;
        return normalize(patient.getPatientCode()).contains(keyword)
                || normalize(patient.getFullName()).contains(keyword)
                || normalize(patient.getPhone()).contains(keyword);
    }

    private String normalize(String value)
    {
        if (value == null) return "";
        String result = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        return result.trim();
    }
}
