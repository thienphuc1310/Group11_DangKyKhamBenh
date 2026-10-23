package murach.business;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "doctors")
public class Doctor implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_code", nullable = false, unique = true)
    private String doctorCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "license_number")
    private String licenseNumber;

    @Nationalized
    @Column(name = "qualification", length = 255)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    protected Doctor() {}

    public Doctor(String doctorCode, User user, Specialty specialty, String licenseNumber,
                  String qualification, Integer experienceYears, String description)
    {
        this.doctorCode = User.requireText(doctorCode, "Mã bác sĩ không được để trống");
        assignUser(user);
        changeSpecialty(specialty);
        this.licenseNumber = normalize(licenseNumber);
        this.qualification = normalize(qualification);
        changeExperienceYears(experienceYears);
        this.description = normalize(description);
    }

    //bác sĩ tự cập nhật hồ sơ của mình
    public void updateProfileDoctor(String fullName, String phone, String email,
                                    java.time.LocalDate dateOfBirth, String gender, String address,
                                    Specialty specialty, String licenseNumber, String qualification,
                                    Integer experienceYears, String description)
    {
        user.updateProfile(fullName, phone, email, dateOfBirth, gender, address);
        changeSpecialty(specialty);
        this.licenseNumber = normalize(licenseNumber);
        this.qualification = normalize(qualification);
        changeExperienceYears(experienceYears);
        this.description = normalize(description);
    }

    private void assignUser(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("Tài khoản bác sĩ không được để trống");
        this.user = user;
    }

    public void changeSpecialty(Specialty specialty)
    {
        if (specialty == null)
            throw new IllegalArgumentException("Chuyên khoa không được để trống");
        this.specialty = specialty;
    }

    public void changeExperienceYears(Integer experienceYears)
    {
        if (experienceYears != null && experienceYears < 0)
            throw new IllegalArgumentException("Số năm kinh nghiệm không được âm");
        this.experienceYears = experienceYears;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public Long getDoctorId() { return doctorId; }
    public String getDoctorCode() { return doctorCode; }
    public User getUser() { return user; }
    public Specialty getSpecialty() { return specialty; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getQualification() { return qualification; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getDescription() { return description; }

    //cần thông tin Profile riêng cho bác sĩ nên phải get các thông tin từ bảng khác
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getGender() { return user.getGender(); }
    public String getEmail() { return user.getEmail(); }
    public String getPhone() { return user.getPhone(); }
}
