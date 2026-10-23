package murach.DAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import murach.business.Appointment;
import murach.business.Payment;
import murach.business.Invoice;
import murach.business.MedicalEncounter;
import murach.business.Account;
import murach.business.Patient;
import javax.persistence.LockModeType;
import murach.util.JPAUtil;

public class PaymentDB
{
    // Toan: Tìm giao dịch thanh toán theo lịch hẹn để hiển thị trang chi tiết.
    public Payment findByAppointmentId(Long appointmentId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Payment> payments = em.createQuery(
                            "SELECT pm FROM Payment pm JOIN FETCH pm.appointment a LEFT JOIN FETCH pm.invoice i " +
                                    "WHERE a.appointmentId = :appointmentId " +
                                    "AND (i.invoiceId IS NULL OR i.invoiceType = :consultation) ORDER BY pm.createdAt DESC", Payment.class)
                    .setParameter("appointmentId", appointmentId)
                    .setParameter("consultation", Invoice.InvoiceType.CONSULTATION)
                    .getResultList();
            return payments.isEmpty() ? null : payments.get(0);
        }
        finally
        {
            em.close();
        }
    }
    public List<Payment> findByPatientId(Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            return em.createQuery(
                            "SELECT DISTINCT pm FROM Payment pm " +
                                    "LEFT JOIN FETCH pm.invoice inv LEFT JOIN FETCH inv.items " +
                                    "JOIN FETCH pm.appointment a " +
                                    "JOIN FETCH a.patient p " +
                                    "JOIN FETCH p.user " +
                                    "JOIN FETCH a.schedule s " +
                                    "JOIN FETCH s.doctor d " +
                                    "JOIN FETCH d.user " +
                                    "JOIN FETCH d.specialty " +
                                    "LEFT JOIN FETCH a.slot " +
                                    "WHERE p.patientId = :patientId " +
                                    "ORDER BY pm.createdAt DESC",
                            Payment.class
                    )
                    .setParameter("patientId", patientId)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public List<Payment> getAllPayments()
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            String jpql =
                    "SELECT pm FROM Payment pm " +
                            "LEFT JOIN FETCH pm.invoice " +
                            "LEFT JOIN FETCH pm.appointment a " +
                            "LEFT JOIN FETCH a.patient p " +
                            "LEFT JOIN FETCH p.user " +
                            "ORDER BY pm.createdAt DESC";

            TypedQuery<Payment> query =
                    em.createQuery(jpql, Payment.class);

            return query.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public Payment getPaymentById(long paymentId)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            return em.find(Payment.class, paymentId);
        }
        finally
        {
            em.close();
        }
    }

    public void insert(Payment payment)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();

        try
        {
            trans.begin();

            em.persist(payment);

            trans.commit();
        }
        catch (RuntimeException e)
        {
            if (trans.isActive())
                trans.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public void updateStatus(long paymentId, String status)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();

        try
        {
            trans.begin();

            Payment payment = lockPayment(em, paymentId);

            if (payment == null)
                throw new IllegalArgumentException(
                        "Không tìm thấy thanh toán"
                );

            Payment.Status target = Payment.Status.valueOf(status.trim().toUpperCase());
            if (target == Payment.Status.PAID) applyPaid(em, payment, null);
            else payment.changeStatus(target);

            trans.commit();
        }
        catch (RuntimeException e)
        {
            if (trans.isActive())
                trans.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }


    // TẠO PAYMENT PENDING CHO LỊCH KHÁM

    public Payment createPendingPayment(Appointment appointment)
    {
        if (appointment == null)
            throw new IllegalArgumentException(
                    "Lịch khám không được để trống"
            );

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();

        try
        {
            trans.begin();

            Appointment managedAppointment =
                    em.find(
                            Appointment.class,
                            appointment.getAppointmentId()
                    );

            if (managedAppointment == null)
                throw new IllegalArgumentException(
                        "Không tìm thấy lịch khám"
                );

            String transactionCode =
                    "PAY-" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 12)
                                    .toUpperCase();

            Payment payment = new Payment(
                    transactionCode,
                    managedAppointment,
                    managedAppointment.getFee(),
                    Payment.Method.BANK_TRANSFER,
                    Payment.Status.PENDING,
                    "Chờ bệnh nhân thanh toán"
            );

            em.persist(payment);

            trans.commit();

            return payment;
        }
        catch (RuntimeException e)
        {
            if (trans.isActive())
                trans.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }


    // XÁC NHẬN THANH TOÁN
    // PAYMENT PENDING -> PAID
    // APPOINTMENT PENDING -> CONFIRMED


    public void confirmPayment(Long paymentId, Long patientId, Payment.Method method)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();

        try
        {
            trans.begin();
            Payment payment = lockPayment(em, paymentId);

            if (payment == null)
                throw new IllegalArgumentException("Không tìm thấy giao dịch thanh toán");

            Appointment appointment = payment.getAppointment();

            Patient patient = em.find(Patient.class, patientId);
            if (patient == null)
                throw new SecurityException("Không tìm thấy hồ sơ bệnh nhân");
            ClinicalAccess.requireUser(em, patient.getUser().getUserId(), Account.Role.PATIENT);

            if (appointment == null)
                throw new IllegalArgumentException(
                        "Giao dịch không có lịch khám"
                );

            if (!appointment.getPatient().getPatientId().equals(patientId))
            {
                throw new IllegalArgumentException("Bạn không có quyền thanh toán lịch khám này");
            }
            //giao việc chuyển status từ PENDING -> PAID cho hàm applyPAID
            applyPaid(em, payment, method);
            trans.commit();
        }
        catch (RuntimeException e)
        {
            if (trans.isActive())
                trans.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }
    // Khóa theo thứ tự lượt khám/lịch hẹn -> hóa đơn -> giao dịch
    private Payment lockPayment(EntityManager em, Long paymentId)
    {
        Payment payment = em.find(Payment.class, paymentId);
        if (payment == null) throw new IllegalArgumentException("Không tìm thấy giao dịch");
        Invoice invoice = payment.getInvoice();
        if (invoice != null && invoice.getEncounter() != null)
            em.refresh(invoice.getEncounter(), LockModeType.PESSIMISTIC_WRITE);
        else em.refresh(payment.getAppointment(), LockModeType.PESSIMISTIC_WRITE);
        if (invoice != null) em.refresh(invoice, LockModeType.PESSIMISTIC_WRITE);
        em.refresh(payment, LockModeType.PESSIMISTIC_WRITE);
        return payment;
    }

    private void applyPaid(EntityManager em, Payment payment, Payment.Method method)
    {
        if (payment.getAppointment().getStatus() == Appointment.Status.CANCELLED)
            throw new IllegalStateException("Không thanh toán lịch hẹn đã hủy");

        if (payment.getStatus() == Payment.Status.PAID)
        {
            InvoiceDB.afterPayment(em, payment);
            return;
        }

        if (payment.getStatus() != Payment.Status.PENDING)
            throw new IllegalStateException("Giao dịch không còn chờ thanh toán");
        Invoice invoice = payment.getInvoice();

        if (invoice != null && invoice.getInvoiceType() == Invoice.InvoiceType.DIAGNOSTIC &&
                invoice.getDiagnosticOrder().getStatus() != murach.business.DiagnosticOrder.Status.CONFIRMED)
            throw new IllegalStateException("Chỉ định không còn hiệu lực");

        if (invoice != null && invoice.getInvoiceType() == Invoice.InvoiceType.MEDICINE &&
                invoice.getPrescription().getStatus() != murach.business.Prescription.Status.CONFIRMED)
            throw new IllegalStateException("Đơn thuốc không còn hiệu lực");

        if (method != null) payment.changeMethod(method);
        // gọi markPaid  đễ set status PAID
        payment.markPaid();

        if (invoice == null || invoice.getInvoiceType() == Invoice.InvoiceType.CONSULTATION)
        {
            if (payment.getAppointment().getStatus() == Appointment.Status.PENDING)
                payment.getAppointment().changeStatus(Appointment.Status.CONFIRMED);
        }
        payment.changeNote("Thanh toán thành công lúc " + LocalDateTime.now());
        InvoiceDB.afterPayment(em, payment);
    }
}
