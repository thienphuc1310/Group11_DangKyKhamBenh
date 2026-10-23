//User, UserDB, TheEmailServlet

package murach.business;

//implements Serializable chỉ nên được dùng khi muốn
//đóng gói và chuyển sang nơi khác
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Entity; //cung cấp annotation
import javax.persistence.Table; //chỉ định tên table tương ứng trong database
import javax.persistence.Id; //đánh dấu Id của 1 table làm khoá chính
import javax.persistence.GeneratedValue; //tự động sinh giá trị cho id mà không cần gán
import javax.persistence.GenerationType; //chứa các chiến lược của primary key, vd: IDENTITY: dành cho cột tự tăng trong sql server
import javax.persistence.Column; //mô tả cột tương ứng với thuộc tính có trong cột của bảng
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.hibernate.annotations.Nationalized;

//chuyển User thành entity
@Entity
@Table(name = "users")
public class User implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Nationalized
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Nationalized
    @Column(name = "gender", length = 10)
    private String gender;

    @Nationalized
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected User() {}

    public User(String fullName, String phone, String email, LocalDate dateOfBirth,
                String gender, String address)
    {
        updateProfile(fullName, phone, email, dateOfBirth, gender, address);
    }

    //Tự động hoá thời gian qua JPA
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

    static String requireText(String value, String errorMessage)
    {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(errorMessage);
        return value.trim();
    }

    public void updateProfile(String fullName, String phone, String email,
                              LocalDate dateOfBirth, String gender, String address)
    {
        this.fullName = requireText(fullName, "Họ tên không được để trống");
        this.phone = normalize(phone);
        this.email = normalize(email);
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai");
        this.dateOfBirth = dateOfBirth;
        this.gender = normalize(gender);
        this.address = normalize(address);
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
