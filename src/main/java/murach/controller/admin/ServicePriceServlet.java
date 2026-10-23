package murach.controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.ServicePriceDB;
import murach.business.ServicePrice;

public class ServicePriceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final ServicePriceDB servicePriceDB = new ServicePriceDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!checkAdmin(request, response))
            return;

        try
        {
            List<ServicePrice> servicePrices = servicePriceDB.getAllServicePrices();

            request.setAttribute("servicePrices", servicePrices);
            request.setAttribute("created", "1".equals(request.getParameter("created")));
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));

            request.getRequestDispatcher("/WEB-INF/views/admin/service-price.jsp")
                    .forward(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể tải bảng giá dịch vụ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");

        if (!checkAdmin(request, response))
            return;

        try
        {
            String action = request.getParameter("action");
            String amountText = request.getParameter("amount");

            if (amountText == null || amountText.trim().isEmpty())
                throw new IllegalArgumentException("Giá dịch vụ không được để trống");

            BigDecimal amount = new BigDecimal(amountText.trim());

            HttpSession session = request.getSession(false);
            Long updatedByUserId = ((Number) session.getAttribute("userId")).longValue();

            if ("create".equals(action))
            {
                String serviceCode = request.getParameter("serviceCode");
                String serviceName = request.getParameter("serviceName");
                servicePriceDB.insert(serviceCode, serviceName, amount, updatedByUserId);
                response.sendRedirect(request.getContextPath() + "/admin/price?created=1");
                return;
            }

            String serviceCode = request.getParameter("serviceCode");
            servicePriceDB.updatePrice(serviceCode, amount, updatedByUserId);
            response.sendRedirect(request.getContextPath() + "/admin/price?updated=1");
        }
        catch (NumberFormatException e)
        {
            request.setAttribute("error", "Giá dịch vụ không hợp lệ");
            doGet(request, response);
        }
        catch (IllegalArgumentException e)
        {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể cập nhật giá dịch vụ", e);
        }
    }

    //chặn đứng bằng servlet thay vì  chỉ ẩn hiển thị chức năng thông qua Role ở jsp
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("currentUser") == null)
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return false;
        }

        if (!"ADMIN".equals(session.getAttribute("role")))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ quản trị viên mới có quyền quản lý giá dịch vụ");
            return false;
        }

        if (!(session.getAttribute("userId") instanceof Number))
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Không tìm thấy thông tin user");
            return false;
        }

        return true;
    }
}
