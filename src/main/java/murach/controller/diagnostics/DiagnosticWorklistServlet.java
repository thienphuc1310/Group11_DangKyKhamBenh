package murach.controller.diagnostics;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DiagnosticOrderItemDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class DiagnosticWorklistServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DiagnosticOrderItemDB itemDB = new DiagnosticOrderItemDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.TECHNICIAN, Account.Role.DOCTOR);
            if (userId == null) return;
            Account.Role role = ClinicalWeb.role(request);
            if (role == Account.Role.DOCTOR)
            {
                response.sendRedirect(request.getContextPath() + "/doctor/lich-hen");
                return;
            }
            request.setAttribute("items", itemDB.findWorklist(userId, role));
            request.getRequestDispatcher("/WEB-INF/views/technician/danh-sach-xet-nghiem.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.TECHNICIAN, Account.Role.DOCTOR);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            Long itemId = ClinicalWeb.id(request, "itemId");
            String action = request.getParameter("action");
            if ("accept".equals(action)) itemDB.accept(itemId, userId, ClinicalWeb.role(request));
            else if ("start".equals(action)) itemDB.startProcessing(itemId, userId, ClinicalWeb.role(request));
            else throw new IllegalArgumentException("Thao tác không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + itemId);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
