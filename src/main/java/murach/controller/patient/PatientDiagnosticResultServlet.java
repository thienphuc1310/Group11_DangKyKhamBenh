package murach.controller.patient;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.*;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class PatientDiagnosticResultServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return;
            Long patientId = ClinicalWeb.profileId(request, "patientId");
            request.setAttribute("results", new DiagnosticResultDB().findReleasedByPatientId(patientId));
            request.setAttribute("resultFiles", new ResultFileDB().findReleasedByPatientId(patientId));
            request.getRequestDispatcher("/WEB-INF/views/patient/ket-qua-can-lam-sang.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
