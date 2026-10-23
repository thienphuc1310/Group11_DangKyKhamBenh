package murach.DAO;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import murach.business.Admin;
import murach.util.JPAUtil;

public class AdminDB
{
    public Admin findById(Long adminId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Admin> admins = em.createQuery("SELECT a FROM Admin a JOIN FETCH a.user WHERE a.adminId = :adminId", Admin.class)
                    .setParameter("adminId", adminId)
                    .getResultList();
            return admins.isEmpty() ? null : admins.get(0);
        } finally
        {
            em.close();
        }
    }

    //update profile cho cả admin và staff
    public Admin updateProfile(Long adminId, String fullName, String phone, String email,
                               LocalDate dateOfBirth, String gender, String address)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            Admin admin = em.find(Admin.class, adminId);
            if (admin == null)
                throw new IllegalArgumentException("Không tìm thấy hồ sơ quản trị");

            admin.getUser().updateProfile(fullName, phone, email, dateOfBirth, gender, address);
            transaction.commit();
            return admin;
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }
}
