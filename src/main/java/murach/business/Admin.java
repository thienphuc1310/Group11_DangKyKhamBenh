package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "admins")
public class Admin implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    protected Admin() {}

    public Admin(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Người dùng quản trị không được để trống");
        this.user = user;
    }

    public Long getAdminId() { return adminId; }
    public User getUser() { return user; }
    public Long getUserId() { return user.getUserId(); }
}
