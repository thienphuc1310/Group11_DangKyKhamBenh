package murach.controller.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.auth.StaffLoginDB;
import murach.business.Account;
import murach.business.Staff;

/** Đăng nhập nội bộ dành cho bác sĩ, nhân viên và quản trị viên. */
public class StaffLoginServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final StaffLoginDB staffLoginDB = new StaffLoginDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/views/auth/loginstaff.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try
        {
            Account account = staffLoginDB.authenticate(username, password);
            if (account == null)
            {
                showError(request, response,
                        "Tên đăng nhập hoặc mật khẩu không chính xác", username);
                return;
            }

            Long userId = account.getUser().getUserId();
            Long profileId;
            Staff operationalStaff = null;
            String doctorSpecialtyType = null;

            if (account.getRole() == Account.Role.DOCTOR)
            {
                profileId = staffLoginDB.findDoctorId(userId);
                if (profileId != null) doctorSpecialtyType = staffLoginDB.findDoctorSpecialtyType(userId);
            }
            else if (account.getRole() == Account.Role.TECHNICIAN)
                profileId = staffLoginDB.findTechnicianId(userId);
            else if (account.getRole() == Account.Role.PHARMACIST)
                profileId = staffLoginDB.findPharmacistId(userId);
            else if (account.getRole() == Account.Role.NURSE)
                profileId = staffLoginDB.findNurseId(userId);
            else if (account.getRole() == Account.Role.STAFF)
            {
                operationalStaff = staffLoginDB.findOperationalStaff(userId);
                if (operationalStaff != null && operationalStaff.getJobType() != Staff.JobType.RECEPTIONIST)
                {
                    showError(request, response, "Chức năng thu ngân chưa được triển khai", username);
                    return;
                }
                profileId = operationalStaff == null ? staffLoginDB.findAdminId(userId) : operationalStaff.getStaffId();
            }
            else if (account.getRole() == Account.Role.ADMIN)
                profileId = staffLoginDB.findAdminId(userId);
            else
            {
                showError(request, response,
                        "Trang này chỉ dành cho tài khoản nội bộ", username);
                return;
            }

            if (profileId == null)
            {
                showError(request, response,
                        "Tài khoản chưa có hồ sơ phù hợp với vai trò", username);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("accountId", account.getAccountId());
            session.setAttribute("currentUser", account.getUser());
            session.setAttribute("userId", userId);
            session.setAttribute("fullName", account.getUser().getFullName());
            session.setAttribute("role", account.getRole().name());

            if (account.getRole() == Account.Role.DOCTOR)
            {
                session.setAttribute("doctorId", profileId);
                session.setAttribute("doctorSpecialtyType", doctorSpecialtyType);
                response.sendRedirect(request.getContextPath() + "/doctor/schedule");
            }
            else if (account.getRole() == Account.Role.TECHNICIAN)
            {
                session.setAttribute("technicianId", profileId);
                response.sendRedirect(request.getContextPath() + "/diagnostics/worklist");
            }
            else if (account.getRole() == Account.Role.PHARMACIST)
            {
                session.setAttribute("pharmacistId", profileId);
                response.sendRedirect(request.getContextPath() + "/pharmacy/medicines");
            }
            else if (account.getRole() == Account.Role.NURSE)
            {
                session.setAttribute("nurseId", profileId);
                response.sendRedirect(request.getContextPath() + "/nurse/assessment");
            }
            else if (operationalStaff != null)
            {
                session.setAttribute("staffId", profileId);
                session.setAttribute("staffJobType", operationalStaff.getJobType().name());
                response.sendRedirect(request.getContextPath() + "/staff/reception");
            }
            else
            {
                session.setAttribute("adminId", profileId);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            }
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể đăng nhập nội bộ", e);
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           String message, String username)
            throws ServletException, IOException
    {
        request.setAttribute("loginError", message);
        request.setAttribute("username", username == null ? "" : username.trim());
        request.getRequestDispatcher("/WEB-INF/views/auth/loginstaff.jsp")
                .forward(request, response);
    }
}
