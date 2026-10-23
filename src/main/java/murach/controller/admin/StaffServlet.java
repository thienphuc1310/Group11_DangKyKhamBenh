package murach.controller.admin;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.UserDB;
import murach.DAO.DoctorDB;
import murach.DAO.StaffDirectoryDB;
import murach.business.Account;
import murach.business.Staff;
import murach.controller.clinical.ClinicalWeb;

public class StaffServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final UserDB userDB = new UserDB();

    // Toan: Tìm kiếm, lọc và xem chi tiết tài khoản nhân viên/quản trị viên.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        try {
            if (ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) == null) return;
            Map<Long, Staff> profiles = new StaffDirectoryDB().getStaffProfiles();
            request.setAttribute("staffProfiles", profiles);
            Long id = request.getParameter("id") == null ? null : ClinicalWeb.id(request, "id");
            if (id != null) {
                Account staff = userDB.findAccountByUserId(id);
                if (staff == null || (staff.getRole() != Account.Role.STAFF
                        && staff.getRole() != Account.Role.ADMIN)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy nhân viên");
                    return;
                }
                request.setAttribute("staff", staff);
            } else {
                String keyword = request.getParameter("q");
                String role = request.getParameter("role");
                request.setAttribute("staffList", userDB.searchStaff(null, parseRole(role),
                        parseActive(request.getParameter("status"))).stream()
                        .filter(a -> {
                            Staff profile = profiles.get(a.getUser().getUserId());
                            if ("receptionist".equals(role) && (profile == null || profile.getJobType() != Staff.JobType.RECEPTIONIST)) return false;
                            return DoctorDB.matches(keyword, a.getAccountId(), a.getUsername(), a.getUser().getFullName(),
                                    profile == null ? null : profile.getStaffCode());
                        }).collect(Collectors.toList()));
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/staff-list.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    // Toan: Khóa hoặc mở khóa tài khoản nhân viên.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        handleLockToggle(request, response, "/admin/admins");
    }

    // Toan: Xử lý khóa/mở khóa dùng chung cho bác sĩ, bệnh nhân và nhân viên.
    static void handleLockToggle(HttpServletRequest request, HttpServletResponse response,
                                 String redirectPath) throws IOException
    {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ quản trị viên được khóa hoặc mở khóa tài khoản.");
            return;
        }
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            Object sessionUserId = request.getSession(false).getAttribute("userId");
            if (!(sessionUserId instanceof Number)
                    || userId.longValue() != ((Number) sessionUserId).longValue())
                new UserDB().toggleLock(userId);
        } catch (IllegalArgumentException ignored) { }
        String back = request.getParameter("back");
        response.sendRedirect(request.getContextPath() + redirectPath
                + (back == null || !back.startsWith("?") ? "" : back));
    }

    // Toan: Kiểm tra tài khoản thuộc nhóm nhân viên hoặc quản trị viên.
    static boolean isStaffOrAdmin(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute("role");
        return "ADMIN".equals(role) || "STAFF".equals(role);
    }

    // Toan: Kiểm tra riêng quyền quản trị viên.
    static boolean isAdmin(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    // Toan: Chuyển tham số ID sang Long an toàn.
    static Long parseLong(String value)
    {
        try { return value == null || value.trim().isEmpty() ? null : Long.valueOf(value); }
        catch (NumberFormatException e) { return null; }
    }

    // Toan: Chuyển mã lọc vai trò thành enum.
    private static Account.Role parseRole(String value)
    {
        if ("staff".equals(value) || "receptionist".equals(value)) return Account.Role.STAFF;
        if ("admin".equals(value)) return Account.Role.ADMIN;
        if (value == null || value.trim().isEmpty()) return null;
        throw new IllegalArgumentException("Vai trò không hợp lệ");
    }

    // Toan: Chuyển mã lọc trạng thái thành Boolean.
    private static Boolean parseActive(String value)
    {
        if ("active".equals(value)) return Boolean.TRUE;
        if ("inactive".equals(value)) return Boolean.FALSE;
        if (value == null || value.trim().isEmpty()) return null;
        throw new IllegalArgumentException("Trạng thái không hợp lệ");
    }
}
