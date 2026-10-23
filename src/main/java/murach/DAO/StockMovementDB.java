package murach.DAO;

import java.util.*;
import java.time.LocalDateTime;
import javax.persistence.*;
import murach.business.*;
import murach.util.JPAUtil;

/** API phối hợp với kho: dùng EntityManager của bên gọi, không tự commit/close. */
public class StockMovementDB
{
    // Người 3: quyền nhập kho độc lập với quyền bác sĩ giữ thuốc cho đơn.
    public User requirePharmacist(EntityManager em, Long userId)
    {
        if (userId == null || userId <= 0) throw new SecurityException("Tài khoản nhập kho không hợp lệ");
        List<Account> accounts = em.createQuery(
                "SELECT a FROM Account a JOIN FETCH a.user u " +
                "WHERE u.userId = :userId AND a.role = :role AND a.status = :status " +
                "AND EXISTS (SELECT p.pharmacistId FROM Pharmacist p WHERE p.user = u)", Account.class)
                .setParameter("userId", userId).setParameter("role", Account.Role.PHARMACIST)
                .setParameter("status", Account.Status.ACTIVE).getResultList();
        if (accounts.isEmpty()) throw new SecurityException("Tài khoản dược sĩ không hợp lệ, thiếu hồ sơ hoặc đã khóa");
        return accounts.get(0).getUser();
    }

    // Thuốc + tồn + lịch sử nhập phải cùng transaction do MedicineDB quản lý.
    public void receive(EntityManager em, Medicine medicine, int quantity, LocalDateTime receivedAt, User user)
    {
        requireManaged(em, medicine, user);
        if (quantity < 1) throw new IllegalArgumentException("Số viên nhập phải lớn hơn 0");
        try { Math.addExact(medicine.getQuantityOnHand(), quantity); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Tổng số viên vượt giới hạn hệ thống"); }
        em.persist(StockMovement.receive(medicine, quantity, receivedAt, user, "Nhập thuốc vào kho"));
    }

    public List<StockMovement> findByMedicineId(Long medicineId)
    {
        if (medicineId == null || medicineId <= 0) throw new IllegalArgumentException("Mã thuốc không hợp lệ");
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery("SELECT sm FROM StockMovement sm JOIN FETCH sm.performedBy " +
                    "JOIN FETCH sm.medicine WHERE sm.medicine.medicineId = :id " +
                    "ORDER BY sm.createdAt DESC, sm.movementId DESC", StockMovement.class)
                    .setParameter("id", medicineId).getResultList();
        }
        finally { em.close(); }
    }

    private void requireManaged(EntityManager em, Object... values)
    {
        if (em == null || !em.getTransaction().isActive())
            throw new IllegalStateException("Thao tác kho phải nằm trong transaction đang mở");
        for (Object value : values)
            if (value == null || !em.contains(value))
                throw new IllegalStateException("Entity kho phải được quản lý bởi EntityManager của bên gọi");
    }

    private List<PrescriptionItem> lockMedicines(EntityManager em, Prescription prescription)
    {
        if (!em.getTransaction().isActive() || !em.contains(prescription))
            throw new IllegalStateException("Đơn thuốc phải nằm trong transaction đang mở");
        List<PrescriptionItem> items = new ArrayList<>(prescription.getItems());
        items.sort(Comparator.comparing(i -> i.getMedicine().getMedicineId()));
        for (PrescriptionItem item : items)
            em.refresh(item.getMedicine(), LockModeType.PESSIMISTIC_WRITE);
        return items;
    }

    private long total(EntityManager em, PrescriptionItem item, StockMovement.MovementType type)
    {
        return em.createQuery("SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
                "WHERE m.prescriptionItem = :item AND m.movementType = :type", Long.class)
                .setParameter("item", item).setParameter("type", type).getSingleResult();
    }

    private long held(EntityManager em, PrescriptionItem item)
    {
        return total(em, item, StockMovement.MovementType.RESERVE)
                - total(em, item, StockMovement.MovementType.RELEASE)
                - total(em, item, StockMovement.MovementType.DISPENSE);
    }

    public void reserveForPrescription(EntityManager em, Prescription prescription, User user)
    {
        requireManaged(em, prescription, user);
        if (prescription.getStatus() != Prescription.Status.DRAFT)
            throw new IllegalStateException("Chỉ giữ thuốc cho đơn nháp trước khi xác nhận");
        for (PrescriptionItem item : lockMedicines(em, prescription))
        {
            if (total(em, item, StockMovement.MovementType.RESERVE) != 0)
                throw new IllegalStateException("Dòng đơn thuốc đã được giữ kho");
            em.persist(StockMovement.reserve(item.getMedicine(), item, item.getQuantity(), user));
        }
    }

    public void releaseForPrescription(EntityManager em, Prescription prescription, User user)
    {
        requireManaged(em, prescription, user);
        for (PrescriptionItem item : lockMedicines(em, prescription))
        {
            if (total(em, item, StockMovement.MovementType.DISPENSE) > 0)
                throw new IllegalStateException("Không giải phóng đơn đã cấp thuốc");
            long remaining = held(em, item);
            if (remaining < 0 || remaining > item.getQuantity())
                throw new IllegalStateException("Lịch sử giữ kho không hợp lệ");
            if (remaining > 0) em.persist(StockMovement.release(item.getMedicine(), item,
                    Math.toIntExact(remaining), user, "Giải phóng lượng giữ của đơn"));
        }
    }

    public boolean hasOutstandingReservation(EntityManager em, Prescription prescription)
    {
        for (PrescriptionItem item : prescription.getItems())
            if (held(em, item) != 0) return true;
        return false;
    }

    public void dispense(EntityManager em, Prescription prescription, Dispensation slip, User user)
    {
        requireManaged(em, prescription, slip, user);
        for (PrescriptionItem item : lockMedicines(em, prescription))
        {
            if (held(em, item) != item.getQuantity() || total(em, item, StockMovement.MovementType.DISPENSE) != 0)
                throw new IllegalStateException("Chưa giữ đủ thuốc cho đúng dòng đơn hoặc đã cấp thuốc");
            em.persist(StockMovement.dispense(item.getMedicine(), item, slip, item.getQuantity(), user));
        }
    }
}
