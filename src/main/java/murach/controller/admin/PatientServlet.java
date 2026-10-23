package murach.controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.PatientDB;
import murach.DAO.UserDB;
import murach.business.Account;
import murach.business.Patient;

public class PatientServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final PatientDB patientDB = new PatientDB();
    private final UserDB userDB = new UserDB();

    // Toan: Tìm kiếm, lọc hoặc xem chi tiết bệnh nhân.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!StaffServlet.isStaffOrAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        try {
            Long id = StaffServlet.parseLong(request.getParameter("id"));
            if (id != null) {
                Patient patient = patientDB.findById(id);
                if (patient == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bệnh nhân");
                    return;
                }
                request.setAttribute("patient", patient);
                request.setAttribute("account", userDB.findAccountByUserId(patient.getUserId()));
            } else {
                String keyword = request.getParameter("q");
                String gender = toGender(request.getParameter("gender"));
                boolean searched = keyword != null;
                if (searched) {
                    request.setAttribute("patients", patientDB.search(keyword, null, gender));
                    request.setAttribute("statusMap", userDB.getStatusMap(Account.Role.PATIENT));
                }
                request.setAttribute("searched", searched);
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/patient-list.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải dữ liệu bệnh nhân", e);
        }
    }

    // Toan: Khóa hoặc mở khóa tài khoản bệnh nhân, chỉ dành cho admin.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        StaffServlet.handleLockToggle(request, response, "/admin/users");
    }

    // Toan: Chuyển giá trị bộ lọc giới tính sang dữ liệu lưu trong database.
    private static String toGender(String code)
    {
        if ("nam".equals(code)) return "Nam";
        if ("nu".equals(code)) return "Nữ";
        return null;
    }
}
