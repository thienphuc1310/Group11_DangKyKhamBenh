package murach.controller.doctor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import murach.DAO.*;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;
import murach.business.InitialAssessment;
import murach.business.MedicalEncounter;

public class DoctorExaminationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicalRecordDB medicalRecordDB = new MedicalRecordDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR) == null) return;
            Long doctorId = ClinicalWeb.profileId(request, "doctorId");
            Long encounterId = parseEncounterId(request);
            MedicalEncounter encounter = medicalRecordDB.findEncounterForDoctor(encounterId, doctorId);
            if (encounter == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy lượt khám");
                return;
            }
            InitialAssessment assessment = medicalRecordDB.findAssessmentForDoctor(encounterId, doctorId);
            if (assessment == null || !assessment.isCompleted())
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Chưa hoàn tất đánh giá ban đầu");
                return;
            }
            if (encounter.getStatus() == MedicalEncounter.Status.CANCELLED
                    || encounter.getStatus() == MedicalEncounter.Status.WAITING_ASSESSMENT
                    || encounter.getStatus() == MedicalEncounter.Status.ASSESSING)
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Lượt khám không ở bước bác sĩ xử lý");
                return;
            }

            request.setAttribute("encounter", encounter);
            request.setAttribute("appointment", encounter.getAppointment());
            request.setAttribute("assessment", assessment);
            request.setAttribute("medicalRecord", medicalRecordDB.findRecordForDoctor(encounterId, doctorId));
            request.setAttribute("orders", new DiagnosticOrderDB().findByEncounterForDoctor(encounterId, doctorId));
            request.setAttribute("resultFiles", new ResultFileDB().findForDoctor(encounterId, doctorId));
            request.setAttribute("prescription", new PrescriptionDB().findForDoctor(encounterId, doctorId));
            request.getRequestDispatcher("/WEB-INF/views/doctor/chi-tiet-buoi-kham.jsp")
                    .forward(request, response);
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR) == null) return;
            ClinicalWeb.requireCsrf(request);
            Long doctorId = ClinicalWeb.profileId(request, "doctorId");
            Long encounterId = parseEncounterId(request);
            String action = request.getParameter("action");
            if ("start".equals(action)) medicalRecordDB.beginConsultation(encounterId, doctorId);
            else if ("save".equals(action))
                medicalRecordDB.saveDraft(encounterId, doctorId, request.getParameter("diagnosis"),
                        request.getParameter("clinicalNote"), request.getParameter("treatment"), request.getParameter("advice"));
            else if ("finish".equals(action))
                medicalRecordDB.finalizeConsultation(encounterId, doctorId, request.getParameter("diagnosis"),
                        request.getParameter("clinicalNote"), request.getParameter("treatment"), request.getParameter("advice"));
            else throw new IllegalArgumentException("Thao tác không hợp lệ");
            response.sendRedirect(request.getContextPath()
                    + "/doctor/chi-tiet-buoi-kham?encounterId=" + encounterId);
        }
        catch (IllegalArgumentException e)
        {
            request.setAttribute("examError", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            doGet(request, response);
        }
        catch (IllegalStateException e)
        {
            request.setAttribute("examError", e.getMessage());
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            doGet(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    private Long parseEncounterId(HttpServletRequest request)
    {
        String value = request.getParameter("encounterId");
        try
        {
            Long id = value == null ? null : Long.valueOf(value.trim());
            if (id == null || id <= 0) throw new IllegalArgumentException();
            return id;
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Mã lượt khám không hợp lệ");
        }
    }
}
