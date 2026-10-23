package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "nurses")
public class Nurse implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum ProfessionType
    {
        NURSE, MIDWIFE
    }

    //nurse_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nurse_id")
    private Long nurseId;

    //nurse_code
    @Column(name = "nurse_code", nullable = false, unique = true, length = 50)
    private String nurseCode;

    //user_id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "profession_type", nullable = false, length = 40)
    private ProfessionType professionType;

    //constructor
    protected Nurse() {}

    // Điều dưỡng và hộ sinh đánh giá ban đầu dùng chung, không phân chuyên khoa
    public Nurse(String nurseCode, User user, ProfessionType professionType)
    {
        this.nurseCode = User.requireText(nurseCode, "Mã điều dưỡng không được để trống");
        if (user == null)
            throw new IllegalArgumentException("Thông tin người dùng không được để trống");
        this.user = user;
        changeProfessionType(professionType);
    }

    public void changeProfessionType(ProfessionType professionType)
    {
        if (professionType == null)
            throw new IllegalArgumentException("Nhóm nghề nghiệp không được để trống");
        this.professionType = professionType;
    }

    // Getter
    public Long getNurseId() { return nurseId; }
    public String getNurseCode() { return nurseCode; }
    public User getUser() { return user; }
    public ProfessionType getProfessionType() { return professionType; }
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getPhone() { return user.getPhone(); }
    public String getEmail() { return user.getEmail(); }
}
