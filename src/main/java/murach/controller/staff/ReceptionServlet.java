package murach.controller.staff;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.MedicalEncounterDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class ReceptionServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicalEncounterDB encounterDB = new MedicalEncounterDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            if (!requireReceptionist(request, response)) return;
            String value = request.getParameter("workDate");
            LocalDate selectedDate;
            if (value == null || value.trim().isEmpty())
                selectedDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            else
                selectedDate = LocalDate.parse(value.trim());

            String keyword = request.getParameter("q");
            HttpSession session = request.getSession(false);
            Long staffId = ClinicalWeb.profileId(request, "staffId");
            Long userId = ((Number) session.getAttribute("userId")).longValue();
            request.setAttribute("appointments", encounterDB.findPaidAppointments(selectedDate, keyword, staffId, userId));
            request.setAttribute("selectedDate", selectedDate);
            request.setAttribute("today", LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            request.setAttribute("keyword", keyword);
            request.getRequestDispatcher("/WEB-INF/views/staff/recive-patient.jsp")
                    .forward(request, response);
        }
        catch (DateTimeParseException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ngày khám không hợp lệ");
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
            if (!requireReceptionist(request, response)) return;
            ClinicalWeb.requireCsrf(request);
            Long appointmentId = ClinicalWeb.id(request, "appointmentId");
            HttpSession session = request.getSession(false);
            Long staffId = ClinicalWeb.profileId(request, "staffId");
            Long userId = ((Number) session.getAttribute("userId")).longValue();
            Long encounterId = encounterDB.checkIn(appointmentId, staffId, userId);
            response.sendRedirect(request.getContextPath() + "/staff/reception?received=1&encounterId=" + encounterId);
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch (IllegalStateException e)
        {
            response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    private boolean requireReceptionist(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Number))
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return false;
        }
        if (!"STAFF".equals(session.getAttribute("role"))
                || !"RECEPTIONIST".equals(session.getAttribute("staffJobType"))
                || !(session.getAttribute("staffId") instanceof Number))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chức năng dành cho tiếp tân");
            return false;
        }
        return ClinicalWeb.requireUser(request, response, Account.Role.STAFF) != null;
    }
}
