package murach.DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import murach.business.Account;
import murach.business.DiagnosticService;
import murach.business.DoctorSchedule;
import murach.business.ServicePrice;
import murach.business.Specialty;
import murach.business.User;
import murach.util.JPAUtil;

public class DiagnosticServiceDB
{
    // Admin: xem toàn bộ danh mục (kể cả dịch vụ đã tắt)
    public List<DiagnosticService> getAll()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT s FROM DiagnosticService s JOIN FETCH s.specialty " +
                    "JOIN FETCH s.price ORDER BY s.serviceName",
                    DiagnosticService.class)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Danh mục cho bác sĩ: vẫn thấy dịch vụ đang dùng dù hôm nay chưa có nhân sự.
    public List<DiagnosticService> findActiveServices()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT s FROM DiagnosticService s JOIN FETCH s.specialty sp " +
                    "JOIN FETCH s.price p WHERE s.status = true AND sp.status = true " +
                    "AND sp.specialtyType = :type AND p.status = true ORDER BY s.serviceName",
                    DiagnosticService.class)
                    .setParameter("type", Specialty.SpecialtyType.PARACLINICAL)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Chỉ cho chọn dịch vụ có nhân sự cùng khoa làm việc hôm nay, tài khoản còn hoạt động.
    public List<DiagnosticService> findAvailableServices()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT s FROM DiagnosticService s JOIN FETCH s.specialty sp " +
                    "JOIN FETCH s.price p " +
                    "WHERE s.status = true AND sp.status = true " +
                    "AND sp.specialtyType = :type AND p.status = true AND (" +
                    "(s.serviceType = :imaging AND EXISTS (SELECT ds.scheduleId FROM DoctorSchedule ds " +
                    "WHERE ds.doctor.specialty = sp AND ds.workDate = :date AND ds.status = :working AND EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = ds.doctor.user AND a.role = :doctorRole AND a.status = :active))) " +
                    "OR (s.serviceType = :laboratory AND EXISTS (SELECT ts.scheduleId FROM TechnicianSchedule ts " +
                    "WHERE ts.technician.specialty = sp AND ts.workDate = :date AND EXISTS " +
                    "(SELECT a.accountId FROM Account a WHERE a.user = ts.technician.user AND a.role = :technicianRole AND a.status = :active)))) " +
                    "ORDER BY s.serviceName",
                    DiagnosticService.class)
                    .setParameter("type", Specialty.SpecialtyType.PARACLINICAL)
                    .setParameter("working", DoctorSchedule.Status.ACTIVE)
                    .setParameter("imaging", DiagnosticService.ServiceType.IMAGING)
                    .setParameter("laboratory", DiagnosticService.ServiceType.LABORATORY)
                    .setParameter("doctorRole", Account.Role.DOCTOR).setParameter("technicianRole", Account.Role.TECHNICIAN)
                    .setParameter("active", Account.Status.ACTIVE).setParameter("date", ClinicalAvailabilityDB.today())
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Tạo dịch vụ và giá nhập trực tiếp trong cùng transaction, tránh bảng giá mồ côi.
    public Long insertWithPrice(String code, String name, DiagnosticService.ServiceType type,
                                Long specialtyId, BigDecimal amount,
                                boolean requiresSpecimen, int minutes, Long actorUserId)
    {
        validateCodeAndName(code, name);
        BigDecimal checkedAmount = requireAmount(amount);
        String checkedCode = code.trim();
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            requireActiveAdmin(em, actorUserId);
            if (specialtyId == null || specialtyId <= 0)
                throw new IllegalArgumentException("Phải chọn khoa thực hiện");
            // Cùng khóa khoa với updateType, tránh đổi loại trong lúc tạo dịch vụ.
            Specialty specialty = em.find(Specialty.class, specialtyId, LockModeType.PESSIMISTIC_WRITE);
            if (specialty == null || !specialty.isStatus()
                    || specialty.getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
                throw new IllegalArgumentException("Phải chọn khoa cận lâm sàng đang hoạt động");

            Long duplicateService = em.createQuery(
                    "SELECT COUNT(s.serviceId) FROM DiagnosticService s WHERE LOWER(s.serviceCode) = :code", Long.class)
                    .setParameter("code", checkedCode.toLowerCase(Locale.ROOT)).getSingleResult();
            if (duplicateService > 0)
                throw new IllegalArgumentException("Mã dịch vụ đã tồn tại");
            Long duplicatePrice = em.createQuery(
                    "SELECT COUNT(p.priceId) FROM ServicePrice p WHERE LOWER(p.serviceCode) = :code", Long.class)
                    .setParameter("code", checkedCode.toLowerCase(Locale.ROOT)).getSingleResult();
            if (duplicatePrice > 0)
                throw new IllegalArgumentException("Mã này đã có trong bảng giá, vui lòng dùng mã dịch vụ mới");

            User admin = em.find(User.class, actorUserId);
            if (admin == null) throw new IllegalArgumentException("Không tìm thấy người tạo dịch vụ");
            ServicePrice price = new ServicePrice(checkedCode, name.trim(), checkedAmount, true, admin);
            DiagnosticService service = new DiagnosticService(checkedCode, name.trim(), type,
                    specialty, price, requiresSpecimen, minutes);
            em.persist(price);
            em.persist(service);
            transaction.commit();
            return service.getServiceId();
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

    // Giữ API chọn bảng giá có sẵn để tương thích những chỗ gọi cũ.
    public Long insert(String code, String name, DiagnosticService.ServiceType type,
                       Long specialtyId, Long priceId,
                       boolean requiresSpecimen, int minutes, Long actorUserId)
    {
        validateCodeAndName(code, name);
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            requireActiveAdmin(em, actorUserId);

            if (specialtyId == null || priceId == null)
                throw new IllegalArgumentException("Phải chọn khoa thực hiện và bảng giá");
            // Cùng khóa khoa với updateType, tránh đổi loại khoa trong lúc tạo dịch vụ
            Specialty specialty = em.find(Specialty.class, specialtyId, LockModeType.PESSIMISTIC_WRITE);
            ServicePrice price = em.find(ServicePrice.class, priceId, LockModeType.PESSIMISTIC_WRITE);
            if (specialty == null || !specialty.isStatus() || price == null || !price.isStatus())
                throw new IllegalArgumentException("Khoa hoặc bảng giá chưa sẵn sàng");
            Long used = em.createQuery(
                    "SELECT COUNT(s.serviceId) FROM DiagnosticService s WHERE s.price = :price",
                    Long.class)
                    .setParameter("price", price)
                    .getSingleResult();
            if (used > 0)
                throw new IllegalArgumentException("Bảng giá đã gắn với dịch vụ khác");

            if (code == null || code.trim().isEmpty() || !code.trim().equals(price.getServiceCode()))
                throw new IllegalArgumentException("Mã dịch vụ phải trùng với mã bảng giá");
            DiagnosticService service = new DiagnosticService(code.trim(), name, type,
                    specialty, price, requiresSpecimen, minutes);
            em.persist(service);
            transaction.commit();
            return service.getServiceId();
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

    // Tắt/bật dịch vụ: không xóa, không đụng tới dữ liệu đã phát sinh
    public void changeStatus(Long serviceId, boolean status, Long actorUserId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            requireActiveAdmin(em, actorUserId);
            DiagnosticService service = em.find(DiagnosticService.class, serviceId, LockModeType.PESSIMISTIC_WRITE);
            if (service == null)
                throw new IllegalArgumentException("Dịch vụ không tồn tại");
            service.changeStatus(status);
            transaction.commit();
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

    private void validateCodeAndName(String code, String name)
    {
        if (code == null || code.trim().isEmpty() || code.trim().length() > 100 ||
                name == null || name.trim().isEmpty() || name.trim().length() > 255)
            throw new IllegalArgumentException("Mã dịch vụ từ 1–100, tên từ 1–255 ký tự");
        if ("DANG_KY_KHAM_BENH".equalsIgnoreCase(code.trim()))
            throw new IllegalArgumentException("Mã đăng ký khám bệnh không dùng cho dịch vụ cận lâm sàng");
    }

    private BigDecimal requireAmount(BigDecimal amount)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá dịch vụ phải lớn hơn 0");
        if (amount.compareTo(new BigDecimal("9999999999999999.99")) > 0)
            throw new IllegalArgumentException("Giá dịch vụ vượt quá giới hạn cho phép");
        try
        {
            BigDecimal checked = amount.setScale(2, RoundingMode.UNNECESSARY);
            if (checked.precision() > 18)
                throw new IllegalArgumentException("Giá dịch vụ vượt quá giới hạn cho phép");
            return checked;
        }
        catch (ArithmeticException e)
        {
            throw new IllegalArgumentException("Giá dịch vụ chỉ được có tối đa 2 chữ số thập phân");
        }
    }

    private void requireActiveAdmin(EntityManager em, Long actorUserId)
    {
        Long active = em.createQuery(
                "SELECT COUNT(a.accountId) FROM Account a WHERE a.user.userId = :userId " +
                "AND a.role = :role AND a.status = :status", Long.class)
                .setParameter("userId", actorUserId)
                .setParameter("role", Account.Role.ADMIN)
                .setParameter("status", Account.Status.ACTIVE)
                .getSingleResult();
        if (active == 0)
            throw new IllegalArgumentException("Không có quyền quản lý dịch vụ");
    }

    //tìm dịch vụ cận lâm sàng theo tên
    public List<DiagnosticService> findActiveByName(String keyword)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String name = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

            name = name.replace("!", "!!").replace("%", "!%")
                    .replace("_", "!_").replace("[", "![");

            String jpql = "SELECT s FROM DiagnosticService s " +
                    "JOIN FETCH s.specialty sp " +
                    "JOIN FETCH s.price p " +
                    "WHERE s.status = true AND sp.status = true " +
                    "AND sp.specialtyType = :type AND p.status = true " +
                    "AND LOWER(s.serviceName) LIKE :name ESCAPE '!' " +
                    "ORDER BY s.serviceName";

            return em.createQuery(jpql, DiagnosticService.class)
                    .setParameter("type", Specialty.SpecialtyType.PARACLINICAL)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }
}
