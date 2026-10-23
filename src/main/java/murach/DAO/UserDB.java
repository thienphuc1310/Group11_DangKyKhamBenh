package murach.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import murach.business.User;
import murach.business.Account;
import murach.util.JPAUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDB {

    // Toan: Tìm tài khoản nhân viên/quản trị viên theo từ khóa, vai trò và trạng thái.
    public List<Account> searchStaff(String keyword, Account.Role role, Boolean active) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT a FROM Account a JOIN FETCH a.user WHERE a.role IN (:roles)");
            if (active != null)
                jpql.append(active ? " AND a.status = :active" : " AND a.status <> :active");
            jpql.append(" ORDER BY a.accountId");
            TypedQuery<Account> query = em.createQuery(jpql.toString(), Account.class)
                    .setParameter("roles", role != null
                            ? java.util.Collections.singletonList(role)
                            : java.util.Arrays.asList(Account.Role.STAFF, Account.Role.ADMIN));
            if (active != null) query.setParameter("active", Account.Status.ACTIVE);
            return query.getResultList().stream()
                    .filter(a -> DoctorDB.matches(keyword, a.getAccountId(),
                            a.getUsername(), a.getUser().getFullName()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // Toan: Lập bảng userId và trạng thái tài khoản để hiển thị nút khóa/mở khóa.
    public Map<Long, Account.Status> getStatusMap(Account.Role role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                            "SELECT a.user.userId, a.status FROM Account a WHERE a.role = :role",
                            Object[].class)
                    .setParameter("role", role)
                    .getResultList();
            Map<Long, Account.Status> result = new HashMap<>();
            for (Object[] row : rows) result.put((Long) row[0], (Account.Status) row[1]);
            return result;
        } finally {
            em.close();
        }
    }

    // Toan: Lấy tài khoản dựa trên userId để hiển thị trang chi tiết.
    public Account findAccountByUserId(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Account> accounts = em.createQuery(
                            "SELECT a FROM Account a JOIN FETCH a.user WHERE a.user.userId = :userId",
                            Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
            return accounts.isEmpty() ? null : accounts.get(0);
        } finally {
            em.close();
        }
    }

    // Toan: Đảo trạng thái tài khoản giữa ACTIVE và LOCKED.
    public void toggleLock(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Account account = em.createQuery(
                            "SELECT a FROM Account a WHERE a.user.userId = :userId", Account.class)
                    .setParameter("userId", userId)
                    .getResultStream().findFirst().orElse(null);
            if (account == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
            account.changeStatus(account.getStatus() == Account.Status.ACTIVE
                    ? Account.Status.LOCKED : Account.Status.ACTIVE);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Account> getAccountsByRole(Account.Role role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Account a JOIN FETCH a.user WHERE a.role = :role",
                    Account.class)
                    .setParameter("role", role)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Account getAccountByUsername(String username) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Account a JOIN FETCH a.user WHERE a.username = :username",
                    Account.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void insertAccount(User user, Account account, Object profile) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(user);
            em.persist(account);
            em.persist(profile);
            trans.commit();
        } catch (RuntimeException e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Lấy danh sách Người dùng/Nhân viên theo Vai trò
    public List<User> getUsersByRole(String role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT a.user FROM Account a WHERE a.role = :role";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("role", Account.Role.valueOf(role));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Tìm User theo Tên đăng nhập
    public User getUserByUsername(String username) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT a.user FROM Account a WHERE a.username = :username";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    // Thêm User mới
    public void insert(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(user);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
