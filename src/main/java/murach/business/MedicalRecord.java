package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "medical_records")
public class MedicalRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        DRAFT, FINALIZED,
        // Tương thích tạm với giá trị đã được MedicalRecordDB cũ lưu
        @Deprecated COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    // Tương thích tạm: hồ sơ cũ chưa có lượt khám; tránh UNIQUE trên nhiều NULL ở SQL Server
    // appointment_id vẫn duy nhất; chuyển lại OneToOne khi đã chuyển hết dữ liệu cũ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private MedicalEncounter encounter;

    // Tương thích tạm: giữ field appointment để các JPQL r.appointment vẫn chạy
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Nationalized
    @Column(name = "diagnosis", columnDefinition = "NVARCHAR(MAX)")
    private String diagnosis;

    @Nationalized
    @Column(name = "clinical_note", columnDefinition = "NVARCHAR(MAX)")
    private String clinicalNote;

    @Nationalized
    @Column(name = "treatment", columnDefinition = "NVARCHAR(MAX)")
    private String treatment;

    @Nationalized
    @Column(name = "advice", columnDefinition = "NVARCHAR(MAX)")
    private String advice;

    // Tương thích tạm với form cũ; đơn thuốc mới vẫn lưu riêng trong Prescription
    @Nationalized
    @Column(name = "prescription", columnDefinition = "NVARCHAR(MAX)")
    private String prescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;

    protected MedicalRecord() {}

    // Kết luận chuyên môn gắn với lượt khám; đơn thuốc được lưu riêng
    public MedicalRecord(MedicalEncounter encounter)
    {
        if (encounter == null || encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalArgumentException("Lượt khám chưa bắt đầu");
        this.encounter = encounter;
        this.appointment = encounter.getAppointment();
        this.doctor = encounter.getDoctor();
        this.status = Status.DRAFT;
    }

    // Tương thích tạm với MedicalRecordDB cũ; xóa khi DAO đã chuyển sang MedicalEncounter
    @Deprecated
    public MedicalRecord(Appointment appointment, String diagnosis, String clinicalNote,
                         String treatment, String prescription, String status)
    {
        if (appointment == null)
            throw new IllegalArgumentException("Lịch hẹn không được để trống");
        this.appointment = appointment;
        this.doctor = appointment.getSchedule().getDoctor();
        update(diagnosis, clinicalNote, treatment, prescription, status);
    }

    // Tương thích tạm; không dùng API cũ để bỏ qua quy trình ký hồ sơ của lượt khám mới
    @Deprecated
    public void update(String diagnosis, String clinicalNote, String treatment,
                       String prescription, String status)
    {
        if (encounter != null)
            throw new IllegalStateException("Hồ sơ theo lượt khám phải cập nhật theo quy trình mới");
        String checkedDiagnosis = User.requireText(diagnosis, "Chẩn đoán không được để trống");
        String checkedStatus = User.requireText(status, "Trạng thái hồ sơ không được để trống");
        Status legacyStatus = Status.valueOf(checkedStatus.toUpperCase(Locale.ROOT));
        this.diagnosis = checkedDiagnosis;
        this.clinicalNote = normalize(clinicalNote);
        this.treatment = normalize(treatment);
        this.prescription = normalize(prescription);
        this.status = legacyStatus;
    }

    public void update(String diagnosis, String clinicalNote, String treatment, String advice)
    {
        if (encounter == null || status != Status.DRAFT)
            throw new IllegalStateException("Hồ sơ đã ký kết luận");
        this.diagnosis = normalize(diagnosis);
        this.clinicalNote = normalize(clinicalNote);
        this.treatment = normalize(treatment);
        this.advice = normalize(advice);
    }

    // pendingDiagnosticCount do DAO đếm trên mọi phiếu đã xác nhận, chưa hủy
    public void finalizeRecord(long pendingDiagnosticCount)
    {
        if (encounter == null || status != Status.DRAFT ||
                encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalStateException("Hồ sơ không ở bước xác nhận kết luận");
        if (pendingDiagnosticCount != 0)
            throw new IllegalStateException("Còn chỉ định chưa có kết quả phát hành");
        User.requireText(diagnosis, "Chẩn đoán không được để trống");
        this.finalizedAt = LocalDateTime.now();
        this.status = Status.FINALIZED;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter
    public Long getRecordId() { return recordId; }
    public MedicalEncounter getEncounter() { return encounter; }
    public Doctor getDoctor() { return doctor != null ? doctor : appointment.getSchedule().getDoctor(); }
    public String getDiagnosis() { return diagnosis; }
    public String getClinicalNote() { return clinicalNote; }
    public String getTreatment() { return treatment; }
    public String getAdvice() { return advice; }
    @Deprecated
    public String getPrescription() { return prescription; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public Long getVersion() { return version; }
    public Appointment getAppointment() { return appointment; }
}
