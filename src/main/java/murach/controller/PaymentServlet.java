package murach.controller;

import murach.DAO.PaymentDB;
import murach.business.Payment;
import murach.business.Account;
import murach.business.Invoice;
import murach.controller.clinical.ClinicalWeb;
import murach.controller.patient.PatientSession;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.io.IOException;
import java.util.List;

public class PaymentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final PaymentDB dao =
            new PaymentDB();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return;
            Long patientId = ClinicalWeb.profileId(request, "patientId");

            if (patientId == null)
            {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            List<Payment> payments = dao.findByPatientId(patientId);
            String value = request.getParameter("appointmentId");
            String paymentValue = request.getParameter("paymentId");

            // THAY ĐỔI: Không mặc định lấy payments.get(0) nữa
            Payment selected = null;

            if (paymentValue != null && !paymentValue.trim().isEmpty())
            {
                Long paymentId = ClinicalWeb.id(request, "paymentId");
                selected = payments.stream().filter(p -> paymentId.equals(p.getPaymentId())).findFirst().orElse(null);
                if (selected == null) { response.sendError(404, "Không tìm thấy thanh toán của bạn"); return; }
            }
            else if (value != null && !value.trim().isEmpty())
            {
                Long appointmentId = Long.valueOf(value.trim());

                selected = payments.stream()
                        .filter(p -> appointmentId.equals(p.getAppointment().getAppointmentId()))
                        .filter(p -> p.getInvoice() == null || p.getInvoice().getInvoiceType() == Invoice.InvoiceType.CONSULTATION)
                        .findFirst()
                        .orElse(null);

                if (selected == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thanh toán");
                    return;
                }
            }

            request.setAttribute("payments", payments);
            request.setAttribute("selectedPayment", selected);

            request.getRequestDispatcher("/WEB-INF/views/thanh-toan.jsp").forward(request, response);
        }
        catch (NumberFormatException e)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "appointmentId phải là số nguyên");
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }


    // XÁC NHẬN THANH TOÁN


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");

        try
        {
            if (ClinicalWeb.requireUser(request, response, Account.Role.PATIENT) == null) return;
            ClinicalWeb.requireCsrf(request);
            Long patientId = ClinicalWeb.profileId(request, "patientId");
            String paymentIdText =
                    request.getParameter("paymentId");

            String methodText =
                    request.getParameter("method");

            if (paymentIdText == null ||
                    paymentIdText.trim().isEmpty())
            {
                throw new IllegalArgumentException(
                        "Không xác định được giao dịch"
                );
            }

            if (methodText == null ||
                    methodText.trim().isEmpty())
            {
                throw new IllegalArgumentException(
                        "Vui lòng chọn phương thức thanh toán"
                );
            }

            Long paymentId =
                    Long.valueOf(
                            paymentIdText.trim()
                    );

            Payment.Method method;

            try
            {
                method =
                        Payment.Method.valueOf(
                                methodText.trim()
                                        .toUpperCase()
                        );
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException(
                        "Phương thức thanh toán không hợp lệ"
                );
            }

            dao.confirmPayment(
                    paymentId,
                    patientId,
                    method
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/thanhtoanthanhcong"
            );
        }
        catch (NumberFormatException e)
        {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã thanh toán không hợp lệ"
            );
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            request.setAttribute(
                    "paymentError",
                    e.getMessage()
            );

            doGet(request, response);
        }
        catch (RuntimeException e)
        {
            ClinicalWeb.fail(response, e);
        }
    }
}
