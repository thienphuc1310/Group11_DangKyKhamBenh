package murach.controller.main;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import murach.DAO.SpecialtyDB;
import murach.business.Specialty;

public class SpecialtyListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try {
            List<Specialty> specialties = specialtyDB.getAllSpecialties();
            request.setAttribute("specialties", specialties);
            request.getRequestDispatcher("/WEB-INF/views/main/chuyenkhoa.jsp").forward(request, response);
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể tải danh sách chuyên khoa", e);
        }
    }
}
