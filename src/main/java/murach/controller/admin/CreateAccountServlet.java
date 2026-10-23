package murach.controller.admin;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.SpecialtyDB;
import murach.DAO.auth.RegisterStaffDB;
import murach.business.Account;
import murach.business.Nurse;
import murach.controller.clinical.ClinicalWeb;

/** ADMIN cấp tài khoản nội bộ và hồ sơ phù hợp với vai trò. */
public class CreateAccountServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final RegisterStaffDB registerDB = new RegisterStaffDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            if (!isAdmin(request, response)) return;
            String supplied = request.getParameter("type");
            // Giữ đường dẫn cũ không có type ở trang nhân viên; POST vẫn bắt buộc loại rõ ràng.
            String type = normalizeType(supplied == null || supplied.trim().isEmpty() ? "STAFF" : supplied);
            request.setAttribute("type", type);
            HttpSession session = request.getSession(false);
            Object message = session.getAttribute("createAccountMessage");
            if (message != null)
            {
                request.setAttribute("message", message);
                request.setAttribute("success", session.getAttribute("createAccountSuccess"));
                session.removeAttribute("createAccountMessage");
                session.removeAttribute("createAccountSuccess");
            }

            if ("TECHNICIAN".equals(type))
            {
                request.setAttribute("specialties", specialtyDB.findActiveParaclinicalSpecialties());
            }
            else if ("DOCTOR".equals(type))
                request.setAttribute("specialties", specialtyDB.getAllSpecialties());

            request.getRequestDispatcher("/WEB-INF/views/admin/create-account.jsp").forward(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String type = null;
        try
        {
            if (!isAdmin(request, response)) return;
            ClinicalWeb.requireCsrf(request);
            type = normalizeType(request.getParameter("type"));
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");

            if ("ADMIN".equals(type))
                registerDB.createAdmin(username, password, fullName, email, phone);
            else if ("STAFF".equals(type))
                registerDB.createStaff(username, password, fullName, email, phone);
            else if ("RECEPTIONIST".equals(type))
                registerDB.createReceptionist(username, password, fullName, email, phone);
            else if ("DOCTOR".equals(type))
                registerDB.createDoctor(username, password, fullName, email, phone,
                        parseRequiredLong(request.getParameter("specialtyId"), "Vui lòng chọn chuyên khoa"));
            else if ("NURSE".equals(type))
                registerDB.createNurse(username, password, fullName, email, phone,
                        parseProfession(request.getParameter("professionType")));
            else if ("TECHNICIAN".equals(type))
                registerDB.createTechnician(username, password, fullName, email, phone,
                        parseRequiredLong(request.getParameter("specialtyId"), "Vui lòng chọn khoa cận lâm sàng"));
            else if ("PHARMACIST".equals(type))
                registerDB.createPharmacist(username, password, fullName, email, phone);

            HttpSession session = request.getSession(false);
            session.setAttribute("createAccountMessage", "Tạo tài khoản " + roleLabel(type) + " thành công");
            session.setAttribute("createAccountSuccess", Boolean.TRUE);
        }
        catch (IllegalArgumentException e)
        {
            if (type == null)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            request.getSession(false).setAttribute("createAccountMessage", e.getMessage());
            request.getSession(false).setAttribute("createAccountSuccess", Boolean.FALSE);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/create-account?type=" + type);
    }

    private static Long parseRequiredLong(String value, String message)
    {
        try
        {
            Long id = Long.valueOf(value == null ? "" : value.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        }
        catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }

    private static String normalizeType(String value)
    {
        String type = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(type) || "STAFF".equals(type) || "DOCTOR".equals(type)
                || "NURSE".equals(type) || "TECHNICIAN".equals(type) || "RECEPTIONIST".equals(type)
                || "PHARMACIST".equals(type)) return type;
        throw new IllegalArgumentException("Loại tài khoản không hợp lệ");
    }

    private static Nurse.ProfessionType parseProfession(String value)
    {
        try { return Nurse.ProfessionType.valueOf(value == null ? "" : value.trim()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Nhóm nghề điều dưỡng không hợp lệ"); }
    }

    private static String roleLabel(String type)
    {
        if ("ADMIN".equals(type)) return "Quản trị viên";
        if ("DOCTOR".equals(type)) return "Bác sĩ";
        if ("NURSE".equals(type)) return "Điều dưỡng";
        if ("TECHNICIAN".equals(type)) return "Chuyên viên kỹ thuật";
        if ("RECEPTIONIST".equals(type)) return "Tiếp tân";
        if ("PHARMACIST".equals(type)) return "Dược sĩ";
        return "Nhân viên";
    }

    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        return ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) != null;
    }
}
