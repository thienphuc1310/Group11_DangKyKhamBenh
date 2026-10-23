package murach.controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import murach.DAO.DiagnosticServiceDB;
import murach.DAO.SpecialtyDB;
import murach.business.DiagnosticService;
import murach.business.Account;
import murach.controller.clinical.ClinicalWeb;

public class DiagnosticServiceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DiagnosticServiceDB diagnosticServiceDB = new DiagnosticServiceDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            if (!requireAdmin(request, response)) return;
            List<DiagnosticService> services = diagnosticServiceDB.getAll();

            request.setAttribute("services", services);
            request.setAttribute("specialties", specialtyDB.findActiveParaclinicalSpecialties());
            request.setAttribute("created", "1".equals(request.getParameter("created")));
            request.setAttribute("updated", "1".equals(request.getParameter("updated")));
            request.getRequestDispatcher("/WEB-INF/views/admin/diagnostic-service-list.jsp")
                    .forward(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        try
        {
            if (!requireAdmin(request, response)) return;
            ClinicalWeb.requireCsrf(request);
            Long userId = ((Number) request.getSession(false).getAttribute("userId")).longValue();

            if ("toggle".equals(request.getParameter("action")))
            {
                Long serviceId = parseId(request.getParameter("serviceId"), "Mã dịch vụ không hợp lệ", true);
                String statusText = request.getParameter("status");
                if (!"true".equals(statusText) && !"false".equals(statusText))
                    throw new IllegalArgumentException("Trạng thái dịch vụ không hợp lệ");
                boolean status = Boolean.parseBoolean(statusText);
                diagnosticServiceDB.changeStatus(serviceId, status, userId);
                response.sendRedirect(request.getContextPath() + "/admin/diagnostic-services?updated=1");
                return;
            }

            String action = request.getParameter("action");
            if (action != null && !"create".equals(action))
                throw new IllegalArgumentException("Thao tác không hợp lệ");
            DiagnosticService.ServiceType type = parseType(request.getParameter("serviceType"));
            Long specialtyId = parseId(request.getParameter("specialtyId"), "Phải chọn khoa thực hiện", true);
            BigDecimal amount = parseAmount(request.getParameter("amount"));
            boolean requiresSpecimen = "on".equals(request.getParameter("requiresSpecimen"));
            int minutes = parseMinutes(request.getParameter("estimatedDurationMinutes"));

            diagnosticServiceDB.insertWithPrice(request.getParameter("serviceCode"), request.getParameter("serviceName"),
                    type, specialtyId, amount, requiresSpecimen, minutes, userId);
            response.sendRedirect(request.getContextPath() + "/admin/diagnostic-services?created=1");
        }
        catch (IllegalArgumentException e)
        {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    private BigDecimal parseAmount(String value)
    {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Giá dịch vụ không được để trống");
        try
        {
            return new BigDecimal(value.trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Giá dịch vụ không hợp lệ");
        }
    }

    private Long parseId(String value, String message, boolean required)
    {
        if (value == null || value.trim().isEmpty())
        {
            if (required) throw new IllegalArgumentException(message);
            return null;
        }
        try
        {
            Long id = Long.valueOf(value.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(message);
        }
    }

    // Không tự đổi loại sai thành LABORATORY: giá trị lạ phải báo lỗi
    private DiagnosticService.ServiceType parseType(String value)
    {
        try
        {
            return DiagnosticService.ServiceType.valueOf(value == null ? "" : value.trim());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Loại dịch vụ không hợp lệ");
        }
    }

    private int parseMinutes(String value)
    {
        try
        {
            return Integer.parseInt(value == null ? "" : value.trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Thời gian dự kiến phải là số nguyên");
        }
    }

    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        return ClinicalWeb.requireUser(request, response, Account.Role.ADMIN) != null;
    }
}
