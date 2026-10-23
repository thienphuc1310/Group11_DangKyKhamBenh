package murach.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import murach.business.Account;
import murach.business.Specialty;
import murach.util.JPAUtil;

public class SpecialtyDB {

    // Toan: Tìm chuyên khoa theo tên, không phân biệt dấu.
    public List<Specialty> search(String keyword) {
        return getAllSpecialties().stream()
                .filter(s -> DoctorDB.matches(keyword, s.getSpecialtyId(), s.getName()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Lấy toàn bộ danh sách Chuyên khoa
    public List<Specialty> getAllSpecialties() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT s FROM Specialty s";
            TypedQuery<Specialty> query = em.createQuery(jpql, Specialty.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Tìm Chuyên khoa theo ID
    public Specialty getSpecialtyById(long specialtyId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Specialty.class, specialtyId);
        } finally {
            em.close();
        }
    }

    // Thêm Chuyên khoa mới
    public void insert(Specialty specialty) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(specialty);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ===== NGƯỜI 4: PHÂN LOẠI CHUYÊN KHOA =====

    // Tạo khoa mới với loại đã chọn, không dùng insert cũ vì hàm đó nuốt lỗi lưu.
    public Long create(String name, String description, Specialty.SpecialtyType type, Long actorUserId)
    {
        String checkedName = name == null ? "" : name.trim();
        if (checkedName.isEmpty() || checkedName.length() > 255)
            throw new IllegalArgumentException("Tên chuyên khoa từ 1 đến 255 ký tự");
        if (type == null)
            throw new IllegalArgumentException("Vui lòng chọn loại chuyên khoa");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            ClinicalAccess.requireUser(em, actorUserId, Account.Role.ADMIN);
            Long existing = em.createQuery("SELECT COUNT(s.specialtyId) FROM Specialty s " +
                    "WHERE LOWER(s.name) = :name", Long.class)
                    .setParameter("name", checkedName.toLowerCase(java.util.Locale.ROOT)).getSingleResult();
            if (existing > 0)
                throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại");

            Specialty specialty = new Specialty(checkedName, description, true, type);
            em.persist(specialty);
            transaction.commit();
            return specialty.getSpecialtyId();
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

    // Khoa lâm sàng đang hoạt động (Người 1 dùng cho luồng đặt khám)
    public List<Specialty> findActiveClinicalSpecialties()
    {
        return findActiveByType(Specialty.SpecialtyType.CLINICAL);
    }

    // Khoa cận lâm sàng đang hoạt động (Người 2 và form nhân sự/dịch vụ dùng)
    public List<Specialty> findActiveParaclinicalSpecialties()
    {
        return findActiveByType(Specialty.SpecialtyType.PARACLINICAL);
    }

    private List<Specialty> findActiveByType(Specialty.SpecialtyType type)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT s FROM Specialty s WHERE s.status = true " +
                    "AND s.specialtyType = :type ORDER BY s.name", Specialty.class)
                    .setParameter("type", type)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Đổi loại khoa: chỉ ADMIN ACTIVE; chặn khi đã có dữ liệu tham chiếu
    public void updateType(Long specialtyId, Specialty.SpecialtyType type, Long actorUserId)
    {
        if (specialtyId == null || type == null)
            throw new IllegalArgumentException("Thiếu chuyên khoa hoặc loại chuyên khoa");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try
        {
            transaction.begin();
            Long active = em.createQuery(
                    "SELECT COUNT(a.accountId) FROM Account a WHERE a.user.userId = :userId " +
                    "AND a.role = :role AND a.status = :status", Long.class)
                    .setParameter("userId", actorUserId)
                    .setParameter("role", Account.Role.ADMIN)
                    .setParameter("status", Account.Status.ACTIVE)
                    .getSingleResult();
            if (active == 0)
                throw new IllegalArgumentException("Tài khoản không có quyền phân loại chuyên khoa");

            Specialty specialty = em.find(Specialty.class, specialtyId, LockModeType.PESSIMISTIC_WRITE);
            if (specialty == null)
                throw new IllegalArgumentException("Chuyên khoa không tồn tại");

            if (specialty.getSpecialtyType() != type)
            {
                // Ghép với luồng mới: không đổi loại khoa đã được dùng ở bất kỳ phía nào
                Long doctors = em.createQuery("SELECT COUNT(d.doctorId) FROM Doctor d WHERE d.specialty = :sp",
                        Long.class).setParameter("sp", specialty).getSingleResult();
                Long technicians = em.createQuery("SELECT COUNT(t.technicianId) FROM Technician t WHERE t.specialty = :sp",
                        Long.class).setParameter("sp", specialty).getSingleResult();
                Long services = em.createQuery("SELECT COUNT(s.serviceId) FROM DiagnosticService s WHERE s.specialty = :sp",
                        Long.class).setParameter("sp", specialty).getSingleResult();
                Long encounters = em.createQuery("SELECT COUNT(e.encounterId) FROM MedicalEncounter e WHERE e.specialty = :sp",
                        Long.class).setParameter("sp", specialty).getSingleResult();
                Long diagnosticItems = em.createQuery("SELECT COUNT(i.orderItemId) FROM DiagnosticOrderItem i " +
                        "WHERE i.performingSpecialty = :sp", Long.class).setParameter("sp", specialty).getSingleResult();
                if (doctors > 0 || technicians > 0 || services > 0 || encounters > 0 || diagnosticItems > 0)
                    throw new IllegalArgumentException("Khoa đã có nhân sự hoặc dữ liệu khám/xét nghiệm. " +
                            "Chỉ phân loại khoa chưa được sử dụng để tránh sai dữ liệu liên quan");
                specialty.changeSpecialtyType(type);
            }
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
}
