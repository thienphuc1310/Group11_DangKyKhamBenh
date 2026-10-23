package murach.controller.clinical;

import java.io.IOException;
import java.util.*;
import javax.persistence.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.ClinicalAccess;
import murach.business.Account;
import murach.util.JPAUtil;

/** Tiện ích nhỏ cho các Servlet khám bệnh, không chứa nghiệp vụ chuyển trạng thái. */
public final class ClinicalWeb
{
    private ClinicalWeb() {}

    public static Long requireUser(HttpServletRequest request, HttpServletResponse response, Account.Role... roles)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Number))
        {
            response.sendRedirect(request.getContextPath() +
                    (Arrays.asList(roles).contains(Account.Role.PATIENT) ? "/login" : "/staff-login"));
            return null;
        }
        Account.Role role = role(request);
        if (!Arrays.asList(roles).contains(role)) throw new SecurityException("Bạn không có quyền truy cập");
        Long userId = ((Number) session.getAttribute("userId")).longValue();
        EntityManager em = JPAUtil.getEntityManager();
        try { ClinicalAccess.requireUser(em, userId, role); }
        finally { em.close(); }
        synchronized (session)
        {
            if (session.getAttribute("clinicalCsrf") == null)
                session.setAttribute("clinicalCsrf", UUID.randomUUID().toString());
        }
        response.setHeader("Cache-Control", "no-store");
        return userId;
    }

    public static Account.Role role(HttpServletRequest request)
    {
        try { return Account.Role.valueOf(String.valueOf(request.getSession(false).getAttribute("role"))); }
        catch (RuntimeException e) { throw new SecurityException("Phiên đăng nhập không hợp lệ"); }
    }

    public static Long profileId(HttpServletRequest request, String name)
    {
        Object value = request.getSession(false).getAttribute(name);
        if (!(value instanceof Number) || ((Number) value).longValue() <= 0)
            throw new SecurityException("Phiên đăng nhập thiếu hồ sơ " + name);
        return ((Number) value).longValue();
    }

    public static Long id(HttpServletRequest request, String name)
    {
        try
        {
            Long value = Long.valueOf(request.getParameter(name));
            if (value <= 0) throw new NumberFormatException();
            return value;
        }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Mã " + name + " không hợp lệ"); }
    }

    public static void requireCsrf(HttpServletRequest request)
    {
        Object token = request.getSession(false).getAttribute("clinicalCsrf");
        if (token == null || !token.equals(request.getParameter("csrf")))
            throw new SecurityException("Phiên gửi biểu mẫu không hợp lệ, hãy tải lại trang");
    }

    public static void fail(HttpServletResponse response, RuntimeException e) throws IOException, ServletException
    {
        if (e instanceof SecurityException) response.sendError(403, e.getMessage());
        else if (e instanceof IllegalArgumentException) response.sendError(400, e.getMessage());
        else if (e instanceof IllegalStateException || e instanceof OptimisticLockException ||
                e instanceof PessimisticLockException || e instanceof LockTimeoutException)
            response.sendError(409, "Không thể thực hiện: " + (e.getMessage() == null ? "Dữ liệu đã thay đổi, hãy tải lại trang" : e.getMessage()));
        else throw new ServletException("Không thể xử lý nghiệp vụ khám bệnh", e);
    }
}
