package murach.controller.main;

import java.io.Serializable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.DAO.SpecialtyDB;
import murach.DAO.ServicePriceDB;
import murach.business.Specialty;
import murach.business.ServicePrice;
import murach.util.JPAUtil;

//dùng để đổ thông tin chuyên khoa, bác sĩ cho phần đăng ký khám ở main
public class MainServlet extends HttpServlet
{
    private static final long serialVersionID = 1L;

    private final SpecialtyDB specialtyDB = new SpecialtyDB();
    //thêm giá khám theo mã
    private final ServicePriceDB servicePriceDB = new ServicePriceDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try
        {
            List<Specialty> specialties = specialtyDB.findActiveClinicalSpecialties();

            //lấy giá
            ServicePrice bookingprice = servicePriceDB.findActiveByCode("DANG_KY_KHAM_BENH");

            request.setAttribute("specialties", specialties);

            request.setAttribute("bookingprice", bookingprice);

            if (request.getSession(false) != null)
            {
                Object bookingError = request.getSession(false).getAttribute("bookingError");
                if (bookingError != null)
                {
                    request.setAttribute("bookingError", bookingError);
                    request.getSession(false).removeAttribute("bookingError");
                }
            }

            request.getRequestDispatcher("/WEB-INF/views/main/main.jsp").forward(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể tải danh sách chuyên khoa", e);
        }
    }
}
