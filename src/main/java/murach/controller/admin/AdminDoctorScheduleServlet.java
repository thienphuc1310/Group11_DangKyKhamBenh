package murach.controller.admin;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.DoctorDB;
import murach.DAO.DoctorScheduleDB;
import murach.DAO.DoctorScheduleDB.RecurringScheduleResult;

/** ADMIN phân lịch làm việc theo khoảng ngày cho bác sĩ. */
public class AdminDoctorScheduleServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!requireAdmin(request, response)) return;
        loadPageData(request);
        request.getRequestDispatcher("/WEB-INF/views/admin/doctor-schedule-management.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!requireAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        try
        {
            Long doctorId = Long.valueOf(require(request, "doctorId"));
            LocalDate fromDate = LocalDate.parse(require(request, "fromDate"));
            LocalDate toDate = LocalDate.parse(require(request, "toDate"));
            String room = require(request, "room");

            if (toDate.isBefore(fromDate))
                throw new IllegalArgumentException("Ngày kết thúc phải từ ngày bắt đầu trở đi");
            if (ChronoUnit.DAYS.between(fromDate, toDate) > 366)
                throw new IllegalArgumentException("Mỗi lần chỉ được phân lịch tối đa 12 tháng");

            Set<DayOfWeek> weekdays = parseWeekdays(request.getParameterValues("weekdays"));
            boolean morning = hasValue(request.getParameterValues("shifts"), "MORNING");
            boolean afternoon = hasValue(request.getParameterValues("shifts"), "AFTERNOON");

            if (weekdays.isEmpty())
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một ngày trong tuần");
            if (!morning && !afternoon)
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một ca làm việc");

            RecurringScheduleResult result = DoctorScheduleDB.createRecurringSchedules(
                    doctorId, fromDate, toDate, weekdays, morning, afternoon, room);

            response.sendRedirect(request.getContextPath()
                    + "/admin/doctor-schedules?created=" + result.getCreatedCount()
                    + "&skipped=" + result.getSkippedCount());
        }
        catch (IllegalArgumentException | DateTimeParseException e)
        {
            request.setAttribute("error", e.getMessage());
            loadPageData(request);
            request.getRequestDispatcher("/WEB-INF/views/admin/doctor-schedule-management.jsp")
                    .forward(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể phân lịch làm việc", e);
        }
    }

    private void loadPageData(HttpServletRequest request)
    {
        request.setAttribute("doctors", new DoctorDB().getAllDoctors());
        request.setAttribute("schedules", DoctorScheduleDB.findAll());
    }

    private Set<DayOfWeek> parseWeekdays(String[] values)
    {
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        if (values == null) return result;
        for (String value : values) result.add(DayOfWeek.valueOf(value));
        return result;
    }

    private boolean hasValue(String[] values, String expected)
    {
        if (values == null) return false;
        for (String value : values)
            if (expected.equals(value)) return true;
        return false;
    }

    private String require(HttpServletRequest request, String name)
    {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin");
        return value.trim();
    }

    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null)
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return false;
        }
        if (!"ADMIN".equals(session.getAttribute("role")))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ quản trị viên được phân lịch làm việc cho bác sĩ.");
            return false;
        }
        return true;
    }
}
