package murach.controller.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.DAO.DoctorScheduleDB;
import murach.DAO.DoctorScheduleDB.SlotInfo;
import murach.business.Doctor;

public class DoctorBySpecialtyServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = request.getParameter("action");

        try
        {
            if ("doctors".equals(action))
                writeDoctors(request, response);
            else if ("schedules".equals(action))
                writeSchedules(request, response);
            else
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Thao tác tải dữ liệu không hợp lệ");
        }
        catch (NumberFormatException | DateTimeParseException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Tham số ngày hoặc mã dữ liệu không hợp lệ");
        }
        catch (IllegalArgumentException e)
        {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
        catch (RuntimeException e)
        {
            throw new ServletException("Không thể tải lựa chọn đặt khám", e);
        }
    }

    private void writeDoctors(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        Long specialtyId = Long.valueOf(require(request, "specialtyId"));
        LocalDate workDate = LocalDate.parse(require(request, "workDate"));
        List<Doctor> doctors = DoctorScheduleDB.findAvailableDoctors(specialtyId, workDate);
        PrintWriter out = response.getWriter();

        if (doctors.isEmpty())
        {
            out.print("<option value=\"\">-- Không có bác sĩ phù hợp --</option>");
            return;
        }

        out.print("<option value=\"\">-- Chọn bác sĩ --</option>");
        for (Doctor doctor : doctors)
        {
            out.print("<option value=\"" + doctor.getDoctorId() + "\">BS. "
                    + escapeHtml(doctor.getFullName()) + "</option>");
        }
    }

    private void writeSchedules(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        Long doctorId = Long.valueOf(require(request, "doctorId"));
        LocalDate workDate = LocalDate.parse(require(request, "workDate"));
        List<SlotInfo> slots = DoctorScheduleDB.findAvailableSlots(doctorId, workDate);
        PrintWriter out = response.getWriter();

        if (slots.isEmpty())
        {
            out.print("<p class=\"empty-time-slots\">Không có khung giờ khám.</p>");
            return;
        }

        for (SlotInfo info : slots)
        {
            String inputId = "slot-" + info.getSlot().getSlotId();
            boolean full = info.getRemainingCount() <= 0;
            out.print("<input type=\"radio\" id=\"" + inputId
                    + "\" name=\"slotId\" value=\"" + info.getSlot().getSlotId()
                    + "\" required" + (full ? " disabled" : "") + ">");
            out.print("<label for=\"" + inputId + "\">"
                    + info.getSlot().getStartTime() + " – " + info.getSlot().getEndTime()
                    + (info.isWaitingReplacement() ? " · Chờ bố trí bác sĩ thay thế" : "")
                    + "</label>");
        }
    }

    private String require(HttpServletRequest request, String parameterName)
    {
        String value = request.getParameter(parameterName);
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Thiếu tham số " + parameterName);
        return value.trim();
    }

    private String escapeHtml(String value)
    {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
