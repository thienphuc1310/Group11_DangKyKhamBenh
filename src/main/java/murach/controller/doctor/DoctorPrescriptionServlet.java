package murach.controller.doctor;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.*;
import murach.business.*;
import murach.model.PrescriptionLineInput;
import murach.controller.clinical.ClinicalWeb;

public class DoctorPrescriptionServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final PrescriptionDB prescriptionDB = new PrescriptionDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR) == null) return;
            Long id = ClinicalWeb.id(request, "encounterId");
            Long doctorId = ClinicalWeb.profileId(request, "doctorId");
            MedicalRecordDB recordDB = new MedicalRecordDB();
            MedicalEncounter encounter = recordDB.findEncounterForDoctor(id, doctorId);
            if (encounter == null) { response.sendError(404); return; }
            InitialAssessment assessment = recordDB.findAssessmentForDoctor(id, doctorId);
            if (assessment == null || !assessment.isCompleted()) throw new IllegalStateException("Chưa hoàn tất đánh giá ban đầu");
            request.setAttribute("encounter", encounter);
            request.setAttribute("medicines", new MedicineDB().findAvailableMedicines());
            request.setAttribute("prescription", prescriptionDB.findForDoctor(id, doctorId));
            request.getRequestDispatcher("/WEB-INF/views/doctor/ke-don.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR) == null) return;
            ClinicalWeb.requireCsrf(request);
            Long id = ClinicalWeb.id(request, "encounterId");
            String[] names = {"medicineId", "quantity", "dosage", "frequency", "route", "durationDays", "instruction"};
            String[][] values = new String[names.length][];
            for (int i = 0; i < names.length; i++) values[i] = request.getParameterValues(names[i]);
            String[] medicineNames = request.getParameterValues("medicineName");
            if (values[0] == null || values[0].length > 50) throw new IllegalArgumentException("Đơn thuốc không hợp lệ");
            if (medicineNames != null && medicineNames.length != values[0].length)
                throw new IllegalArgumentException("Các dòng thuốc không đầy đủ");
            for (String[] row : values)
                if (row == null || row.length != values[0].length) throw new IllegalArgumentException("Các dòng thuốc không đầy đủ");
            List<PrescriptionLineInput> lines = new ArrayList<>();
            for (int i = 0; i < values[0].length; i++)
            {
                if (values[0][i].trim().isEmpty())
                {
                    if (medicineNames != null && !medicineNames[i].trim().isEmpty())
                        throw new IllegalArgumentException("Chọn thuốc từ danh sách gợi ý.");
                    continue;
                }
                lines.add(new PrescriptionLineInput(Long.valueOf(values[0][i]), Integer.parseInt(values[1][i]),
                        values[2][i], values[3][i], values[4][i], Integer.parseInt(values[5][i]), values[6][i]));
            }
            prescriptionDB.saveAndConfirm(id, ClinicalWeb.profileId(request, "doctorId"), lines, request.getParameter("advice"));
            response.sendRedirect(request.getContextPath() + "/doctor/chi-tiet-buoi-kham?encounterId=" + id);
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            request.setAttribute("formError", e.getMessage());
            response.setStatus(400);
            doGet(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
