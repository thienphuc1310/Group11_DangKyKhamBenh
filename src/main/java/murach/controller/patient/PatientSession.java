package murach.controller.patient;

import murach.DAO.PatientDB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class PatientSession
{
    private PatientSession() {}

    public static Long getPatientId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("patientId");
        if (value == null)
        {
            return null;
        }
        try
        {
            long id = Long.parseLong(value.toString().trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static Long getPreviewPatientId(HttpServletRequest request)
    {
        Long patientId = getPatientId(request);
        return patientId == null ? new PatientDB().findFirstPatientId() : patientId;
    }
}
