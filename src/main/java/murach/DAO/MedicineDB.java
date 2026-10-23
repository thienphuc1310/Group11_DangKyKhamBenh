package murach.DAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import murach.business.Medicine;
import murach.business.User;
import murach.util.JPAUtil;

public class MedicineDB
{

    private final StockMovementDB stockDB = new StockMovementDB();

    // GET danh mục/lịch sử cũng xác minh hồ sơ dược sĩ hiện tại trong DB.
    public void requireInventoryAccess(Long userId)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try { stockDB.requirePharmacist(em, userId); }
        finally { em.close(); }
    }

    public List<Medicine> search(String keyword)
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            String value = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

            return em.createQuery(
                    "SELECT m FROM Medicine m "
                    + "WHERE LOWER(m.medicineName) LIKE :q "
                    + "OR LOWER(m.medicineCode) LIKE :q "
                    + "ORDER BY m.medicineName",
                    Medicine.class)
                    .setParameter("q", "%" + value + "%")
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public List<Medicine> findAvailableMedicines()
    {
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.createQuery(
                    "SELECT m FROM Medicine m "
                    + "WHERE m.status = true "
                    + "AND m.quantityOnHand > m.quantityReserved "
                    + "ORDER BY m.medicineName",
                    Medicine.class)
                    .getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public Medicine findById(Long id)
    {
        if (id == null || id <= 0) throw new IllegalArgumentException("Mã thuốc không hợp lệ");
        EntityManager em = JPAUtil.getEntityManager();
        try
        {
            return em.find(Medicine.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public Long createAndReceive(String name, String description, BigDecimal price,
                                 int quantity, LocalDate date, Long userId)
    {
        validate(name, price, quantity, date);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try
        {
            tx.begin();
            User user = stockDB.requirePharmacist(em, userId);

            Long duplicates = em.createQuery(
                    "SELECT COUNT(m) FROM Medicine m "
                    + "WHERE LOWER(m.medicineName) = :name",
                    Long.class)
                    .setParameter("name", name.trim().toLowerCase(Locale.ROOT))
                    .getSingleResult();

            if (duplicates > 0)
            {
                throw new IllegalArgumentException(
                        "Tên thuốc đã có trong danh mục; hãy kiểm tra thuốc hiện có trước khi nhập thêm");
            }

            Medicine medicine = new Medicine(name, description, price);
            em.persist(medicine);
            stockDB.receive(em, medicine, quantity, date.atStartOfDay(), user);
            tx.commit();

            return medicine.getMedicineId();
        }
        catch (RuntimeException e)
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public void receiveAdditional(Long id, int quantity, LocalDate date, Long userId)
    {
        if (id == null || id <= 0)
        {
            throw new IllegalArgumentException("Mã thuốc không hợp lệ");
        }
        validateDateQuantity(quantity, date);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try
        {
            tx.begin();
            User user = stockDB.requirePharmacist(em, userId);
            Medicine medicine = em.find(Medicine.class, id, LockModeType.PESSIMISTIC_WRITE);

            if (medicine == null)
            {
                throw new IllegalArgumentException("Thuốc không tồn tại");
            }
            if (!medicine.isStatus())
            {
                throw new IllegalStateException("Thuốc đã ngừng sử dụng");
            }

            stockDB.receive(em, medicine, quantity, date.atStartOfDay(), user);
            tx.commit();
        }
        catch (RuntimeException e)
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    public void updateInformation(Long id, String name, String description,
                                  BigDecimal price, Long userId)
    {
        if (id == null || id <= 0) throw new IllegalArgumentException("Mã thuốc không hợp lệ");
        validateInformation(name, price);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try
        {
            tx.begin();
            stockDB.requirePharmacist(em, userId);
            Medicine medicine = em.find(Medicine.class, id, LockModeType.PESSIMISTIC_WRITE);

            if (medicine == null)
            {
                throw new IllegalArgumentException("Không tìm thấy thuốc");
            }

            Long duplicate = em.createQuery(
                    "SELECT COUNT(x) FROM Medicine x "
                    + "WHERE LOWER(x.medicineName) = :name AND x.medicineId <> :id",
                    Long.class)
                    .setParameter("name", name.trim().toLowerCase(Locale.ROOT))
                    .setParameter("id", id)
                    .getSingleResult();

            if (duplicate > 0)
            {
                throw new IllegalArgumentException("Tên thuốc đã được sử dụng");
            }

            medicine.update(name, description, price);
            tx.commit();
        }
        catch (RuntimeException e)
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    private void validate(String name, BigDecimal price, int quantity, LocalDate date)
    {
        validateInformation(name, price);
        validateDateQuantity(quantity, date);
    }

    private void validateInformation(String name, BigDecimal price)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Tên thuốc không được để trống");
        }
        if (name.trim().length() > 255)
        {
            throw new IllegalArgumentException("Tên thuốc tối đa 255 ký tự");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new IllegalArgumentException("Giá bán/viên phải lớn hơn 0");
        }
        if (price.compareTo(new BigDecimal("9999999999999999.99")) > 0 || price.stripTrailingZeros().scale() > 2)
        {
            throw new IllegalArgumentException("Giá bán tối đa 16 chữ số nguyên và 2 chữ số thập phân");
        }
    }

    private void validateDateQuantity(int quantity, LocalDate date)
    {
        if (quantity < 1)
        {
            throw new IllegalArgumentException("Số viên nhập phải lớn hơn 0");
        }
        if (date == null)
        {
            throw new IllegalArgumentException("Ngày nhập không được để trống");
        }
        if (date.isAfter(LocalDate.now()))
        {
            throw new IllegalArgumentException("Ngày nhập không được ở tương lai");
        }
    }
}
