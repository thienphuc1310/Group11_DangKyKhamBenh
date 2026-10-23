package murach.controller.patient;

import murach.DAO.PatientDB;
import murach.business.Patient;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PatientDB dao = new PatientDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long patientId = PatientSession.getPatientId(request);
            if (patientId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            Patient profile = dao.findById(patientId);
            if (profile == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bệnh nhân");
                return;
            }
            request.setAttribute("profile", profile);
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));
            request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải thông tin cá nhân", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long patientId = PatientSession.getPatientId(request);
        if (patientId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            String birth = request.getParameter("birthDate");
            LocalDate birthDate = birth == null || birth.trim().isEmpty()
                    ? null : LocalDate.parse(birth.trim());
            dao.updateProfile(patientId, request.getParameter("fullName"), request.getParameter("phone"),
                    request.getParameter("email"), birthDate, request.getParameter("gender"),
                    request.getParameter("address"));
            response.sendRedirect(request.getContextPath() + "/patient/profile?updated=1");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể lưu thông tin cá nhân", e);
        }
    }
}
