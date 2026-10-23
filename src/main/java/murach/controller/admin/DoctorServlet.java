package murach.controller.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.DoctorDB;
import murach.DAO.SpecialtyDB;
import murach.DAO.UserDB;
import murach.business.Account;
import murach.business.Doctor;

public class DoctorServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DoctorDB doctorDB = new DoctorDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();
    private final UserDB userDB = new UserDB();

    // Toan: Tìm kiếm, lọc hoặc xem chi tiết bác sĩ.
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
                Doctor doctor = doctorDB.findById(id);
                if (doctor == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bác sĩ");
                    return;
                }
                request.setAttribute("doctor", doctor);
                if (doctor.getUser().getDateOfBirth() != null)
                    request.setAttribute("age", Period.between(
                            doctor.getUser().getDateOfBirth(), LocalDate.now()).getYears());
                request.setAttribute("account", userDB.findAccountByUserId(doctor.getUserId()));
            } else {
                String keyword = request.getParameter("q");
                Long specialtyId = StaffServlet.parseLong(request.getParameter("specialtyId"));
                String gender = toGender(request.getParameter("gender"));
                boolean searched = keyword != null;
                if (searched) {
                    request.setAttribute("doctors", doctorDB.search(keyword, specialtyId, gender));
                    request.setAttribute("statusMap", userDB.getStatusMap(Account.Role.DOCTOR));
                }
                request.setAttribute("searched", searched);
                request.setAttribute("specialties", specialtyDB.getAllSpecialties());
                request.setAttribute("selectedSpecialtyId", specialtyId);
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/doctor-list.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải dữ liệu bác sĩ", e);
        }
    }

    // Toan: Khóa hoặc mở khóa tài khoản bác sĩ, chỉ dành cho admin.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        StaffServlet.handleLockToggle(request, response, "/admin/doctors");
    }

    // Toan: Chuyển giá trị bộ lọc giới tính sang dữ liệu lưu trong database.
    private static String toGender(String code)
    {
        if ("nam".equals(code)) return "Nam";
        if ("nu".equals(code)) return "Nữ";
        return null;
    }
}
