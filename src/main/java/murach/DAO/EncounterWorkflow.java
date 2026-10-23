package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;

/** Điều kiện hoàn tất dùng lại sau kết luận, thanh toán cuối và cấp thuốc. */
public final class EncounterWorkflow
{
    private EncounterWorkflow() {}

    public static long countPendingResults(EntityManager em, MedicalEncounter encounter)
    {
        return em.createQuery("SELECT COUNT(i.orderItemId) FROM DiagnosticOrderItem i " +
                "WHERE i.order.encounter = :encounter AND i.order.status = :confirmed " +
                "AND i.status <> :cancelled AND NOT EXISTS (SELECT r.resultId FROM DiagnosticResult r " +
                "WHERE r.orderItem = i AND r.status = :released)", Long.class)
                .setParameter("encounter", encounter).setParameter("confirmed", DiagnosticOrder.Status.CONFIRMED)
                .setParameter("cancelled", DiagnosticOrderItem.Status.CANCELLED)
                .setParameter("released", DiagnosticResult.Status.RELEASED).getSingleResult();
    }

    public static void updateResultsReady(EntityManager em, MedicalEncounter encounter)
    {
        em.flush();
        long required = em.createQuery("SELECT COUNT(i.orderItemId) FROM DiagnosticOrderItem i " +
                "WHERE i.order.encounter = :encounter AND i.order.status = :confirmed AND i.status <> :cancelled",
                Long.class).setParameter("encounter", encounter)
                .setParameter("confirmed", DiagnosticOrder.Status.CONFIRMED)
                .setParameter("cancelled", DiagnosticOrderItem.Status.CANCELLED).getSingleResult();
        if (encounter.getStatus() == MedicalEncounter.Status.WAITING_RESULTS && required > 0 &&
                countPendingResults(em, encounter) == 0)
            encounter.markResultsReady(required, required);
    }

    public static boolean tryCompleteEncounter(EntityManager em, Long encounterId)
    {
        if (!em.getTransaction().isActive()) throw new IllegalStateException("Cần transaction đang mở");
        MedicalEncounter encounter = em.find(MedicalEncounter.class, encounterId, LockModeType.PESSIMISTIC_WRITE);
        if (encounter == null) throw new IllegalArgumentException("Không tìm thấy lượt khám");
        if (encounter.getStatus() == MedicalEncounter.Status.COMPLETED) return true;
        if (encounter.getStatus() != MedicalEncounter.Status.WAITING_CHECKOUT) return false;
        em.flush();
        Long finalized = em.createQuery("SELECT COUNT(r.recordId) FROM MedicalRecord r " +
                "WHERE r.encounter = :encounter AND r.status = :finalized", Long.class)
                .setParameter("encounter", encounter).setParameter("finalized", MedicalRecord.Status.FINALIZED)
                .getSingleResult();
        if (finalized == 0 || countPendingResults(em, encounter) > 0) return false;
        Long consultationPaid = em.createQuery("SELECT COUNT(p.paymentId) FROM Payment p LEFT JOIN p.invoice i " +
                "WHERE p.appointment = :appointment AND p.status = :paid " +
                "AND (i.invoiceId IS NULL OR (i.invoiceType = :type AND i.status = :invoicePaid))", Long.class)
                .setParameter("appointment", encounter.getAppointment()).setParameter("paid", Payment.Status.PAID)
                .setParameter("type", Invoice.InvoiceType.CONSULTATION).setParameter("invoicePaid", Invoice.Status.PAID)
                .getSingleResult();
        if (consultationPaid == 0) return false;
        Long debt = em.createQuery("SELECT COUNT(i.invoiceId) FROM Invoice i WHERE " +
                "(i.encounter = :encounter OR (i.appointment = :appointment AND i.invoiceType = :consultation)) " +
                "AND i.status IN (:draft, :unpaid)", Long.class)
                .setParameter("encounter", encounter).setParameter("appointment", encounter.getAppointment())
                .setParameter("consultation", Invoice.InvoiceType.CONSULTATION)
                .setParameter("draft", Invoice.Status.DRAFT).setParameter("unpaid", Invoice.Status.UNPAID).getSingleResult();
        if (debt > 0) return false;
        List<DiagnosticOrder> orders = em.createQuery("SELECT o FROM DiagnosticOrder o WHERE " +
                "o.encounter = :encounter AND o.status = :confirmed", DiagnosticOrder.class)
                .setParameter("encounter", encounter).setParameter("confirmed", DiagnosticOrder.Status.CONFIRMED)
                .getResultList();
        for (DiagnosticOrder order : orders)
            if (!InvoiceDB.isPaid(em, "diagnosticOrder", order, Invoice.InvoiceType.DIAGNOSTIC)) return false;
        List<Prescription> prescriptions = em.createQuery("SELECT p FROM Prescription p " +
                "WHERE p.encounter = :encounter", Prescription.class).setParameter("encounter", encounter).getResultList();
        for (Prescription prescription : prescriptions)
        {
            if (prescription.getStatus() == Prescription.Status.CANCELLED)
            {
                if (new StockMovementDB().hasOutstandingReservation(em, prescription)) return false;
                Long activeInvoice = em.createQuery("SELECT COUNT(i.invoiceId) FROM Invoice i " +
                        "WHERE i.prescription = :prescription AND i.status <> :cancelled", Long.class)
                        .setParameter("prescription", prescription).setParameter("cancelled", Invoice.Status.CANCELLED)
                        .getSingleResult();
                if (activeInvoice > 0) return false;
                continue;
            }
            if (prescription.getStatus() != Prescription.Status.CONFIRMED ||
                    !InvoiceDB.isPaid(em, "prescription", prescription, Invoice.InvoiceType.MEDICINE)) return false;
            Long dispensed = em.createQuery("SELECT COUNT(d.dispensationId) FROM Dispensation d " +
                    "WHERE d.prescription = :prescription AND d.status = :dispensed", Long.class)
                    .setParameter("prescription", prescription).setParameter("dispensed", Dispensation.Status.DISPENSED)
                    .getSingleResult();
            if (dispensed == 0) return false;
        }
        encounter.complete();
        encounter.getAppointment().complete();
        return true;
    }
}
