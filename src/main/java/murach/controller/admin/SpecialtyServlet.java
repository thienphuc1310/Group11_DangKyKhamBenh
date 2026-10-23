package murach.controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.SpecialtyDB;
import murach.business.Specialty;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class SpecialtyServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    // Toan: Hiển thị và tìm kiếm chuyên khoa cho admin/staff.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!StaffServlet.isStaffOrAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        try {
            if (ClinicalWeb.requireUser(request, response, Account.Role.ADMIN, Account.Role.STAFF) == null) return;
            String keyword = request.getParameter("q");
            request.setAttribute("specialties", keyword == null
                    ? specialtyDB.getAllSpecialties() : specialtyDB.search(keyword));
            request.setAttribute("keyword", keyword == null ? "" : keyword);
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));
            request.setAttribute("created", "1".equals(request.getParameter("created")));
            request.getRequestDispatcher("/WEB-INF/views/admin/specialty-list.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            ClinicalWeb.fail(response, e);
        }
    }

    // Người 4: đổi loại chuyên khoa (CLINICAL / PARACLINICAL), chỉ ADMIN
    // Bổ sung tạo mới, loại khoa luôn được chọn rõ ràng từ form.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Number)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        if (!StaffServlet.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ quản trị viên mới được tạo hoặc phân loại chuyên khoa");
            return;
        }
        try {
            if (ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) == null) return;
            ClinicalWeb.requireCsrf(request);
            Specialty.SpecialtyType type = parseType(request.getParameter("specialtyType"));
            Long userId = ((Number) session.getAttribute("userId")).longValue();
            String action = request.getParameter("action");
            if ("create".equals(action))
            {
                specialtyDB.create(request.getParameter("name"), request.getParameter("description"), type, userId);
                response.sendRedirect(request.getContextPath() + "/admin/specialties?created=1");
            }
            else if (action == null || "updateType".equals(action))
            {
                Long specialtyId = parseId(request.getParameter("specialtyId"));
                specialtyDB.updateType(specialtyId, type, userId);
                response.sendRedirect(request.getContextPath() + "/admin/specialties?updated=1");
            }
            else
                throw new IllegalArgumentException("Thao tác chuyên khoa không hợp lệ");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (RuntimeException e) {
            ClinicalWeb.fail(response, e);
        }
    }

    private Long parseId(String value)
    {
        try {
            Long id = Long.valueOf(value == null ? "" : value.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã chuyên khoa không hợp lệ");
        }
    }

    // Không mặc định CLINICAL: giá trị lạ phải báo lỗi
    private Specialty.SpecialtyType parseType(String value)
    {
        try {
            return Specialty.SpecialtyType.valueOf(value == null ? "" : value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại chuyên khoa không hợp lệ");
        }
    }
}
