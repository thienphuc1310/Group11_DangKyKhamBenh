package murach.controller.pharmacy;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.MedicineDB;
import murach.controller.clinical.ClinicalWeb;

public class MedicineStockServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicineDB medicineDB = new MedicineDB();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            Long userId = MedicineServlet.getUserId(request, response);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            MedicineServlet.consumeToken(request, "receive");
            medicineDB.receiveAdditional(MedicineServlet.positiveId(request, "medicineId"),
                    MedicineServlet.quantity(request), MedicineServlet.date(request), userId);
            response.sendRedirect(request.getContextPath() + "/pharmacy/medicines?received=1");
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            MedicineServlet.showInputError(request, response, e);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }
}
