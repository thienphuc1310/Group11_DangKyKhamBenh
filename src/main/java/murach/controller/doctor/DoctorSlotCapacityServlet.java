package murach.controller.doctor;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import murach.DAO.DoctorDB;
import murach.DAO.DoctorScheduleDB;
import murach.business.Account;
import murach.business.Doctor;
import murach.business.Specialty;
import murach.controller.clinical.ClinicalWeb;

public class DoctorSlotCapacityServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !"DOCTOR".equals(session.getAttribute("role"))
                || !(session.getAttribute("doctorId") instanceof Number))
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            Long doctorId = ((Number) session.getAttribute("doctorId")).longValue();
            Doctor doctor = new DoctorDB().findById(doctorId);
            if (doctor == null || !userId.equals(doctor.getUserId()))
                throw new SecurityException("Hồ sơ không thuộc bác sĩ đang đăng nhập");
            // Không chỉ ẩn biểu mẫu: bác sĩ cận lâm sàng cũng không được gửi cập nhật trực tiếp.
            if (doctor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.CLINICAL)
                throw new SecurityException("Giới hạn khung giờ chỉ áp dụng cho bác sĩ lâm sàng");
            int maxPatients = Integer.parseInt(request.getParameter("maxPatients"));
            DoctorScheduleDB.updateSharedSlotCapacity(doctorId, maxPatients);
            response.sendRedirect(request.getContextPath() + "/doctor/schedule?capacityUpdated=1");
        }
        catch (IllegalArgumentException e)
        {
            session.setAttribute("capacityError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/doctor/schedule?capacityError=1");
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }
}
