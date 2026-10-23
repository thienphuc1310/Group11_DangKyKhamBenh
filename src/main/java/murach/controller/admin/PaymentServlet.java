package murach.controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import murach.DAO.PaymentDB;
import murach.business.Payment;

public class PaymentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final PaymentDB paymentDB = new PaymentDB();

    // Toan: Hiển thị danh sách thanh toán cho admin và staff.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!StaffServlet.isStaffOrAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/staff-login");
            return;
        }
        request.setAttribute("payments", paymentDB.getAllPayments());
        request.getRequestDispatcher("/WEB-INF/views/admin/payment-list.jsp")
                .forward(request, response);
    }

    // Toan: Xác nhận đã thu đối với giao dịch đang chờ hoặc thất bại.
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
            long paymentId = Long.parseLong(request.getParameter("paymentId"));
            String action = request.getParameter("action");
            Payment payment = paymentDB.getPaymentById(paymentId);
            if (payment != null) {
                if ("confirm".equals(action)
                        && (payment.getStatus() == Payment.Status.PENDING
                        || payment.getStatus() == Payment.Status.PAID))
                    paymentDB.updateStatus(paymentId, "PAID");
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) { }
        response.sendRedirect(request.getContextPath() + "/admin/payments");
    }
}
