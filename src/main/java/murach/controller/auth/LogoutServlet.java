package murach.controller.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute("role");

        if (session != null) session.invalidate();

        if ("PATIENT".equals(role))
            response.sendRedirect(request.getContextPath() + "/main");
        else
            response.sendRedirect(request.getContextPath() + "/staff-login");
    }
}
