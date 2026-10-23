package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "technicians")
public class Technician implements Serializable
{
    private static final long serialVersionUID = 1L;

    //technician_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technician_id")
    private Long technicianId;

    //technician_code
    @Column(name = "technician_code", nullable = false, unique = true, length = 50)
    private String technicianCode;

    //user_id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    //constructor
    protected Technician() {}

    public Technician(String technicianCode, User user, Specialty specialty)
    {
        this.technicianCode = User.requireText(technicianCode, "Mã kỹ thuật viên không được để trống");
        if (user == null)
            throw new IllegalArgumentException("Thông tin người dùng không được để trống");
        this.user = user;
        assignWork(specialty);
    }

    public void assignWork(Specialty specialty)
    {
        if (specialty == null || specialty.getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
            throw new IllegalArgumentException("Kỹ thuật viên phải thuộc khoa cận lâm sàng");
        this.specialty = specialty;
    }

    // Kiểm tra cả ở backend khi nhận chỉ định, không chỉ lọc trên giao diện.
    public boolean canPerform(DiagnosticService service)
    {
        if (service == null || service.getServiceType() != DiagnosticService.ServiceType.LABORATORY
                || specialty.getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL
                || !sameSpecialty(specialty, service.getSpecialty())) return false;

        return true;
    }

    private static boolean sameSpecialty(Specialty first, Specialty second)
    {
        return first != null && second != null && (first == second ||
                (first.getSpecialtyId() != null && first.getSpecialtyId().equals(second.getSpecialtyId())));
    }

    // Getter
    public Long getTechnicianId() { return technicianId; }
    public String getTechnicianCode() { return technicianCode; }
    public User getUser() { return user; }
    public Specialty getSpecialty() { return specialty; }
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getPhone() { return user.getPhone(); }
    public String getEmail() { return user.getEmail(); }
}
