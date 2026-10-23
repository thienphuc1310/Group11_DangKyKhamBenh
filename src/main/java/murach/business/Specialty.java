package murach.business;

import java.io.Serializable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Entity; //cung cấp annotation
import javax.persistence.Table; //chỉ  định tên table tương ứng trong database
import javax.persistence.Id; //đánh  dấu Id của 1 table làm khoá chính
import javax.persistence.GeneratedValue; //tự động sinh giá trị cho id mà không cần gán
import javax.persistence.GenerationType; //chứa các chiến lược của primary key, vd: IDENTITY: dành cho cột tự tăng trong sql server
import javax.persistence.Column; //mô tả cột tương ứng với thuộc tính có trong cột của bảng
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "specialties")
public class Specialty implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum SpecialtyType
    {
        CLINICAL, PARACLINICAL
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty_type", nullable = false, length = 30)
    private SpecialtyType specialtyType = SpecialtyType.CLINICAL;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specialty_id")
    private Long specialtyId;

    @Nationalized
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "status")
    private boolean status;

    protected Specialty() {}

    //constructor
    public Specialty(String name, String description, boolean status)
    {
        rename(name);
        this.description = normalize(description);
        this.status = status;
    }

    public void rename(String name)
    {
        this.name = User.requireText(name, "Tên chuyên khoa không được để trống");
    }

    public void update(String name, String description, boolean status)
    {
        rename(name);
        this.description = normalize(description);
        this.status = status;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Tạo chuyên khoa có phân loại rõ ràng
    public Specialty(String name, String description, boolean status, SpecialtyType specialtyType)
    {
        this(name, description, status);
        changeSpecialtyType(specialtyType);
    }

    public void changeSpecialtyType(SpecialtyType specialtyType)
    {
        if (specialtyType == null)
            throw new IllegalArgumentException("Loại chuyên khoa không được để trống");
        this.specialtyType = specialtyType;
    }

    public SpecialtyType getSpecialtyType() { return specialtyType; }

    //getter
    public Long getSpecialtyId() { return specialtyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isStatus() { return status; }
}
