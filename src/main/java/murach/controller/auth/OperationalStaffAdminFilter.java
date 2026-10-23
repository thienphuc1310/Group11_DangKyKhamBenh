package murach.controller.auth;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

/** STAFF tiếp tân không kế thừa các trang quản lý dành cho STAFF có hồ sơ Admin. */
public class OperationalStaffAdminFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpSession session = ((HttpServletRequest)request).getSession(false);
        if (session != null && "STAFF".equals(session.getAttribute("role"))
                && (session.getAttribute("staffId") != null || session.getAttribute("staffJobType") != null))
        {
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tiếp tân không có quyền truy cập trang quản trị");
            return;
        }
        chain.doFilter(request, response);
    }
}
