package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "pharmacists")
public class Pharmacist implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacist_id")
    private Long pharmacistId;

    @Column(name = "pharmacist_code", nullable = false, unique = true, length = 50)
    private String pharmacistCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    protected Pharmacist() {}

    public Pharmacist(String pharmacistCode, User user)
    {
        this.pharmacistCode = User.requireText(pharmacistCode, "Mã dược sĩ không được để trống");
        if (user == null)
            throw new IllegalArgumentException("Thông tin người dùng không được để trống");
        this.user = user;
    }

    // Getter
    public Long getPharmacistId() { return pharmacistId; }
    public String getPharmacistCode() { return pharmacistCode; }
    public User getUser() { return user; }
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getPhone() { return user.getPhone(); }
    public String getEmail() { return user.getEmail(); }
}
