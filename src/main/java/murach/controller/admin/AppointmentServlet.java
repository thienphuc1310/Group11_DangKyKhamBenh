package murach.controller.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.AppointmentDB;
import murach.DAO.PaymentDB;
import murach.DAO.SpecialtyDB;
import murach.business.Appointment;

public class AppointmentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final AppointmentDB appointmentDB = new AppointmentDB();
    private final PaymentDB paymentDB = new PaymentDB();
    private final SpecialtyDB specialtyDB = new SpecialtyDB();

    // Toan: Tìm kiếm, lọc hoặc xem chi tiết lịch hẹn.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!StaffServlet.isStaffOrAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        try {
            Long id = StaffServlet.parseLong(request.getParameter("id"));
            if (id != null) {
                Appointment appointment = appointmentDB.findById(id);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy lịch hẹn");
                    return;
                }
                request.setAttribute("appointment", appointment);
                request.setAttribute("payment", paymentDB.findByAppointmentId(id));
            } else {
                String keyword = request.getParameter("q");
                boolean searched = keyword != null;
                Long specialtyId = StaffServlet.parseLong(request.getParameter("specialtyId"));
                if (searched)
                    request.setAttribute("appointments", appointmentDB.search(keyword,
                            parseStatus(request.getParameter("status")), specialtyId,
                            parseDate(request.getParameter("date"))));
                request.setAttribute("searched", searched);
                request.setAttribute("specialties", specialtyDB.getAllSpecialties());
                request.setAttribute("selectedSpecialtyId", specialtyId);
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/appointment-list.jsp")
                    .forward(request, response);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải dữ liệu lịch khám", e);
        }
    }

    // Toan: Admin/staff xác nhận hoặc hủy lịch hẹn; hoàn tất khám thuộc quyền bác sĩ.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        if (!StaffServlet.isStaffOrAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện thao tác này.");
            return;
        }
        try {
            Long id = Long.valueOf(request.getParameter("appointmentId"));
            Appointment.Status status = parseStatus(request.getParameter("status"));
            if (status != Appointment.Status.CONFIRMED && status != Appointment.Status.CANCELLED)
                throw new IllegalArgumentException("Trạng thái không được phép");
            appointmentDB.updateAdministrativeStatus(id, status);
        } catch (IllegalArgumentException ignored) { }
        String back = request.getParameter("back");
        response.sendRedirect(request.getContextPath() + "/admin/appointments"
                + (back == null || !back.startsWith("?") ? "" : back));
    }

    // Toan: Chuyển tham số trạng thái thành enum.
    private static Appointment.Status parseStatus(String value)
    {
        try { return value == null || value.isEmpty() ? null : Appointment.Status.valueOf(value); }
        catch (IllegalArgumentException e) { return null; }
    }

    // Toan: Chuyển tham số ngày lọc thành LocalDate.
    private static LocalDate parseDate(String value)
    {
        try { return value == null || value.isEmpty() ? null : LocalDate.parse(value); }
        catch (DateTimeParseException e) { return null; }
    }
}
