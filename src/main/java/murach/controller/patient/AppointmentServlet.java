package murach.controller.patient;

import murach.DAO.AppointmentDB;
import murach.DAO.MedicalEncounterDB;
import murach.business.Account;
import murach.business.Appointment;
import murach.controller.clinical.ClinicalWeb;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class AppointmentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final AppointmentDB dao = new AppointmentDB();
    private final MedicalEncounterDB encounterDB = new MedicalEncounterDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            Long patientId = requirePatient(request, response);
            if (patientId == null) return;

            List<Appointment> items = dao.findByPatientId(patientId);
            request.setAttribute("appointments", items);
            // Ghép người 1: JSP có số khám/tiến trình thì Servlet phải nạp dữ liệu này.
            request.setAttribute("progressByAppointment", encounterDB.findProgressByPatientId(patientId));

            HttpSession session = request.getSession(false);
            request.setAttribute("successMessage", session.getAttribute("appointmentSuccess"));
            request.setAttribute("errorMessage", session.getAttribute("appointmentError"));
            session.removeAttribute("appointmentSuccess");
            session.removeAttribute("appointmentError");
            request.getRequestDispatcher("/WEB-INF/views/patient/lich-kham-cua-toi.jsp")
                    .forward(request, response);
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
            Long patientId = requirePatient(request, response);
            if (patientId == null) return;
            ClinicalWeb.requireCsrf(request);
            if (!"cancel".equals(request.getParameter("action")))
                throw new IllegalArgumentException("Thao tác không hợp lệ");

            try
            {
                Long appointmentId = ClinicalWeb.id(request, "appointmentId");
                dao.cancelAppointment(appointmentId, patientId);
                request.getSession(false).setAttribute("appointmentSuccess", "Huỷ lịch khám thành công.");
            }
            catch (IllegalArgumentException e)
            {
                request.getSession(false).setAttribute("appointmentError", e.getMessage());
            }
            // Redirect để tải lại danh sách, tránh gửi lại yêu cầu hủy khi refresh.
            response.sendRedirect(request.getContextPath() + "/patient/lich-kham-cua-toi");
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    private Long requirePatient(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return null;
        Long patientId = PatientSession.getPatientId(request);
        if (patientId == null) throw new SecurityException("Phiên đăng nhập thiếu hồ sơ bệnh nhân");
        return patientId;
    }
}
