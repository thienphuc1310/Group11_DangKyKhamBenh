package murach.DAO;

import java.util.*;
import javax.persistence.*;
import murach.business.*;
import murach.model.PrescriptionLineInput;
import murach.util.JPAUtil;

public class PrescriptionDB
{
    static final String FETCH = "SELECT DISTINCT p FROM Prescription p JOIN FETCH p.encounter e " +
            "JOIN FETCH e.appointment a JOIN FETCH a.patient pa JOIN FETCH pa.user " +
            "JOIN FETCH p.doctor d JOIN FETCH d.user LEFT JOIN FETCH p.items i LEFT JOIN FETCH i.medicine ";

    public Prescription findForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Prescription> prescriptions = em.createQuery(FETCH +
                    "WHERE e.encounterId = :encounterId AND d.doctorId = :doctorId", Prescription.class)
                    .setParameter("encounterId", encounterId).setParameter("doctorId", doctorId).getResultList();
            return prescriptions.isEmpty() ? null : prescriptions.get(0);
        }
        finally { em.close(); }
    }

    public Long saveAndConfirm(Long encounterId, Long doctorId, List<PrescriptionLineInput> lines, String advice)
    {
        if (lines == null || lines.isEmpty() || lines.size() > 50)
            throw new IllegalArgumentException("Đơn cần từ 1 đến 50 loại thuốc");
        Set<Long> unique = new HashSet<>();
        for (PrescriptionLineInput line : lines)
            if (line == null || !unique.add(line.medicineId)) throw new IllegalArgumentException("Không chọn trùng thuốc");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            MedicalEncounter encounter = ClinicalAccess.lockForDoctor(em, encounterId, doctorId);
            ClinicalAccess.requireConsulting(encounter);
            List<Prescription> existing = em.createQuery("SELECT p FROM Prescription p WHERE p.encounter = :encounter",
                    Prescription.class).setParameter("encounter", encounter)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
            Prescription prescription;
            if (existing.isEmpty()) prescription = new Prescription(ClinicalAccess.code("RX"), encounter, advice);
            else
            {
                prescription = existing.get(0);
                if (prescription.getStatus() != Prescription.Status.DRAFT)
                    throw new IllegalStateException("Lượt khám đã có đơn xác nhận; không kê trùng");
                for (PrescriptionItem item : new ArrayList<>(prescription.getItems())) em.remove(item);
                prescription.clearDraftItems(advice);
                em.flush();
            }
            List<PrescriptionLineInput> sorted = new ArrayList<>(lines);
            sorted.sort(Comparator.comparing(line -> line.medicineId));
            for (PrescriptionLineInput line : sorted)
            {
                Medicine medicine = em.find(Medicine.class, line.medicineId, LockModeType.PESSIMISTIC_WRITE);
                if (medicine == null || !medicine.isStatus() || medicine.getAvailableQuantity() < line.quantity)
                    throw new IllegalArgumentException("Thuốc không còn đủ số lượng khả dụng");
                prescription.addMedicine(medicine, line.quantity, line.dosage, line.frequency,
                        line.route, line.durationDays, line.instruction);
            }
            em.persist(prescription);
            em.flush();
            new StockMovementDB().reserveForPrescription(em, prescription, encounter.getDoctor().getUser());
            prescription.confirm();
            InvoiceDB.issue(em, Invoice.forPrescription(ClinicalAccess.code("INV"), prescription));
            transaction.commit();
            return prescription.getPrescriptionId();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    public List<Prescription> findConfirmedByPatientId(Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(FETCH + "WHERE pa.patientId = :patientId AND p.status = :confirmed " +
                    "ORDER BY p.confirmedAt DESC", Prescription.class).setParameter("patientId", patientId)
                    .setParameter("confirmed", Prescription.Status.CONFIRMED).getResultList();
        }
        finally { em.close(); }
    }
}
