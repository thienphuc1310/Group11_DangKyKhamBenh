package murach.controller.pharmacy;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DispensationDB;
import murach.business.*;
import murach.controller.clinical.ClinicalWeb;

public class PharmacyPrescriptionServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DispensationDB dispensationDB = new DispensationDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PHARMACIST) == null) return;
            ClinicalWeb.profileId(request, "pharmacistId");
            String value = request.getParameter("prescriptionId");
            if (value != null && !value.trim().isEmpty())
            {
                Prescription prescription = dispensationDB.findPaidPrescriptionDetail(ClinicalWeb.id(request, "prescriptionId"));
                if (prescription == null) { response.sendError(404, "Đơn chưa đủ điều kiện hoặc đã cấp thuốc"); return; }
                request.setAttribute("prescription", prescription);
                request.getRequestDispatcher("/WEB-INF/views/pharmacy/chi-tiet-don-thuoc.jsp").forward(request, response);
            }
            else
            {
                request.setAttribute("prescriptions", dispensationDB.findPaidPrescriptionsWaitingDispense());
                request.getRequestDispatcher("/WEB-INF/views/pharmacy/danh-sach-don-thuoc.jsp").forward(request, response);
            }
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
