package murach.business;

//implements Serializable chỉ nên được dùng khi muốn
//đóng gói và chuyển sang nơi khác
import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "patients")
public class Patient implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_code", nullable = false, unique = true)
    private String patientCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    protected Patient() {}

    public Patient(String patientCode, User user)
    {
        this.patientCode = User.requireText(patientCode, "Mã bệnh nhân không được để trống");
        assignUser(user);
    }

    private void assignUser(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Tài khoản bệnh nhân không được để trống");
        this.user = user;
    }

    public void updatePatientProfile(String fullName, String phone, String email,
                                     java.time.LocalDate dateOfBirth, String gender, String address)
    {
        user.updateProfile(fullName, phone, email, dateOfBirth, gender, address);
    }

    //getter trong
    public Long getPatientId() { return patientId; }
    public String getPatientCode() { return patientCode; }
    public User getUser() { return user; }

    //getter ngoài
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getGender() { return user.getGender(); }
    public java.time.LocalDate getDateOfBirth() { return user.getDateOfBirth(); }
    public String getEmail() { return user.getEmail(); }
    public String getPhone() { return user.getPhone(); }
    public String getAddress() { return user.getAddress(); }
}
