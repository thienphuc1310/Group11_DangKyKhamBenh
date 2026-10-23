package murach.controller.clinical;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.NotificationDB;
import murach.business.Account;

public class NotificationServlet extends HttpServlet
{
    private final NotificationDB notificationDB = new NotificationDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.ADMIN, Account.Role.DOCTOR, Account.Role.PATIENT);
            if (userId == null) return;
            if ("1".equals(request.getParameter("count")))
            {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().print("{\"unread\":" + notificationDB.countUnread(userId) + "}");
                return;
            }
            request.setAttribute("notifications", notificationDB.findForUser(userId));
            request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.ADMIN, Account.Role.DOCTOR, Account.Role.PATIENT);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            notificationDB.markRead(userId, ClinicalWeb.id(request, "notificationId"));
            response.sendRedirect(request.getContextPath() + "/notifications");
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
