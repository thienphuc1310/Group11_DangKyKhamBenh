package murach.controller.clinical;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.ClinicalAvailabilityDB;
import murach.DAO.StaffScheduleDB;
import murach.business.*;

public class StaffWorkScheduleServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final StaffScheduleDB scheduleDB = new StaffScheduleDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.NURSE, Account.Role.TECHNICIAN, Account.Role.STAFF);
            if (userId == null) return;
            boolean nursePage = "/nurse/schedule".equals(request.getServletPath());
            boolean receptionistPage = "/staff/schedule".equals(request.getServletPath());
            if (!nursePage && !receptionistPage && !"/technician/schedule".equals(request.getServletPath()))
                throw new IllegalArgumentException("Trang lịch làm việc không hợp lệ");
            Account.Role role = receptionistPage ? Account.Role.STAFF : nursePage ? Account.Role.NURSE : Account.Role.TECHNICIAN;
            if (ClinicalWeb.role(request) != role)
                throw new SecurityException("Bạn không có quyền xem lịch của vai trò này");
            Long profileId = ClinicalWeb.profileId(request, receptionistPage ? "staffId" : nursePage ? "nurseId" : "technicianId");
            LocalDate today = ClinicalAvailabilityDB.today(), date = today;
            String value = request.getParameter("date");
            if (value != null && !value.trim().isEmpty())
            {
                try { date = LocalDate.parse(value); }
                catch (DateTimeException e) { throw new IllegalArgumentException("Ngày xem lịch không hợp lệ"); }
            }
            LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), end = start.plusDays(6);
            List<?> schedules;
            if (receptionistPage) schedules = scheduleDB.findReceptionistSchedules(profileId, userId, start, end);
            else if (nursePage) schedules = scheduleDB.findNurseSchedules(profileId, userId, start, end);
            else schedules = scheduleDB.findTechnicianSchedules(profileId, userId, start, end);
            List<LocalDate> days = new ArrayList<>();
            Map<LocalDate, List<Object>> byDate = new LinkedHashMap<>();
            for (int index = 0; index < 7; index++)
            {
                LocalDate day = start.plusDays(index);
                days.add(day); byDate.put(day, new ArrayList<>());
            }
            for (Object schedule : schedules)
            {
                LocalDate day = receptionistPage ? ((StaffSchedule)schedule).getWorkDate()
                        : nursePage ? ((NurseSchedule)schedule).getWorkDate() : ((TechnicianSchedule)schedule).getWorkDate();
                byDate.get(day).add(schedule);
            }
            request.setAttribute("days", days); request.setAttribute("schedulesByDate", byDate);
            request.setAttribute("hasSchedules", !schedules.isEmpty());
            request.setAttribute("dayLabels", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            request.setAttribute("weekStart", start); request.setAttribute("weekEnd", end);
            request.setAttribute("previousDate", start.minusWeeks(1)); request.setAttribute("nextDate", start.plusWeeks(1));
            request.setAttribute("selectedDate", date); request.setAttribute("today", today);
            request.setAttribute("schedulePath", request.getServletPath());
            request.getRequestDispatcher("/WEB-INF/views/clinical/work-schedule.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
