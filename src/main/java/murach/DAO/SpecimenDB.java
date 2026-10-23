package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public class SpecimenDB
{
    public List<DiagnosticItemSpecimen> findForPerformer(Long itemId, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            DiagnosticOrderItem item = em.find(DiagnosticOrderItem.class, itemId);
            if (item == null) throw new IllegalArgumentException("Không tìm thấy chỉ định");
            ClinicalAccess.requirePerformer(em, item, userId);
            return em.createQuery("SELECT link FROM DiagnosticItemSpecimen link JOIN FETCH link.specimen s " +
                    "WHERE link.orderItem = :item ORDER BY s.collectedAt DESC", DiagnosticItemSpecimen.class)
                    .setParameter("item", item).getResultList();
        }
        finally { em.close(); }
    }

    public void collect(Long itemId, Long userId, String specimenType)
    {
        if (specimenType == null || specimenType.trim().isEmpty() || specimenType.length() > 100)
            throw new IllegalArgumentException("Nhập loại mẫu, tối đa 100 ký tự");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            DiagnosticOrderItem item = ClinicalAccess.lockItem(em, itemId);
            ClinicalAccess.requireWorkingPerformer(em, item, userId);
            if (!item.isRequiresSpecimen() || item.getStatus() != DiagnosticOrderItem.Status.ACCEPTED)
                throw new IllegalStateException("Chỉ định chưa ở bước lấy mẫu");
            Long existing = em.createQuery("SELECT COUNT(link.linkId) FROM DiagnosticItemSpecimen link " +
                    "WHERE link.orderItem = :item AND link.specimen.status <> :rejected", Long.class)
                    .setParameter("item", item).setParameter("rejected", Specimen.Status.REJECTED).getSingleResult();
            if (existing > 0) throw new IllegalStateException("Chỉ định đã có mẫu hợp lệ");
            Specimen specimen = new Specimen(ClinicalAccess.code("SPC"), item.getOrder(), specimenType);
            specimen.collect(em.find(User.class, userId));
            em.persist(specimen);
            em.persist(new DiagnosticItemSpecimen(item, specimen));
            item.markSampleCollected(specimen.getCollectedAt());
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    public void receive(Long specimenId, Long userId) { change(specimenId, userId, null); }
    public void reject(Long specimenId, Long userId, String reason) { change(specimenId, userId, reason); }

    private void change(Long specimenId, Long userId, String reason)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            Specimen specimen = em.find(Specimen.class, specimenId);
            if (specimen == null) throw new IllegalArgumentException("Không tìm thấy mẫu");
            em.refresh(specimen.getOrder().getEncounter(), LockModeType.PESSIMISTIC_WRITE);
            em.refresh(specimen, LockModeType.PESSIMISTIC_WRITE);
            List<DiagnosticItemSpecimen> links = em.createQuery("SELECT link FROM DiagnosticItemSpecimen link " +
                    "WHERE link.specimen = :specimen", DiagnosticItemSpecimen.class)
                    .setParameter("specimen", specimen).getResultList();
            if (links.isEmpty()) throw new IllegalStateException("Mẫu chưa liên kết chỉ định");
            for (DiagnosticItemSpecimen link : links)
            {
                DiagnosticOrderItem item = ClinicalAccess.lockItem(em, link.getOrderItem().getOrderItemId());
                ClinicalAccess.requireWorkingPerformer(em, item, userId);
                if (item.getStatus() != DiagnosticOrderItem.Status.SAMPLE_COLLECTED)
                    throw new IllegalStateException("Chỉ thay đổi mẫu trước khi bắt đầu phân tích");
            }
            if (reason == null) specimen.receive();
            else
            {
                if (reason.trim().isEmpty() || reason.length() > 1000)
                    throw new IllegalArgumentException("Nhập lý do từ chối mẫu, tối đa 1000 ký tự");
                specimen.reject(reason);
                for (DiagnosticItemSpecimen link : links) link.getOrderItem().requestNewSpecimen();
            }
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally { em.close(); }
    }

    static void requireReceivedSamples(EntityManager em, DiagnosticOrderItem item)
    {
        if (!item.isRequiresSpecimen()) return;
        Long received = em.createQuery("SELECT COUNT(link.linkId) FROM DiagnosticItemSpecimen link " +
                "WHERE link.orderItem = :item AND link.specimen.status = :received", Long.class)
                .setParameter("item", item).setParameter("received", Specimen.Status.RECEIVED).getSingleResult();
        Long notReceived = em.createQuery("SELECT COUNT(link.linkId) FROM DiagnosticItemSpecimen link " +
                "WHERE link.orderItem = :item AND link.specimen.status NOT IN (:received, :rejected)", Long.class)
                .setParameter("item", item).setParameter("received", Specimen.Status.RECEIVED)
                .setParameter("rejected", Specimen.Status.REJECTED).getSingleResult();
        if (received == 0 || notReceived > 0) throw new IllegalStateException("Chưa nhận đủ mẫu hợp lệ để phân tích");
    }
}
