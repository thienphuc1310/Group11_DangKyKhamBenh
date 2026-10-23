package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "medical_encounters")
public class MedicalEncounter implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        WAITING_ASSESSMENT, ASSESSING, WAITING_DOCTOR, IN_CONSULTATION,
        WAITING_RESULTS, READY_FOR_REVIEW, WAITING_CHECKOUT, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "encounter_id")
    private Long encounterId;

    @Column(name = "encounter_code", nullable = false, unique = true, length = 50)
    private String encounterCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "received_by_staff_id", nullable = false)
    private Staff receivedBy;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected MedicalEncounter() {}

    // Tạo lượt khám khi tiếp tân xác nhận bệnh nhân đã đến
    public MedicalEncounter(String encounterCode, Appointment appointment, Staff receivedBy)
    {
        this.encounterCode = User.requireText(encounterCode, "Mã lượt khám không được để trống");
        if (appointment == null || appointment.getStatus() != Appointment.Status.CONFIRMED)
            throw new IllegalArgumentException("Chỉ tiếp nhận lịch hẹn đã được xác nhận");
        if (receivedBy == null || receivedBy.getJobType() != Staff.JobType.RECEPTIONIST)
            throw new IllegalArgumentException("Người tiếp nhận phải là tiếp tân");
        this.appointment = appointment;
        this.doctor = appointment.getSchedule().getDoctor();
        this.specialty = doctor.getSpecialty();
        this.receivedBy = receivedBy;
        this.checkedInAt = LocalDateTime.now();
        this.status = Status.WAITING_ASSESSMENT;
    }

    public boolean sameAs(MedicalEncounter other)
    {
        return other != null && (this == other ||
                (encounterId != null && encounterId.equals(other.getEncounterId())));
    }

    public boolean belongsTo(Specialty other)
    {
        return other != null && (specialty == other ||
                (specialty.getSpecialtyId() != null &&
                 specialty.getSpecialtyId().equals(other.getSpecialtyId())));
    }

    public void beginAssessment()
    {
        requireStatus(Status.WAITING_ASSESSMENT);
        this.status = Status.ASSESSING;
    }

    public void transferTo(Doctor replacement)
    {
        if (startedAt != null || (status != Status.WAITING_ASSESSMENT && status != Status.ASSESSING && status != Status.WAITING_DOCTOR))
            throw new IllegalStateException("Lượt đã bắt đầu khám, cần bàn giao riêng");
        if (replacement == null || !belongsTo(replacement.getSpecialty())) throw new IllegalArgumentException("Bác sĩ thay phải cùng khoa");
        doctor = replacement;
    }

    public void finishAssessment(InitialAssessment assessment)
    {
        requireStatus(Status.ASSESSING);
        if (assessment == null || !sameAs(assessment.getEncounter()) || !assessment.isCompleted())
            throw new IllegalStateException("Đánh giá ban đầu chưa hoàn thành");
        this.status = Status.WAITING_DOCTOR;
    }

    public void startConsultation(InitialAssessment assessment)
    {
        if (status != Status.WAITING_DOCTOR && status != Status.READY_FOR_REVIEW)
            throw new IllegalStateException("Lượt khám chưa sẵn sàng cho bác sĩ");
        if (assessment == null || !sameAs(assessment.getEncounter()) || !assessment.isCompleted())
            throw new IllegalStateException("Chưa hoàn thành đánh giá ban đầu");
        if (startedAt == null)
            this.startedAt = LocalDateTime.now();
        this.status = Status.IN_CONSULTATION;
    }

    public void waitForResults()
    {
        requireStatus(Status.IN_CONSULTATION);
        this.status = Status.WAITING_RESULTS;
    }

    // Hai số đếm phải được DAO tính từ chỉ định còn hiệu lực trong cùng transaction
    public void markResultsReady(long requiredCount, long releasedCount)
    {
        requireStatus(Status.WAITING_RESULTS);
        if (requiredCount < 1 || requiredCount != releasedCount)
            throw new IllegalStateException("Chưa đủ kết quả cận lâm sàng");
        this.status = Status.READY_FOR_REVIEW;
    }

    public void finishConsultation()
    {
        requireStatus(Status.IN_CONSULTATION);
        this.status = Status.WAITING_CHECKOUT;
    }

    // DAO kiểm tra kết luận, công nợ và cấp thuốc trước khi gọi
    public void complete()
    {
        requireStatus(Status.WAITING_CHECKOUT);
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel()
    {
        if (status == Status.COMPLETED || status == Status.CANCELLED)
            throw new IllegalStateException("Lượt khám đã kết thúc");
        this.status = Status.CANCELLED;
    }

    private void requireStatus(Status expected)
    {
        if (status != expected)
            throw new IllegalStateException("Trạng thái lượt khám không phù hợp");
    }

    // Getter
    public Long getEncounterId() { return encounterId; }
    public String getEncounterCode() { return encounterCode; }
    public Appointment getAppointment() { return appointment; }
    public Doctor getDoctor() { return doctor; }
    public Specialty getSpecialty() { return specialty; }
    public Staff getReceivedBy() { return receivedBy; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Status getStatus() { return status; }
    public Long getVersion() { return version; }
    public Patient getPatient() { return appointment.getPatient(); }
}
