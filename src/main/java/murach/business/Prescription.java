package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "prescriptions")
public class Prescription implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        DRAFT, CONFIRMED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "prescription_code", nullable = false, unique = true, length = 50)
    private String prescriptionCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false, unique = true)
    private MedicalEncounter encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Nationalized
    @Column(name = "advice", columnDefinition = "NVARCHAR(MAX)")
    private String advice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.PERSIST)
    private List<PrescriptionItem> items = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Prescription() {}

    public Prescription(String prescriptionCode, MedicalEncounter encounter, String advice)
    {
        this.prescriptionCode = User.requireText(prescriptionCode, "Mã đơn thuốc không được để trống");
        if (encounter == null || encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalArgumentException("Lượt khám không ở bước bác sĩ thăm khám");
        this.encounter = encounter;
        this.doctor = encounter.getDoctor();
        this.advice = normalize(advice);
        this.status = Status.DRAFT;
    }

    public boolean sameAs(Prescription other)
    {
        return other != null && (this == other ||
                (prescriptionId != null && prescriptionId.equals(other.getPrescriptionId())));
    }

    public PrescriptionItem addMedicine(Medicine medicine, int quantity, String dosage, String frequency,
                                        String route, int durationDays, String instruction)
    {
        if (status != Status.DRAFT)
            throw new IllegalStateException("Chỉ được sửa đơn thuốc nháp");
        if (medicine == null)
            throw new IllegalArgumentException("Thuốc không được để trống");
        for (PrescriptionItem item : items)
            if (item.getMedicine() == medicine || (medicine.getMedicineId() != null &&
                    medicine.getMedicineId().equals(item.getMedicine().getMedicineId())))
                throw new IllegalArgumentException("Thuốc đã có trong đơn");
        PrescriptionItem item = new PrescriptionItem(this, medicine, quantity,
                dosage, frequency, route, durationDays, instruction);
        items.add(item);
        return item;
    }

    // DAO kiểm tra và giữ tồn kho cho từng dòng trong cùng transaction
    public void confirm()
    {
        if (status != Status.DRAFT || items.isEmpty())
            throw new IllegalStateException("Đơn phải ở dạng nháp và có thuốc");
        if (encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalStateException("Lượt khám không ở bước thăm khám");
        for (PrescriptionItem item : items)
            if (!item.getMedicine().isStatus())
                throw new IllegalStateException("Có thuốc đã ngừng sử dụng");
        this.status = Status.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // DAO xóa các dòng nháp cũ trong cùng transaction trước khi thêm nội dung mới
    public void clearDraftItems(String advice)
    {
        if (status != Status.DRAFT) throw new IllegalStateException("Không sửa đơn đã xác nhận");
        items.clear();
        this.advice = normalize(advice);
    }

    // DAO giải phóng lượng giữ, hủy hóa đơn phù hợp; không hủy đơn đã cấp thuốc
    public void cancel()
    {
        if (status == Status.CANCELLED)
            throw new IllegalStateException("Đơn đã bị hủy");
        this.status = Status.CANCELLED;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    // Getter
    public Long getPrescriptionId() { return prescriptionId; }
    public String getPrescriptionCode() { return prescriptionCode; }
    public MedicalEncounter getEncounter() { return encounter; }
    public Doctor getDoctor() { return doctor; }
    public String getAdvice() { return advice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public Status getStatus() { return status; }
    public List<PrescriptionItem> getItems() { return Collections.unmodifiableList(items); }
    public Long getVersion() { return version; }
}
