package murach.controller.admin;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.ClinicalAvailabilityDB;
import murach.DAO.StaffScheduleDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class AdminStaffScheduleServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final StaffScheduleDB scheduleDB = new StaffScheduleDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) == null) return;
            Account.Role role = readRole(request);
            LocalDate from = readDate(request.getParameter("fromDate"), ClinicalAvailabilityDB.today());
            LocalDate to = readDate(request.getParameter("toDate"), from.plusMonths(1).minusDays(1));
            validateRange(from, to);
            Long profileId = readId(request.getParameter("profileId"), false);
            loadPage(request, role, profileId, from, to);
            request.getRequestDispatcher("/WEB-INF/views/admin/staff-schedule-management.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long actorUserId = ClinicalWeb.requireUser(request, response, Account.Role.ADMIN);
            if (actorUserId == null) return;
            ClinicalWeb.requireCsrf(request);
            Account.Role role = readRole(request);
            Long profileId = readId(request.getParameter("profileId"), true);
            LocalDate from = readDate(request.getParameter("fromDate"), null);
            LocalDate to = readDate(request.getParameter("toDate"), null);
            validateRange(from, to);
            Set<DayOfWeek> weekdays = EnumSet.noneOf(DayOfWeek.class);
            String[] selectedDays = request.getParameterValues("weekdays");
            if (selectedDays != null) for (String value : selectedDays)
            {
                try { weekdays.add(DayOfWeek.valueOf(value)); }
                catch (IllegalArgumentException e) { throw new IllegalArgumentException("Thứ trong tuần không hợp lệ"); }
            }
            Set<String> shifts = new HashSet<>();
            String[] selectedShifts = request.getParameterValues("shifts");
            if (selectedShifts != null) for (String shift : selectedShifts)
            {
                if (!"MORNING".equals(shift) && !"AFTERNOON".equals(shift))
                    throw new IllegalArgumentException("Ca làm việc không hợp lệ");
                shifts.add(shift);
            }
            Map<String, Integer> result = scheduleDB.createRecurring(role, profileId, from, to, weekdays,
                    shifts.contains("MORNING"), shifts.contains("AFTERNOON"), request.getParameter("room"), actorUserId);
            response.sendRedirect(request.getContextPath() + request.getServletPath() + "?fromDate=" + from + "&toDate=" + to
                    + "&profileId=" + profileId + "&created=" + result.get("created") + "&skipped=" + result.get("skipped"));
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    private void loadPage(HttpServletRequest request, Account.Role role, Long profileId, LocalDate from, LocalDate to)
    {
        request.setAttribute("scheduleRole", role.name());
        request.setAttribute("scheduleLabel", role == Account.Role.STAFF ? "tiếp tân" : role == Account.Role.NURSE ? "điều dưỡng" : "chuyên viên kỹ thuật");
        request.setAttribute("schedulePath", request.getServletPath());
        request.setAttribute("fromDate", from); request.setAttribute("toDate", to);
        request.setAttribute("selectedProfileId", profileId);
        if (role == Account.Role.NURSE)
        {
            request.setAttribute("people", scheduleDB.findNurses());
            request.setAttribute("schedules", scheduleDB.findNurseSchedules(profileId, null, from, to));
        }
        else if (role == Account.Role.TECHNICIAN)
        {
            request.setAttribute("people", scheduleDB.findTechnicians());
            request.setAttribute("schedules", scheduleDB.findTechnicianSchedules(profileId, null, from, to));
        }
        else
        {
            request.setAttribute("people", scheduleDB.findReceptionists());
            request.setAttribute("schedules", scheduleDB.findReceptionistSchedules(profileId, null, from, to));
        }
    }

    private static Account.Role readRole(HttpServletRequest request)
    {
        if ("/admin/nurse-schedules".equals(request.getServletPath())) return Account.Role.NURSE;
        if ("/admin/technician-schedules".equals(request.getServletPath())) return Account.Role.TECHNICIAN;
        if ("/admin/receptionist-schedules".equals(request.getServletPath())) return Account.Role.STAFF;
        throw new IllegalArgumentException("Trang phân lịch không hợp lệ");
    }

    private static LocalDate readDate(String value, LocalDate fallback)
    {
        if (value == null || value.trim().isEmpty())
        {
            if (fallback != null) return fallback;
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và kết thúc");
        }
        try { return LocalDate.parse(value.trim()); }
        catch (DateTimeException e) { throw new IllegalArgumentException("Ngày không hợp lệ"); }
    }

    private static Long readId(String value, boolean required)
    {
        if (!required && (value == null || value.trim().isEmpty())) return null;
        try
        {
            long id = Long.parseLong(value == null ? "" : value.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Vui lòng chọn nhân sự hợp lệ"); }
    }

    private static void validateRange(LocalDate from, LocalDate to)
    {
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 366)
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ, tối đa 12 tháng");
    }
}
