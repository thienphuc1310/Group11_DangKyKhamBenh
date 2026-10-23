package murach.DAO;

import java.util.*;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class DiagnosticOrderDB
{
    public List<DiagnosticOrder> findByEncounterForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT DISTINCT o FROM DiagnosticOrder o JOIN o.encounter e " +
                    "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.result " +
                    "WHERE e.encounterId = :encounterId AND e.doctor.doctorId = :doctorId " +
                    "ORDER BY o.orderedAt DESC", DiagnosticOrder.class)
                    .setParameter("encounterId", encounterId).setParameter("doctorId", doctorId).getResultList();
        }
        finally { em.close(); }
    }

    public List<DiagnosticOrder> findByEncounterForPatient(Long encounterId, Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT DISTINCT o FROM DiagnosticOrder o JOIN o.encounter e " +
                    "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.result " +
                    "WHERE e.encounterId = :id AND e.appointment.patient.patientId = :patientId " +
                    "AND o.status = :confirmed ORDER BY o.orderedAt", DiagnosticOrder.class)
                    .setParameter("id", encounterId).setParameter("patientId", patientId)
                    .setParameter("confirmed", DiagnosticOrder.Status.CONFIRMED).getResultList();
        }
        finally { em.close(); }
    }

    public Long createAndConfirm(Long encounterId, Long doctorId, List<Long> serviceIds, String clinicalIndication)
    {
        if (serviceIds == null || serviceIds.isEmpty() || serviceIds.size() > 50 ||
                new HashSet<>(serviceIds).size() != serviceIds.size())
            throw new IllegalArgumentException("Chọn từ 1 đến 50 dịch vụ, không chọn trùng");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            MedicalEncounter encounter = ClinicalAccess.lockForDoctor(em, encounterId, doctorId);
            ClinicalAccess.requireConsulting(encounter);
            DiagnosticOrder order = new DiagnosticOrder(ClinicalAccess.code("ORD"), encounter,
                    encounter.getDoctor(), clinicalIndication);
            for (Long serviceId : serviceIds)
            {
                DiagnosticService service = em.find(DiagnosticService.class, serviceId);
                if (service == null || !service.isStatus() || !service.getSpecialty().isStatus() ||
                        service.getSpecialty().getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL ||
                        !service.getPrice().isStatus())
                    throw new IllegalArgumentException("Dịch vụ không còn khả dụng");
                ClinicalAvailabilityDB.requireServiceAvailable(em, service, ClinicalAvailabilityDB.today());
                order.addService(service);
            }
            order.confirm();
            em.persist(order);
            InvoiceDB.issue(em, Invoice.forDiagnostics(ClinicalAccess.code("INV"), order));
            List<QueueTicket> tickets = em.createQuery("SELECT q FROM QueueTicket q WHERE q.encounter = :encounter",
                    QueueTicket.class).setParameter("encounter", encounter).getResultList();
            if (tickets.isEmpty()) throw new IllegalStateException("Chưa có số thứ tự khám");
            tickets.get(0).putOnHold();
            encounter.waitForResults();
            transaction.commit();
            return order.getOrderId();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }
}
