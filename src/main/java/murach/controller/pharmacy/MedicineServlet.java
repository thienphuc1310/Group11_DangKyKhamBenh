package murach.controller.pharmacy;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.MedicineDB;
import murach.DAO.StockMovementDB;
import murach.business.Account;
import murach.business.Medicine;
import murach.controller.clinical.ClinicalWeb;

public class MedicineServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicineDB medicineDB = new MedicineDB();
    private final StockMovementDB stockDB = new StockMovementDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = getUserId(request, response);
            if (userId == null) return;
            medicineDB.requireInventoryAccess(userId);
            String keyword = request.getParameter("q");
            request.setAttribute("medicines", medicineDB.search(keyword));
            request.setAttribute("keyword", keyword);
            request.setAttribute("today", LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            String value = request.getParameter("medicineId");
            if (value != null && !value.trim().isEmpty())
            {
                Long medicineId = positiveId(request, "medicineId");
                Medicine selected = medicineDB.findById(medicineId);
                if (selected == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thuốc");
                    return;
                }
                request.setAttribute("selectedMedicine", selected);
                request.setAttribute("stockHistory", stockDB.findByMedicineId(medicineId));
            }

            HttpSession session = request.getSession(false);
            // Token riêng từng loại thao tác; mở lại trang làm các form cũ hết hiệu lực.
            synchronized (session)
            {
                for (String purpose : new String[]{"create", "receive", "update"})
                {
                    String token = UUID.randomUUID().toString();
                    session.setAttribute("medicineFormToken:" + purpose, token);
                    request.setAttribute(purpose + "Token", token);
                }
            }
            request.setAttribute("medicineError", session.getAttribute("medicineError"));
            session.removeAttribute("medicineError");
            request.getRequestDispatcher("/WEB-INF/views/pharmacy/nhap-thuoc.jsp").forward(request, response);
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
        try
        {
            Long userId = getUserId(request, response);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            String action = request.getParameter("action");
            if (!"create".equals(action) && !"update".equals(action))
                throw new IllegalArgumentException("Thao tác thuốc không hợp lệ");
            consumeToken(request, action);

            String name = require(request, "medicineName");
            String description = request.getParameter("description");
            BigDecimal price = price(request);
            if ("update".equals(action))
            {
                medicineDB.updateInformation(positiveId(request, "medicineId"), name, description, price, userId);
                response.sendRedirect(request.getContextPath() + "/pharmacy/medicines?updated=1");
            }
            else
            {
                medicineDB.createAndReceive(name, description, price, quantity(request), date(request), userId);
                response.sendRedirect(request.getContextPath() + "/pharmacy/medicines?created=1");
            }
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            showInputError(request, response, e);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

    // Dùng chung với MedicineStockServlet, kiểm tra Account ACTIVE ở backend.
    static Long getUserId(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        Long userId = ClinicalWeb.requireUser(request, response, Account.Role.PHARMACIST);
        if (userId != null) ClinicalWeb.profileId(request, "pharmacistId");
        return userId;
    }

    static String require(HttpServletRequest request, String name)
    {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Thiếu thông tin: " + name);
        return value.trim();
    }

    static Long positiveId(HttpServletRequest request, String name)
    {
        try
        {
            Long id = Long.valueOf(require(request, name));
            if (id <= 0) throw new NumberFormatException();
            return id;
        }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Mã thuốc không hợp lệ"); }
    }

    static int quantity(HttpServletRequest request)
    {
        try { return Integer.parseInt(require(request, "quantity")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Số viên nhập phải là số nguyên hợp lệ"); }
    }

    static BigDecimal price(HttpServletRequest request)
    {
        try { return new BigDecimal(require(request, "price")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Giá bán/viên phải là số hợp lệ"); }
    }

    static LocalDate date(HttpServletRequest request)
    {
        try { return LocalDate.parse(require(request, "receivedDate")); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Ngày nhập không hợp lệ"); }
    }

    static void consumeToken(HttpServletRequest request, String purpose)
    {
        if (!"create".equals(purpose) && !"receive".equals(purpose) && !"update".equals(purpose))
            throw new IllegalArgumentException("Thao tác thuốc không hợp lệ");
        HttpSession session = request.getSession(false);
        String supplied = require(request, "formToken");
        synchronized (session)
        {
            String key = "medicineFormToken:" + purpose;
            if (!supplied.equals(session.getAttribute(key)))
                throw new IllegalStateException("Form đã xử lý hoặc hết hiệu lực, vui lòng tải lại trang");
            session.removeAttribute(key);
        }
    }

    static void showInputError(HttpServletRequest request, HttpServletResponse response, RuntimeException e) throws IOException
    {
        request.getSession(false).setAttribute("medicineError",
                e.getMessage() == null ? "Dữ liệu nhập không hợp lệ" : e.getMessage());
        response.sendRedirect(request.getContextPath() + "/pharmacy/medicines");
    }
}
