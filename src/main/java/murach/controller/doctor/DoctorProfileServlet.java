package murach.controller.doctor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.DoctorDB;
import murach.DAO.SpecialtyDB;
import murach.business.Doctor;

public class DoctorProfileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DoctorDB doctorDB = new DoctorDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Long doctorId = getDoctorId(request, response);
        if (doctorId == null) return;

        try {
            Doctor profile = doctorDB.findById(doctorId);
            if (profile == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ bác sĩ");
                return;
            }
            request.setAttribute("profile", profile);
            request.setAttribute("specialties", specialtyDB.getAllSpecialties());
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));
            request.getRequestDispatcher("/WEB-INF/views/doctor/thong-tin-ca-nhan.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải thông tin cá nhân của bác sĩ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Long doctorId = getDoctorId(request, response);
        if (doctorId == null) return;

        try {
            Doctor profile = doctorDB.updateProfile(
                    doctorId,
                    request.getParameter("fullName"),
                    request.getParameter("phone"),
                    request.getParameter("email"),
                    parseDate(request.getParameter("birthDate")),
                    request.getParameter("gender"),
                    request.getParameter("address"),
                    parseRequiredLong(request.getParameter("specialtyId"), "Vui lòng chọn chuyên khoa"),
                    request.getParameter("licenseNumber"),
                    request.getParameter("qualification"),
                    parseInteger(request.getParameter("experienceYears")),
                    request.getParameter("description"));

            request.getSession().setAttribute("currentUser", profile.getUser());
            response.sendRedirect(request.getContextPath() + "/doctor/profile?updated=1");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể cập nhật thông tin bác sĩ", e);
        }
    }

    private Long getDoctorId(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !"DOCTOR".equals(session.getAttribute("role"))
                || !(session.getAttribute("doctorId") instanceof Number)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return null;
        }
        return ((Number) session.getAttribute("doctorId")).longValue();
    }

    private static LocalDate parseDate(String value)
    {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim());
    }

    private static Integer parseInteger(String value)
    {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số năm kinh nghiệm không hợp lệ");
        }
    }

    private static Long parseRequiredLong(String value, String message)
    {
        try {
            if (value == null || value.trim().isEmpty()) throw new NumberFormatException();
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }
}
