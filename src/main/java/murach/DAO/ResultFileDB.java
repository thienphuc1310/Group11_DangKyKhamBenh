package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class ResultFileDB
{
    public List<ResultFile> findForPerformer(Long itemId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            DiagnosticOrderItem item = em.find(DiagnosticOrderItem.class, itemId);
            if (item == null) throw new IllegalArgumentException("Không tìm thấy chỉ định");
            ClinicalAccess.requirePerformer(em, item, userId);
            return em.createQuery("SELECT f FROM ResultFile f JOIN FETCH f.result r " +
                    "WHERE r.orderItem = :item ORDER BY f.uploadedAt", ResultFile.class)
                    .setParameter("item", item).getResultList();
        }
        finally { em.close(); }
    }

    public List<ResultFile> findForDoctor(Long encounterId, Long doctorId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT f FROM ResultFile f JOIN FETCH f.result r JOIN FETCH r.orderItem i " +
                    "JOIN i.order o JOIN o.encounter e WHERE e.encounterId = :id " +
                    "AND e.doctor.doctorId = :doctorId AND r.status = :released ORDER BY f.uploadedAt",
                    ResultFile.class).setParameter("id", encounterId).setParameter("doctorId", doctorId)
                    .setParameter("released", DiagnosticResult.Status.RELEASED).getResultList();
        }
        finally { em.close(); }
    }

    public List<ResultFile> findReleasedByPatientId(Long patientId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT f FROM ResultFile f JOIN FETCH f.result r JOIN FETCH r.orderItem i " +
                    "WHERE i.order.encounter.appointment.patient.patientId = :patientId " +
                    "AND r.status = :released ORDER BY f.uploadedAt", ResultFile.class)
                    .setParameter("patientId", patientId).setParameter("released", DiagnosticResult.Status.RELEASED)
                    .getResultList();
        }
        finally { em.close(); }
    }

    public void insert(Long resultId, Long userId, String name, String storageKey, String contentType, long size)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DiagnosticResult result = em.find(DiagnosticResult.class, resultId);
            if (result == null) throw new IllegalArgumentException("Cần lưu kết quả nháp trước khi tải tệp");
            DiagnosticOrderItem item = ClinicalAccess.lockItem(em, result.getOrderItem().getOrderItemId());
            ClinicalAccess.requireWorkingPerformer(em, item, userId);
            em.refresh(result, LockModeType.PESSIMISTIC_WRITE);
            if (item.getStatus() != DiagnosticOrderItem.Status.IN_PROGRESS)
                throw new IllegalStateException("Chỉ tải tệp khi đang thực hiện");
            em.persist(new ResultFile(result, name, storageKey, contentType, size, em.find(User.class, userId)));
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    public ResultFile findAuthorized(Long fileId, Long userId, Account.Role role)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            ClinicalAccess.requireUser(em, userId, role);
            ResultFile file = em.find(ResultFile.class, fileId);
            if (file == null) return null;
            DiagnosticResult result = file.getResult();
            DiagnosticOrderItem item = result.getOrderItem();
            MedicalEncounter encounter = item.getOrder().getEncounter();
            if (role == Account.Role.PATIENT && result.getStatus() == DiagnosticResult.Status.RELEASED &&
                    userId.equals(encounter.getPatient().getUser().getUserId())) return file;
            if (role == Account.Role.DOCTOR && result.getStatus() == DiagnosticResult.Status.RELEASED &&
                    userId.equals(encounter.getDoctor().getUser().getUserId())) return file;
            ClinicalAccess.requirePerformer(em, item, userId);
            return file;
        }
        finally { em.close(); }
    }
}
