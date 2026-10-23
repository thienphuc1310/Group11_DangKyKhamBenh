package murach.DAO.auth;

import java.util.Locale;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import murach.business.Account;
import murach.business.Admin;
import murach.business.Doctor;
import murach.business.Specialty;
import murach.business.User;
import murach.business.Nurse;
import murach.business.Technician;
import murach.business.Staff;
import murach.business.Pharmacist;
import murach.util.JPAUtil;

/** Tạo tài khoản nội bộ trong một transaction; chỉ được gọi từ chức năng quản trị. */
public final class RegisterStaffDB
{
    public Account createAdmin(String username, String password, String fullName,
                               String email, String phone)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.ADMIN, null);
    }

    public Account createStaff(String username, String password, String fullName,
                               String email, String phone)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.STAFF, null);
    }

    public Account createDoctor(String username, String password, String fullName,
                                String email, String phone, Long specialtyId)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.DOCTOR, specialtyId);
    }

    // Tiếp tân dùng role STAFF nhưng có hồ sơ staffs, không phải hồ sơ admins.
    public Account createReceptionist(String username, String password, String fullName,
                                      String email, String phone)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.STAFF, null, null, Staff.JobType.RECEPTIONIST);
    }

    public Account createNurse(String username, String password, String fullName,
                               String email, String phone, Nurse.ProfessionType professionType)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.NURSE, null, professionType);
    }

    public Account createTechnician(String username, String password, String fullName,
                                    String email, String phone, Long specialtyId)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.TECHNICIAN, specialtyId, null);
    }

    // Dược sĩ có hồ sơ riêng, không cần chọn chuyên khoa.
    public Account createPharmacist(String username, String password, String fullName,
                                    String email, String phone)
    {
        return create(username, password, fullName, email, phone,
                Account.Role.PHARMACIST, null);
    }

    private Account create(String username, String password, String fullName,
                           String email, String phone, Account.Role role,
                           Long specialtyId)
    {
        return create(username, password, fullName, email, phone, role, specialtyId, null);
    }

    private Account create(String username, String password, String fullName,
                           String email, String phone, Account.Role role, Long specialtyId,
                           Nurse.ProfessionType professionType)
    {
        return create(username, password, fullName, email, phone, role, specialtyId, professionType, null);
    }

    private Account create(String username, String password, String fullName,
                           String email, String phone, Account.Role role, Long specialtyId,
                           Nurse.ProfessionType professionType, Staff.JobType jobType)
    {
        String normalizedUsername = require(username, "Tên đăng nhập không được để trống")
                .toLowerCase(Locale.ROOT);
        String validatedPassword = require(password, "Mật khẩu không được để trống");
        if (validatedPassword.length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        String normalizedEmail = normalize(email);
        if (normalizedEmail != null) normalizedEmail = normalizedEmail.toLowerCase(Locale.ROOT);
        String checkedName = require(fullName, "Họ tên không được để trống");
        if (normalizedUsername.length() > 100 || checkedName.length() > 100
                || validatedPassword.length() > 255 || (normalizedEmail != null && normalizedEmail.length() > 255)
                || (normalize(phone) != null && normalize(phone).length() > 15))
            throw new IllegalArgumentException("Thông tin vượt độ dài cho phép: tên đăng nhập/họ tên 100, mật khẩu/email 255, điện thoại 15 ký tự");
        if (role == Account.Role.NURSE && professionType == null)
            throw new IllegalArgumentException("Vui lòng chọn nhóm nghề điều dưỡng");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            long usernameCount = em.createQuery(
                            "SELECT COUNT(a) FROM Account a WHERE LOWER(a.username) = :username",
                            Long.class)
                    .setParameter("username", normalizedUsername)
                    .getSingleResult();
            if (usernameCount > 0)
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");

            if (normalizedEmail != null)
            {
                long emailCount = em.createQuery(
                                "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = :email",
                                Long.class)
                        .setParameter("email", normalizedEmail)
                        .getSingleResult();
                if (emailCount > 0)
                    throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
            }

            User user = new User(checkedName, normalize(phone), normalizedEmail,
                    null, null, null);
            Account account = new Account(user, normalizedUsername, validatedPassword,
                    role, Account.Status.ACTIVE);
            em.persist(user);
            em.persist(account);

            // Tiếp tân tách khỏi STAFF quản lý cũ, mã hồ sơ được tạo tự động.
            if (role == Account.Role.STAFF && jobType == Staff.JobType.RECEPTIONIST)
            {
                String staffCode = "REC-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
                em.persist(new Staff(staffCode, user, jobType));
            }
            // ADMIN và STAFF quản lý dùng chung hồ sơ quản lý trong bảng admins.
            // Quyền cụ thể vẫn được phân biệt bằng Account.role.
            else if (role == Account.Role.ADMIN || role == Account.Role.STAFF)
            {
                em.persist(new Admin(user));
            }
            else if (role == Account.Role.DOCTOR)
            {
                Specialty specialty = requireSpecialty(em, specialtyId, null);

                String doctorCode = "DOC-" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
                em.persist(new Doctor(doctorCode, user, specialty,
                        null, null, null, null));
            }
            else if (role == Account.Role.NURSE)
            {
                String nurseCode = "NUR-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
                em.persist(new Nurse(nurseCode, user, professionType));
            }
            else if (role == Account.Role.TECHNICIAN)
            {
                Specialty specialty = requireSpecialty(em, specialtyId, Specialty.SpecialtyType.PARACLINICAL);
                String technicianCode = "TEC-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
                em.persist(new Technician(technicianCode, user, specialty));
            }
            else if (role == Account.Role.PHARMACIST)
            {
                String pharmacistCode = "PHA-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
                em.persist(new Pharmacist(pharmacistCode, user));
            }
            else
                throw new IllegalArgumentException("Vai trò tạo tài khoản không hợp lệ");

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

    // Cùng khóa với thao tác phân loại khoa, tránh đổi loại trong lúc cấp hồ sơ.
    private Specialty requireSpecialty(EntityManager em, Long specialtyId, Specialty.SpecialtyType requiredType)
    {
        if (specialtyId == null || specialtyId <= 0)
            throw new IllegalArgumentException("Vui lòng chọn chuyên khoa");
        Specialty specialty = em.find(Specialty.class, specialtyId, LockModeType.PESSIMISTIC_WRITE);
        if (specialty == null || !specialty.isStatus())
            throw new IllegalArgumentException("Chuyên khoa không tồn tại hoặc đã ngừng hoạt động");
        if (requiredType != null && specialty.getSpecialtyType() != requiredType)
            throw new IllegalArgumentException("Chuyên khoa không đúng loại yêu cầu: " + requiredType);
        return specialty;
    }

    private static String require(String value, String message)
    {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
