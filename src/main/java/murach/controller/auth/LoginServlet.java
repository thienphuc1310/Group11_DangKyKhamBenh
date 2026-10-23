package murach.controller.auth;

import murach.DAO.auth.LoginDB;
import murach.business.Account;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final LoginDB loginDB = new LoginDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp")
                .forward(request, response);
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
            Account account = loginDB.authenticate(username, password);

            if (account == null)
            {
                showLoginError(request, response,
                        "Tên đăng nhập hoặc mật khẩu không chính xác", username);
                return;
            }

            Long userId = account.getUser().getUserId();
            Long patientId = loginDB.findPatientId(userId);

            if (patientId == null)
            {
                showLoginError(request, response,
                        "Tài khoản chưa có hồ sơ phù hợp với vai trò", username);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null)
            {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("accountId", account.getAccountId());
            session.setAttribute("currentUser", account.getUser());
            session.setAttribute("userId", userId);
            session.setAttribute("fullName", account.getUser().getFullName());
            session.setAttribute("role", account.getRole().name());

            session.setAttribute("patientId", patientId);
            response.sendRedirect(request.getContextPath() + "/main?loginSuccess=1");
        }
        catch (IllegalStateException e)
        {
            showLoginError(request, response, e.getMessage(), username);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể đăng nhập", e);
        }
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response,
                                String message, String username)
            throws ServletException, IOException
    {
        request.setAttribute("loginError", message);
        request.setAttribute("username", username == null ? "" : username.trim());
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp")
                .forward(request, response);
    }
}
