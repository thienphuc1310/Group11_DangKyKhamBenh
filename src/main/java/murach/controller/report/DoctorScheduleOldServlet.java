package murach.controller.report;

import murach.DAO.DoctorScheduleDB;
import murach.business.DoctorSchedule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Bản cũ được giữ riêng để trình bày và chụp ảnh báo cáo.
 * Không sử dụng servlet này cho luồng nghiệp vụ chính thức.
 */
public class DoctorScheduleOldServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        Object sessionDoctorId = session == null ? null : session.getAttribute("doctorId");
        String doctorIdParameter = request.getParameter("doctorId");

        try
        {
            Long doctorId;
            if (sessionDoctorId instanceof Number)
            {
                doctorId = ((Number) sessionDoctorId).longValue();
            }
            else
            {
                doctorId = doctorIdParameter == null || doctorIdParameter.trim().isEmpty()
                        ? 1L
                        : Long.valueOf(doctorIdParameter.trim());
            }

            if (doctorId <= 0)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "doctorId phải lớn hơn 0");
                return;
            }

            List<DoctorSchedule> schedules = DoctorScheduleDB.findByDoctor(doctorId);
            request.setAttribute("schedules", schedules);
            request.setAttribute("doctorId", doctorId);
            request.getRequestDispatcher(
                    "/WEB-INF/views/report/lich-lam-viec-old.jsp")
                    .forward(request, response);
        }
        catch (NumberFormatException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "doctorId phải là một số nguyên");
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể tải lịch làm việc cũ của bác sĩ", e);
        }
    }
}
