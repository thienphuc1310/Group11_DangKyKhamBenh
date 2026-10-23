package murach.DAO;

import java.util.List;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

public final class NotificationDB
{
    // Dùng transaction của nghiệp vụ gọi đến, không commit riêng thông báo.
    static void send(EntityManager em, User recipient, DoctorLeaveRequest leave, String message)
    {
        em.persist(new Notification(recipient, leave, message));
    }

    public List<Notification> findForUser(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT n FROM Notification n JOIN FETCH n.leaveRequest " +
                    "WHERE n.recipient.userId = :userId " +
                    "ORDER BY n.read ASC, n.createdAt DESC, n.notificationId DESC", Notification.class)
                    .setParameter("userId", userId).setMaxResults(100).getResultList();
        }
        finally { em.close(); }
    }

    public long countUnread(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT COUNT(n.notificationId) FROM Notification n " +
                    "WHERE n.recipient.userId = :userId AND n.read = false", Long.class)
                    .setParameter("userId", userId).getSingleResult();
        }
        finally { em.close(); }
    }

    public void markRead(Long userId, Long notificationId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();
            Notification notification = em.find(Notification.class, notificationId, LockModeType.PESSIMISTIC_WRITE);
            if (notification == null || !userId.equals(notification.getRecipient().getUserId()))
                throw new SecurityException("Thông báo không thuộc tài khoản này");
            notification.markRead();
            tx.commit();
        }
        catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        finally { em.close(); }
    }
}
