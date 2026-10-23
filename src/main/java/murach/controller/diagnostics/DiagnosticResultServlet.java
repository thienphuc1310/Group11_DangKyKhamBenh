package murach.controller.diagnostics;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.*;
import murach.business.*;
import murach.controller.clinical.ClinicalWeb;

public class DiagnosticResultServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DiagnosticResultDB resultDB = new DiagnosticResultDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.TECHNICIAN, Account.Role.DOCTOR);
            if (userId == null) return;
            Long itemId = ClinicalWeb.id(request, "itemId");
            DiagnosticOrderItem item = new DiagnosticOrderItemDB().findForPerformer(itemId, userId);
            if (item == null) { response.sendError(404); return; }
            request.setAttribute("item", item);
            request.setAttribute("result", item.getResult());
            request.setAttribute("specimenLinks", new SpecimenDB().findForPerformer(itemId, userId));
            request.setAttribute("resultFiles", new ResultFileDB().findForPerformer(itemId, userId));
            request.getRequestDispatcher(ClinicalWeb.role(request) == Account.Role.TECHNICIAN
                    ? "/WEB-INF/views/technician/thuc-hien-xet-nghiem.jsp"
                    : "/WEB-INF/views/imaging/thuc-hien-chan-doan.jsp").forward(request, response);
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
            if ("save".equals(action)) resultDB.saveDraft(itemId, userId, request.getParameter("findings"), request.getParameter("conclusion"));
            else if ("release".equals(action))
            {
                resultDB.saveAndRelease(itemId, userId, request.getParameter("findings"), request.getParameter("conclusion"));
            }
            else throw new IllegalArgumentException("Thao tác không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + itemId);
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            request.setAttribute("formError", e.getMessage());
            response.setStatus(400);
            doGet(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
