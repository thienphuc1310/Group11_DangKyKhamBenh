package murach.controller.main;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import murach.DAO.DoctorDB;
import murach.DAO.SpecialtyDB;

public class DoctorListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final DoctorDB doctorDB = new DoctorDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    // Toan: Hiển thị và lọc danh sách bác sĩ trên trang công khai.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String keyword = request.getParameter("q");
        Long specialtyId = parseId(request.getParameter("specialtyId"));
        String gender = toGender(request.getParameter("gender"));
        try {
            request.setAttribute("doctors", doctorDB.search(keyword, specialtyId, gender));
            request.setAttribute("specialties", specialtyDB.getAllSpecialties());
            request.setAttribute("selectedSpecialtyId", specialtyId);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải danh sách bác sĩ", e);
        }
        request.getRequestDispatcher("/WEB-INF/views/main/bacsi.jsp").forward(request, response);
    }

    // Toan: Chuyển tham số chuyên khoa thành Long an toàn.
    private static Long parseId(String value)
    {
        try { return value == null || value.isEmpty() ? null : Long.valueOf(value); }
        catch (NumberFormatException e) { return null; }
    }

    // Toan: Chuyển mã giới tính trên URL sang dữ liệu trong database.
    private static String toGender(String code)
    {
        if ("nam".equals(code)) return "Nam";
        if ("nu".equals(code)) return "Nữ";
        return null;
    }
}
