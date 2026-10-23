package murach.DAO.auth;

import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import murach.business.Account;
import murach.business.Staff;
import java.util.List;
import murach.util.JPAUtil;

/** Đăng nhập nội bộ; tài khoản PATIENT vẫn dùng LoginDB riêng. */
public final class StaffLoginDB
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
                                    + "AND a.role IN (:doctorRole, :staffRole, :adminRole, :technicianRole, :pharmacistRole, :nurseRole)",
                            Account.class)
                    .setParameter("username", username.trim().toLowerCase(Locale.ROOT))
                    .setParameter("doctorRole", Account.Role.DOCTOR)
                    .setParameter("staffRole", Account.Role.STAFF)
                    .setParameter("adminRole", Account.Role.ADMIN)
                    .setParameter("technicianRole", Account.Role.TECHNICIAN)
                    .setParameter("pharmacistRole", Account.Role.PHARMACIST)
                    .setParameter("nurseRole", Account.Role.NURSE)
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

    public Long findDoctorId(Long userId)
    {
        return findProfileId(
                "SELECT d.doctorId FROM Doctor d WHERE d.user.userId = :userId", userId);
    }

    public Long findAdminId(Long userId)
    {
        return findProfileId(
                "SELECT a.adminId FROM Admin a WHERE a.user.userId = :userId", userId);
    }

    public Long findTechnicianId(Long userId)
    {
        return findProfileId("SELECT t.technicianId FROM Technician t WHERE t.user.userId = :userId", userId);
    }

    public Long findPharmacistId(Long userId)
    {
        return findProfileId("SELECT p.pharmacistId FROM Pharmacist p WHERE p.user.userId = :userId", userId);
    }

    // Ghép người 1: điều dưỡng phải có hồ sơ Nurse thật trước khi tạo session.
    public Long findNurseId(Long userId)
    {
        return findProfileId("SELECT n.nurseId FROM Nurse n WHERE n.user.userId = :userId", userId);
    }

    // STAFF nghiệp vụ khác STAFF quản lý cũ đang có hồ sơ Admin.
    public Staff findOperationalStaff(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            List<Staff> profiles = em.createQuery("SELECT s FROM Staff s WHERE s.user.userId = :userId", Staff.class)
                    .setParameter("userId", userId).getResultList();
            return profiles.isEmpty() ? null : profiles.get(0);
        }
        finally
        {
            em.close();
        }
    }

    public String findDoctorSpecialtyType(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT d.specialty.specialtyType FROM Doctor d WHERE d.user.userId = :userId",
                    murach.business.Specialty.SpecialtyType.class).setParameter("userId", userId).getSingleResult().name();
        }
        finally { em.close(); }
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
