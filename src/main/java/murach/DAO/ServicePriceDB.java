package murach.DAO;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import murach.business.ServicePrice;
import murach.business.User;
import murach.util.JPAUtil;

public class ServicePriceDB
{
    //lấy giá dịch vụ đang vẫn dc sử dụng
    public ServicePrice findActiveByCode(String serviceCode)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "LEFT JOIN FETCH sp.updatedBy " +
                    "WHERE sp.serviceCode = :serviceCode " +
                    "AND sp.status = true";

            TypedQuery<ServicePrice> query = em.createQuery(jpql, ServicePrice.class);
            query.setParameter("serviceCode", serviceCode);

            List<ServicePrice> prices = query.getResultList();

            return prices.isEmpty() ? null : prices.get(0);
        }
        finally
        {
            em.close();
        }
    }

    //lấy giá dịch vụ theo mã bao gồm cả dịch vụ đang off
    public ServicePrice findByCode(String serviceCode)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "LEFT JOIN FETCH sp.updatedBy " +
                    "WHERE sp.serviceCode = :serviceCode";

            TypedQuery<ServicePrice> query = em.createQuery(jpql, ServicePrice.class);
            query.setParameter("serviceCode", serviceCode);

            List<ServicePrice> prices = query.getResultList();

            return prices.isEmpty() ? null : prices.get(0);
        }
        finally
        {
            em.close();
        }
    }

    //lấy giá dịch vụ theo khóa chính
    public ServicePrice findById(Long priceId)
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            //hàm sẵn có khỏi phải viết query
            return em.find(ServicePrice.class, priceId);
        }
        finally
        {
            em.close();
        }
    }

    //lấy toàn bộ bảng giá để hiển thị
    public List<ServicePrice> getAllServicePrices()
    {
        EntityManager em = JPAUtil.getEntityManager();

        try
        {
            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "LEFT JOIN FETCH sp.updatedBy " +
                    "ORDER BY sp.priceId";

            TypedQuery<ServicePrice> query = em.createQuery(jpql, ServicePrice.class);

            return query.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    // Thêm dịch vụ mới kèm giá
    public void insert(ServicePrice servicePrice)
    {
        if (servicePrice == null)
            throw new IllegalArgumentException("Giá dịch vụ không được để trống");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();
            em.persist(servicePrice);
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    // Thêm dịch vụ mới kèm giá và người tạo
    public void insert(String serviceCode, String serviceName, BigDecimal amount, Long updatedByUserId)
    {
        if (serviceCode == null || serviceCode.trim().isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống");

        if (updatedByUserId == null)
            throw new IllegalArgumentException("Người cập nhật không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            List<ServicePrice> existingPrices = em.createQuery(
                    "SELECT sp FROM ServicePrice sp WHERE sp.serviceCode = :serviceCode",
                    ServicePrice.class)
                    .setParameter("serviceCode", serviceCode.trim())
                    .getResultList();

            if (!existingPrices.isEmpty())
                throw new IllegalArgumentException("Mã dịch vụ đã tồn tại");

            User updatedBy = em.find(User.class, updatedByUserId);

            if (updatedBy == null)
                throw new IllegalArgumentException("Người cập nhật không tồn tại");

            ServicePrice servicePrice = new ServicePrice(
                    serviceCode.trim(), serviceName, amount, true, updatedBy);

            em.persist(servicePrice);
            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    //cập nhật giá dịch vụ
    public void updatePrice(String serviceCode, BigDecimal amount, Long updatedByUserId)
    {
        if (serviceCode == null || serviceCode.trim().isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống");

        if (updatedByUserId == null)
            throw new IllegalArgumentException("Người cập nhật không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "WHERE sp.serviceCode = :serviceCode";

            List<ServicePrice> prices = em.createQuery(jpql, ServicePrice.class)
                    .setParameter("serviceCode", serviceCode.trim())
                    .getResultList();

            if (prices.isEmpty())
                throw new IllegalArgumentException("Dịch vụ không tồn tại");

            User updatedBy = em.find(User.class, updatedByUserId);

            if (updatedBy == null)
                throw new IllegalArgumentException("Người cập nhật không tồn tại");

            ServicePrice servicePrice = prices.get(0);
            servicePrice.changePrice(amount, updatedBy);

            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    //thay đổi tên hiển thị của dịch vụ
    public void rename(String serviceCode, String serviceName, Long updatedByUserId)
    {
        if (serviceCode == null || serviceCode.trim().isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống");

        if (updatedByUserId == null)
            throw new IllegalArgumentException("Người cập nhật không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "WHERE sp.serviceCode = :serviceCode";

            List<ServicePrice> prices = em.createQuery(jpql, ServicePrice.class)
                    .setParameter("serviceCode", serviceCode.trim())
                    .getResultList();

            if (prices.isEmpty())
                throw new IllegalArgumentException("Dịch vụ không tồn tại");

            User updatedBy = em.find(User.class, updatedByUserId);

            if (updatedBy == null)
                throw new IllegalArgumentException("Người cập nhật không tồn tại");

            ServicePrice servicePrice = prices.get(0);
            servicePrice.rename(serviceName, updatedBy);

            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    //bật áp dụng giá dịch vụ
    public void activate(String serviceCode, Long updatedByUserId)
    {
        changeStatus(serviceCode, true, updatedByUserId);
    }

    //ngừng áp dụng giá dịch vụ
    public void deactivate(String serviceCode, Long updatedByUserId)
    {
        changeStatus(serviceCode, false, updatedByUserId);
    }

    //dùng chung để thay đổi trạng thái dịch vụ
    private void changeStatus(String serviceCode, boolean active, Long updatedByUserId)
    {
        if (serviceCode == null || serviceCode.trim().isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống");

        if (updatedByUserId == null)
            throw new IllegalArgumentException("Người cập nhật không hợp lệ");

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try
        {
            transaction.begin();

            String jpql = "SELECT sp FROM ServicePrice sp " +
                    "WHERE sp.serviceCode = :serviceCode";

            List<ServicePrice> prices = em.createQuery(jpql, ServicePrice.class)
                    .setParameter("serviceCode", serviceCode.trim())
                    .getResultList();

            if (prices.isEmpty())
                throw new IllegalArgumentException("Dịch vụ không tồn tại");

            User updatedBy = em.find(User.class, updatedByUserId);

            if (updatedBy == null)
                throw new IllegalArgumentException("Người cập nhật không tồn tại");

            ServicePrice servicePrice = prices.get(0);

            if (active)
                servicePrice.activate(updatedBy);
            else
                servicePrice.deactivate(updatedBy);

            transaction.commit();
        }
        catch (RuntimeException e)
        {
            if (transaction.isActive())
                transaction.rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }
}
