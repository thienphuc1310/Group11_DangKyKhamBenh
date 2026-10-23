package murach.controller.doctor;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DoctorLeaveDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class DoctorLeaveServlet extends HttpServlet
{
    private final DoctorLeaveDB leaveDB = new DoctorLeaveDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            boolean replacements = request.getServletPath().equals("/doctor/replacements");
            request.setAttribute("replacementPage", replacements);
            request.setAttribute("leaves", leaveDB.findForDoctor(userId));
            if (replacements) request.setAttribute("offers", leaveDB.findOpenReplacements(userId));
            else request.setAttribute("schedules", leaveDB.findRequestableSchedules(userId));
            request.getRequestDispatcher("/WEB-INF/views/doctor/leave-requests.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            boolean replacements = request.getServletPath().equals("/doctor/replacements");
            String[] values = request.getParameterValues(replacements ? "leaveId" : "scheduleId");
            if (values == null || values.length < 1 || values.length > 2)
                throw new IllegalArgumentException("Chọn một hoặc hai ca trong cùng ngày");
            List<Long> ids = new ArrayList<>();
            for (String value : values)
            {
                try { ids.add(Long.valueOf(value)); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Mã ca không hợp lệ"); }
            }
            if (replacements) leaveDB.accept(userId, ids);
            else leaveDB.submit(userId, ids, request.getParameter("reason"));
            response.sendRedirect(request.getContextPath() + request.getServletPath() + "?saved=1");
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
