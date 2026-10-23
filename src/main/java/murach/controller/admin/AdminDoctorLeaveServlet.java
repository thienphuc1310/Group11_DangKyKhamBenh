package murach.controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DoctorLeaveDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class AdminDoctorLeaveServlet extends HttpServlet
{
    private final DoctorLeaveDB leaveDB = new DoctorLeaveDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.ADMIN);
            if (userId == null) return;
            request.setAttribute("leaves", leaveDB.findForAdmin(userId));
            request.getRequestDispatcher("/WEB-INF/views/admin/doctor-leaves.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.ADMIN);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            String action = request.getParameter("action");
            if (!"approve".equals(action) && !"reject".equals(action))
                throw new IllegalArgumentException("Thao tác không hợp lệ");
            leaveDB.review(userId, ClinicalWeb.id(request, "leaveId"), "approve".equals(action));
            String nextPage = "notifications".equals(request.getParameter("returnTo"))
                    ? "/notifications?reviewed=1" : "/admin/doctor-leaves?saved=1";
            response.sendRedirect(request.getContextPath() + nextPage);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
