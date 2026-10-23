package murach.controller.doctor;

import murach.DAO.DoctorScheduleDB;
import murach.DAO.DoctorDB;
import murach.business.Account;
import murach.business.Doctor;
import murach.business.Specialty;
import murach.controller.clinical.ClinicalWeb;
import murach.business.DoctorSchedule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class DoctorScheduleServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null || !"DOCTOR".equals(session.getAttribute("role"))
                || !(session.getAttribute("doctorId") instanceof Number))
        {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }

        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            Long doctorId = ((Number) session.getAttribute("doctorId")).longValue();
            Doctor doctor = new DoctorDB().findById(doctorId);
            if (doctor == null || !userId.equals(doctor.getUserId()))
                throw new SecurityException("Hồ sơ không thuộc bác sĩ đang đăng nhập");
            request.setAttribute("paraclinical", doctor.getSpecialty().getSpecialtyType() == Specialty.SpecialtyType.PARACLINICAL);
            boolean showSlotCapacity = doctor.getSpecialty().getSpecialtyType() == Specialty.SpecialtyType.CLINICAL;
            request.setAttribute("showSlotCapacity", showSlotCapacity);

            List<DoctorSchedule> schedules = DoctorScheduleDB.findByDoctor(doctorId);

            request.setAttribute("schedules", schedules);
            request.setAttribute("doctorId", doctorId);

            // Chỉ bác sĩ lâm sàng sử dụng giới hạn bệnh nhân theo khung giờ đặt khám.
            if (showSlotCapacity)
                request.setAttribute("slotCapacity", DoctorScheduleDB.findSharedSlotCapacity(doctorId));

            Object capacityError = session.getAttribute("capacityError");
            if (capacityError != null)
            {
                if (showSlotCapacity) request.setAttribute("capacityError", capacityError);
                session.removeAttribute("capacityError");
            }

            //Chuyển tiếp request sang JSP để hiển thị
            request.getRequestDispatcher("/WEB-INF/views/doctor/lich-lam-viec.jsp").forward(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }

}
