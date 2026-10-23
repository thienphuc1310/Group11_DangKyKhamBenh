package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class DiagnosticResultDB
{
    public DiagnosticResult findForPerformer(Long itemId, Long userId)
    {
        DiagnosticOrderItem item = new DiagnosticOrderItemDB().findForPerformer(itemId, userId);
        return item == null ? null : item.getResult();
    }

    public Long saveDraft(Long itemId, Long userId, String findings, String conclusion)
    {
        return save(itemId, userId, findings, conclusion, false);
    }

    public Long saveAndRelease(Long itemId, Long userId, String findings, String conclusion)
    {
        return save(itemId, userId, findings, conclusion, true);
    }

    // Ghi nội dung mới nhất và phát hành trong một transaction, tránh ký nhầm bản nháp từ tab khác
    private Long save(Long itemId, Long userId, String findings, String conclusion, boolean release)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DiagnosticOrderItem item = ClinicalAccess.lockItem(em, itemId);
            ClinicalAccess.requireWorkingPerformer(em, item, userId);
            SpecimenDB.requireReceivedSamples(em, item);
            if (item.getStatus() != DiagnosticOrderItem.Status.IN_PROGRESS)
                throw new IllegalStateException("Chỉ nhập kết quả khi đang thực hiện");
            DiagnosticResult result = item.getResult();
            if (result == null)
            {
                result = new DiagnosticResult(item, em.find(User.class, userId));
                em.persist(result);
            }
            result.update(findings, conclusion);
            if (release)
            {
                result.release(em.find(User.class, userId));
                EncounterWorkflow.updateResultsReady(em, item.getOrder().getEncounter());
            }
            transaction.commit();
            return result.getResultId();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    public void release(Long resultId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DiagnosticResult result = em.find(DiagnosticResult.class, resultId);
            if (result == null) throw new IllegalArgumentException("Không tìm thấy kết quả");
            DiagnosticOrderItem item = ClinicalAccess.lockItem(em, result.getOrderItem().getOrderItemId());
            ClinicalAccess.requireWorkingPerformer(em, item, userId);
            em.refresh(result, LockModeType.PESSIMISTIC_WRITE);
            SpecimenDB.requireReceivedSamples(em, item);
            result.release(em.find(User.class, userId));
            EncounterWorkflow.updateResultsReady(em, item.getOrder().getEncounter());
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    public List<DiagnosticResult> findReleasedByPatientId(Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT r FROM DiagnosticResult r JOIN FETCH r.orderItem i " +
                    "JOIN FETCH i.order o JOIN FETCH o.encounter e JOIN FETCH e.appointment a " +
                    "JOIN FETCH e.doctor d JOIN FETCH d.user WHERE a.patient.patientId = :patientId " +
                    "AND r.status = :released ORDER BY r.releasedAt DESC", DiagnosticResult.class)
                    .setParameter("patientId", patientId).setParameter("released", DiagnosticResult.Status.RELEASED)
                    .getResultList();
        }
        finally { em.close(); }
    }
}
