package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "accounts")
public class Account implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Role
    {
        PATIENT, DOCTOR, STAFF, ADMIN, NURSE, TECHNICIAN, PHARMACIST
    }

    public enum Status
    {
        ACTIVE, INACTIVE, LOCKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Nationalized
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    protected Account() {}

    public Account(User user, String username, String passwordHash, Role role, Status status)
    {
        assignUser(user);
        this.username = User.requireText(username, "Tên đăng nhập không được để trống");
        this.passwordHash = User.requireText(passwordHash, "Mật khẩu mã hoá không được để trống");
        if (role == null)
            throw new IllegalArgumentException("Vai trò không được để trống");
        if (status == null)
            throw new IllegalArgumentException("Trạng thái tài khoản không được để trống");
        this.role = role;
        this.status = status;
    }

    private void assignUser(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Người dùng không được để trống");
        this.user = user;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    public void changePasswordHash(String passwordHash)
    {
        this.passwordHash = User.requireText(passwordHash, "Mật khẩu mã hoá không được để trống");
    }

    public void changeStatus(Status status)
    {
        if (status == null)
            throw new IllegalArgumentException("Trạng thái tài khoản không được để trống");
        this.status = status;
    }

    public void recordLogin()
    {
        this.lastLoginAt = LocalDateTime.now();
    }

    public Long getAccountId() { return accountId; }
    public User getUser() { return user; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}
