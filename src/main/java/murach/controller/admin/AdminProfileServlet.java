package murach.controller.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.AdminDB;
import murach.business.Admin;

public class AdminProfileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final AdminDB adminDB = new AdminDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Long adminId = getAdminId(request, response);
        if (adminId == null) return;

        try {
            Admin profile = adminDB.findById(adminId);
            if (profile == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy hồ sơ nhân sự quản lý");
                return;
            }

            request.setAttribute("profile", profile);
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));
            request.getRequestDispatcher("/WEB-INF/views/admin/admin-profile.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải thông tin cá nhân", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Long adminId = getAdminId(request, response);
        if (adminId == null) return;

        try {
            Admin profile = adminDB.updateProfile(
                    adminId,
                    request.getParameter("fullName"),
                    request.getParameter("phone"),
                    request.getParameter("email"),
                    parseDate(request.getParameter("birthDate")),
                    request.getParameter("gender"),
                    request.getParameter("address"));

            request.getSession().setAttribute("currentUser", profile.getUser());
            request.getSession().setAttribute("fullName", profile.getUser().getFullName());
            response.sendRedirect(request.getContextPath() + "/admin/profile?updated=1");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể cập nhật thông tin cá nhân", e);
        }
    }

    private Long getAdminId(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute("role");
        if (session == null || !("ADMIN".equals(role) || "STAFF".equals(role))
                || !(session.getAttribute("adminId") instanceof Number)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return null;
        }
        return ((Number) session.getAttribute("adminId")).longValue();
    }

    private static LocalDate parseDate(String value)
    {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim());
    }
}
