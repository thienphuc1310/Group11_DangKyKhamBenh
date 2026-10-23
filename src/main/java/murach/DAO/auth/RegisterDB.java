package murach.DAO.auth;

import java.util.Locale;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import murach.business.Account;
import murach.business.Patient;
import murach.business.User;
import murach.util.JPAUtil;

public final class RegisterDB
{
    public Account registerPatient(String fullName, String email, String phone, String password)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email không được để trống");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = (phone == null || phone.trim().isEmpty()) ? null : phone.trim();

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            Long accountCount = em.createQuery("SELECT COUNT(a) FROM Account a WHERE LOWER(a.username) = :value", Long.class).setParameter("value", normalizedEmail).getSingleResult();
            if (accountCount > 0)
            {
                throw new IllegalArgumentException("Email này đã được dùng để đăng ký tài khoản");
            }

            Long userCount = em.createQuery("SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = :value", Long.class).setParameter("value", normalizedEmail).getSingleResult();
            if (userCount > 0)
            {
                throw new IllegalArgumentException("Email này đã tồn tại trong hệ thống");
            }

            String patientCode = "PAT-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase(Locale.ROOT);

            User user = new User(fullName, normalizedPhone, normalizedEmail, null, null, null);
            Account account = new Account(user, normalizedEmail, password, Account.Role.PATIENT, Account.Status.ACTIVE);
            Patient patient = new Patient(patientCode, user);

            em.persist(user);
            em.persist(account);
            em.persist(patient);

            transaction.commit();
            return account;
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
}
