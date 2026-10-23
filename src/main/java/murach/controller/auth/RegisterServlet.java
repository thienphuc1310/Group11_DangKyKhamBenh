package murach.controller.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import murach.DAO.auth.RegisterDB;

public class RegisterServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    //gọi hàm đối tượng
    private final RegisterDB registerDB = new RegisterDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String fullName = request.getParameter("fullname");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmmpass = request.getParameter("confirm-password");

        try
        {
            //kiểm tra dữ liệu hợp lệ hay không
            validate(fullName, email, phone, password, confirmmpass);
            //chuyển dữ liệu qua ResgisterDB
            registerDB.registerPatient(fullName, email, phone, password);

            //Redirect để refresh trang thành công không gửi lại form đăng ký.
            response.sendRedirect(request.getContextPath() + "/register-success");
        }
        catch (IllegalArgumentException e)
        {
            request.setAttribute("registerError", e.getMessage());
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp")
                    .forward(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể đăng ký tài khoản", e);
        }
    }

    private void validate(String fullName, String email, String phone,
                          String password, String confirmation)
    {
        if (isBlank(fullName) || isBlank(email) || isBlank(phone))
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự");
        if (!password.equals(confirmation))
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
