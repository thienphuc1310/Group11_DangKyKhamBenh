package murach.controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.*;
import murach.business.*;
import murach.controller.clinical.ClinicalWeb;

public class AdminMedicalStaffServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final StaffDirectoryDB directoryDB = new StaffDirectoryDB();
    private final UserDB userDB = new UserDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) == null) return;
            String path = request.getServletPath();
            boolean nurse = "/admin/nurses".equals(path);
            if (!nurse && !"/admin/technicians".equals(path)) throw new IllegalArgumentException("Danh sách không hợp lệ");
            Account.Role role = nurse ? Account.Role.NURSE : Account.Role.TECHNICIAN;
            request.setAttribute("directoryRole", role.name());
            request.setAttribute("directoryPath", path);
            request.setAttribute("directoryTitle", nurse ? "DANH SÁCH ĐIỀU DƯỠNG" : "DANH SÁCH CHUYÊN VIÊN");
            if (request.getParameter("id") != null)
            {
                Long id = ClinicalWeb.id(request, "id");
                Long userId;
                if (nurse)
                {
                    Nurse person = directoryDB.findNurseById(id);
                    if (person == null) { response.sendError(404); return; }
                    request.setAttribute("person", person); userId = person.getUserId();
                }
                else
                {
                    Technician person = directoryDB.findTechnicianById(id);
                    if (person == null) { response.sendError(404); return; }
                    request.setAttribute("person", person); userId = person.getUserId();
                }
                Account account = userDB.findAccountByUserId(userId);
                if (account != null && account.getRole() == role) request.setAttribute("account", account);
            }
            else
            {
                String keyword = request.getParameter("q");
                if (nurse)
                {
                    String value = request.getParameter("profession");
                    Nurse.ProfessionType profession = value == null || value.trim().isEmpty() ? null : Nurse.ProfessionType.valueOf(value);
                    request.setAttribute("people", directoryDB.searchNurses(keyword, profession));
                }
                else
                {
                    String value = request.getParameter("specialtyId");
                    Long specialtyId = value == null || value.trim().isEmpty() ? null : ClinicalWeb.id(request, "specialtyId");
                    request.setAttribute("people", directoryDB.searchTechnicians(keyword, specialtyId));
                    request.setAttribute("specialties", new SpecialtyDB().getAllSpecialties());
                    request.setAttribute("selectedSpecialtyId", specialtyId);
                }
                request.setAttribute("statusMap", userDB.getStatusMap(role));
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/medical-staff-list.jsp").forward(request, response);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }
}
