package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class DispensationDB
{
    private static final String WAITING = " AND p.status = :confirmed AND e.status = :checkout " +
            "AND EXISTS (SELECT inv.invoiceId FROM Invoice inv WHERE inv.prescription = p " +
            "AND inv.invoiceType = :type AND inv.status = :paid " +
            "AND EXISTS (SELECT pm.paymentId FROM Payment pm WHERE pm.invoice = inv AND pm.status = :paymentPaid)) " +
            "AND NOT EXISTS (SELECT s.dispensationId FROM Dispensation s WHERE s.prescription = p AND s.status = :dispensed) ";

    public List<Prescription> findPaidPrescriptionsWaitingDispense()
    {
        return find(null);
    }

    public Prescription findPaidPrescriptionDetail(Long prescriptionId)
    {
        List<Prescription> items = find(prescriptionId);
        return items.isEmpty() ? null : items.get(0);
    }

    private List<Prescription> find(Long prescriptionId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            TypedQuery<Prescription> query = em.createQuery(PrescriptionDB.FETCH + "WHERE 1 = 1 " + WAITING +
                    (prescriptionId == null ? "" : "AND p.prescriptionId = :id ") +
                    "ORDER BY p.confirmedAt", Prescription.class)
                    .setParameter("confirmed", Prescription.Status.CONFIRMED)
                    .setParameter("checkout", MedicalEncounter.Status.WAITING_CHECKOUT)
                    .setParameter("type", Invoice.InvoiceType.MEDICINE).setParameter("paid", Invoice.Status.PAID)
                    .setParameter("paymentPaid", Payment.Status.PAID).setParameter("dispensed", Dispensation.Status.DISPENSED);
            if (prescriptionId != null) query.setParameter("id", prescriptionId);
            return query.getResultList();
        }
        finally { em.close(); }
    }

    public void dispense(Long prescriptionId, Long pharmacistId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            Prescription prescription = em.find(Prescription.class, prescriptionId);
            if (prescription == null) throw new IllegalArgumentException("Không tìm thấy đơn thuốc");
            MedicalEncounter encounter = prescription.getEncounter();
            em.refresh(encounter, LockModeType.PESSIMISTIC_WRITE);
            em.refresh(prescription, LockModeType.PESSIMISTIC_WRITE);
            Pharmacist pharmacist = em.find(Pharmacist.class, pharmacistId);
            if (pharmacist == null) throw new SecurityException("Không tìm thấy hồ sơ dược sĩ");
            ClinicalAccess.requireUser(em, pharmacist.getUserId(), Account.Role.PHARMACIST);
            if (encounter.getStatus() != MedicalEncounter.Status.WAITING_CHECKOUT ||
                    prescription.getStatus() != Prescription.Status.CONFIRMED ||
                    !InvoiceDB.isPaid(em, "prescription", prescription, Invoice.InvoiceType.MEDICINE))
                throw new IllegalStateException("Chưa ký kết luận hoặc chưa thanh toán thuốc đầy đủ");
            Long existing = em.createQuery("SELECT COUNT(s.dispensationId) FROM Dispensation s " +
                    "WHERE s.prescription = :prescription", Long.class).setParameter("prescription", prescription).getSingleResult();
            if (existing > 0) throw new IllegalStateException("Đơn đã có phiếu cấp thuốc, không cấp lại");
            Dispensation slip = new Dispensation(prescription, pharmacist, null);
            em.persist(slip);
            new StockMovementDB().dispense(em, prescription, slip, pharmacist.getUser());
            slip.complete();
            EncounterWorkflow.tryCompleteEncounter(em, encounter.getEncounterId());
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }
}
