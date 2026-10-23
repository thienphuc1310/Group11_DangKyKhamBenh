package murach.DAO.auth;

import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import murach.business.Account;
import murach.util.JPAUtil;

public final class LoginDB
{
    public Account authenticate(String username, String password)
    {
        if (username == null || password == null)
            return null;

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            Account account = em.createQuery(
                            "SELECT a FROM Account a JOIN FETCH a.user "
                                    + "WHERE a.username = :username "
                                    + "AND a.role = :role",
                            Account.class)
                    .setParameter("username", username.trim().toLowerCase(Locale.ROOT))
                    .setParameter("role", Account.Role.PATIENT)
                    .getSingleResult();

            if (account.getStatus() != Account.Status.ACTIVE
                    || !account.getPasswordHash().equals(password))
                return null;

            transaction.begin();
            account.recordLogin();
            transaction.commit();
            return account;
        }
        catch (NoResultException e)
        {
            return null;
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public Long findPatientId(Long userId)
    {
        return findProfileId("SELECT p.patientId FROM Patient p WHERE p.user.userId = :userId", userId);
    }

    private Long findProfileId(String jpql, Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(jpql, Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
        finally
        {
            em.close();
        }
    }

}
