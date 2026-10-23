package murach.controller.doctor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import murach.DAO.MedicalRecordDB;
import murach.DAO.DoctorDB;
import murach.DAO.DiagnosticOrderItemDB;
import murach.business.Account;
import murach.business.Doctor;
import murach.business.DiagnosticOrderItem;
import murach.business.Specialty;
import murach.controller.clinical.ClinicalWeb;
import murach.business.QueueTicket;

public class DoctorAppointmentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final MedicalRecordDB medicalRecordDB = new MedicalRecordDB();
    private final DiagnosticOrderItemDB itemDB = new DiagnosticOrderItemDB();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
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
            Doctor doctor = requireOwnDoctor(doctorId, userId);
            boolean paraclinical = doctor.getSpecialty().getSpecialtyType() == Specialty.SpecialtyType.PARACLINICAL;
            request.setAttribute("paraclinical", paraclinical);
            String value = request.getParameter("workDate");
            LocalDate selectedDate = value == null || value.trim().isEmpty()
                    ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                    : LocalDate.parse(value.trim());

            if (paraclinical)
            {
                // Cận lâm sàng chưa có lịch hẹn riêng theo giờ: lấy phiếu đã trả phí,
                // kể cả phiếu từ ngày trước còn chờ. DAO kiểm tra lịch làm hôm nay.
                request.setAttribute("items", itemDB.findWorklist(userId, Account.Role.DOCTOR).stream()
                        .filter(item -> item.getStatus() != DiagnosticOrderItem.Status.COMPLETED)
                        .collect(Collectors.toList()));
            }
            else
            {
                List<QueueTicket> tickets = medicalRecordDB.findDoctorQueue(doctorId, selectedDate);
                request.setAttribute("tickets", tickets);
            }
            request.setAttribute("selectedDate", selectedDate);
            request.getRequestDispatcher("/WEB-INF/views/doctor/lich-hen.jsp")
                    .forward(request, response);
        }
        catch (DateTimeParseException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ngày khám không hợp lệ");
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
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            Doctor doctor = requireOwnDoctor(ClinicalWeb.profileId(request, "doctorId"), userId);
            if (doctor.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                throw new SecurityException("Thao tác dành cho bác sĩ cận lâm sàng");
            Long itemId = ClinicalWeb.id(request, "itemId");
            String action = request.getParameter("action");
            if ("accept".equals(action)) itemDB.accept(itemId, userId, Account.Role.DOCTOR);
            else if ("start".equals(action)) itemDB.startProcessing(itemId, userId, Account.Role.DOCTOR);
            else throw new IllegalArgumentException("Thao tác không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + itemId);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    private Doctor requireOwnDoctor(Long doctorId, Long userId)
    {
        Doctor doctor = new DoctorDB().findById(doctorId);
        if (doctor == null || !userId.equals(doctor.getUserId()))
            throw new SecurityException("Hồ sơ không thuộc bác sĩ đang đăng nhập");
        return doctor;
    }
}
