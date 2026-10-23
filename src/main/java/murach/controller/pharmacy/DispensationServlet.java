package murach.controller.pharmacy;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DispensationDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class DispensationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PHARMACIST) == null) return;
            ClinicalWeb.requireCsrf(request);
            new DispensationDB().dispense(ClinicalWeb.id(request, "prescriptionId"), ClinicalWeb.profileId(request, "pharmacistId"));
            response.sendRedirect(request.getContextPath() + "/pharmacy/prescriptions?dispensed=1");
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
