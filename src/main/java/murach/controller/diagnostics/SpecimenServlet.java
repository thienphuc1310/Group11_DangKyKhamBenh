package murach.controller.diagnostics;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.SpecimenDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class SpecimenServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final SpecimenDB specimenDB = new SpecimenDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.TECHNICIAN) == null) return;
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + ClinicalWeb.id(request, "itemId"));
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.TECHNICIAN);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            Long itemId = ClinicalWeb.id(request, "itemId");
            String action = request.getParameter("action");
            if ("collect".equals(action)) specimenDB.collect(itemId, userId, request.getParameter("specimenType"));
            else if ("receive".equals(action)) specimenDB.receive(ClinicalWeb.id(request, "specimenId"), userId);
            else if ("reject".equals(action))
            {
                String reason = request.getParameter("reason");
                if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("Cần nhập lý do từ chối");
                specimenDB.reject(ClinicalWeb.id(request, "specimenId"), userId, reason);
            }
            else throw new IllegalArgumentException("Thao tác không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + itemId);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
