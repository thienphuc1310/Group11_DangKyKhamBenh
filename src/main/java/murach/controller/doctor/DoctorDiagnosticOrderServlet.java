package murach.controller.doctor;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.*;
import murach.business.*;
import murach.controller.clinical.ClinicalWeb;

public class DoctorDiagnosticOrderServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicalRecordDB recordDB = new MedicalRecordDB();
    private final DiagnosticOrderDB orderDB = new DiagnosticOrderDB();
    private final DiagnosticServiceDB serviceDB = new DiagnosticServiceDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR) == null) return;
            Long encounterId = ClinicalWeb.id(request, "encounterId");
            Long doctorId = ClinicalWeb.profileId(request, "doctorId");
            MedicalEncounter encounter = recordDB.findEncounterForDoctor(encounterId, doctorId);
            if (encounter == null) { response.sendError(404); return; }
            InitialAssessment assessment = recordDB.findAssessmentForDoctor(encounterId, doctorId);
            if (assessment == null || !assessment.isCompleted()) throw new IllegalStateException("Chưa hoàn tất đánh giá ban đầu");

            List<DiagnosticService> services = serviceDB.findActiveServices();

            Set<Long> availableIds = new HashSet<>();
            for (DiagnosticService service : serviceDB.findAvailableServices())
                availableIds.add(service.getServiceId());
            Map<Long, Boolean> availability = new HashMap<>();
            int availableCount = 0;
            for (DiagnosticService service : services)
            {
                boolean available = availableIds.contains(service.getServiceId());
                availability.put(service.getServiceId(), available);
                if (available) availableCount++;
            }
            // Chỉ phục hồi lựa chọn để hiển thị lỗi form; POST vẫn kiểm tra lại tại DAO.
            Map<Long, Boolean> selectedServices = new HashMap<>();
            String[] selected = request.getParameterValues("serviceIds");
            if (selected != null) for (String value : selected)
            {
                try { selectedServices.put(Long.valueOf(value), Boolean.TRUE); }
                catch (NumberFormatException ignored) { /* Giá trị giả mạo không dùng để đánh dấu ô chọn. */ }
            }
            request.setAttribute("encounter", encounter);
            request.setAttribute("services", services);
            request.setAttribute("serviceAvailability", availability);
            request.setAttribute("selectedServices", selectedServices);
            request.setAttribute("availableServiceCount", availableCount);
            request.setAttribute("serviceDate", ClinicalAvailabilityDB.today());
            request.setAttribute("orders", orderDB.findByEncounterForDoctor(encounterId, doctorId));
            request.getRequestDispatcher("/WEB-INF/views/doctor/chi-dinh.jsp").forward(request, response);
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
            Long encounterId = ClinicalWeb.id(request, "encounterId");
            String[] values = request.getParameterValues("serviceIds");
            List<Long> ids = new ArrayList<>();
            if (values != null) for (String value : values) ids.add(Long.valueOf(value));
            orderDB.createAndConfirm(encounterId, ClinicalWeb.profileId(request, "doctorId"), ids,
                    request.getParameter("clinicalIndication"));
            response.sendRedirect(request.getContextPath() + "/doctor/chi-tiet-buoi-kham?encounterId=" + encounterId);
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
