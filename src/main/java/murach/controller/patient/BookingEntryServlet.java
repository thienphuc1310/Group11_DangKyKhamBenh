package murach.controller.patient;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import murach.DAO.AppointmentDB;
import murach.DAO.PaymentDB;
import murach.DAO.ServicePriceDB;
import murach.business.Appointment;
import murach.business.ServicePrice;

public class BookingEntryServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final AppointmentDB appointmentDB = new AppointmentDB();
    private final PaymentDB paymentDB = new PaymentDB();
    private final ServicePriceDB servicePriceDB = new ServicePriceDB();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Long patientId = PatientSession.getPatientId(request);

        if (patientId == null)
        {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        try
        {
            String slotIdText = request.getParameter("slotId");

            if (slotIdText == null || slotIdText.trim().isEmpty())
            {
                throw new IllegalArgumentException("Vui lòng chọn khung giờ khám");
            }

            Long slotId = Long.valueOf(slotIdText.trim());

            String reason = request.getParameter("reason");

            // LẤY GIÁ ĐĂNG KÝ KHÁM ĐANG ÁP DỤNG
            ServicePrice bookingPrice =
                    servicePriceDB.findActiveByCode("DANG_KY_KHAM_BENH");

            if (bookingPrice == null)
                throw new IllegalArgumentException("Giá đăng ký khám chưa được thiết lập");

            //  TẠO APPOINTMENT PENDING
            Appointment appointment = appointmentDB.createBooking(
                    patientId, slotId, reason, bookingPrice.getAmount());
            //  TẠO PAYMENT PENDING
            paymentDB.createPendingPayment(appointment);
            // LƯU APPOINTMENT ĐANG CHỜ THANH TOÁN
            HttpSession session = request.getSession();
            session.setAttribute("pendingAppointmentId", appointment.getAppointmentId()
            );

            // 4. CHUYỂN SANG TRANG THANH TOÁN
            response.sendRedirect(request.getContextPath() + "/thanhtoan?appointmentId=" + appointment.getAppointmentId()
            );
        }
        catch (NumberFormatException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã khung giờ không hợp lệ");
        }
        catch (IllegalArgumentException e)
        {
            request.getSession().setAttribute("bookingError", e.getMessage()
            );

            response.sendRedirect(request.getContextPath() + "/main?bookingError=1"
            );
        }
    }
}
