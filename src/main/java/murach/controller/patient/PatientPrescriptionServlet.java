package murach.controller.patient;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.PrescriptionDB;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class PatientPrescriptionServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return;
            request.setAttribute("prescriptions", new PrescriptionDB().findConfirmedByPatientId(ClinicalWeb.profileId(request, "patientId")));
            request.getRequestDispatcher("/WEB-INF/views/patient/don-thuoc.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
