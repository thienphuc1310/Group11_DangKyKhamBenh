package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "appointments")
public class Appointment implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "appointment_code", nullable = false, unique = true)
    private String appointmentCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DoctorSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private AppointmentSlot slot;

    @Nationalized
    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "fee", nullable = false)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Appointment() {}

    public Appointment(String appointmentCode, Patient patient, DoctorSchedule schedule,
                       String reason, BigDecimal fee, Status status)
    {
        this.appointmentCode = User.requireText(appointmentCode, "Mã lịch hẹn không được để trống");
        if (patient == null)
            throw new IllegalArgumentException("Bệnh nhân không được để trống");
        if (schedule == null)
            throw new IllegalArgumentException("Lịch bác sĩ không được để trống");
        if (fee == null)
            throw new IllegalArgumentException("Phí khám không được để trống");
        if (status == null)
            throw new IllegalArgumentException("Trạng thái lịch hẹn không được để trống");
        this.patient = patient;
        this.schedule = schedule;
        this.reason = normalize(reason);
        this.fee = fee;
        this.status = status;
    }

    public Appointment(String appointmentCode, Patient patient, AppointmentSlot slot,
                       String reason, BigDecimal fee, Status status)
    {
        this(appointmentCode, patient,
                slot == null ? null : slot.getSchedule(), reason, fee, status);
        this.slot = slot;
    }

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

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public void complete()
    {
        if (status == Status.CANCELLED)
            throw new IllegalStateException("Không thể hoàn thành lịch hẹn đã hủy");
        if (status == Status.COMPLETED)
            throw new IllegalStateException("Lịch hẹn đã được hoàn thành");
        this.status = Status.COMPLETED;
    }

    public Long getAppointmentId() { return appointmentId; }
    public String getAppointmentCode() { return appointmentCode; }
    public Patient getPatient() { return patient; }
    public DoctorSchedule getSchedule() { return schedule; }
    public AppointmentSlot getSlot() { return slot; }
    public String getReason() { return reason; }
    public BigDecimal getFee() { return fee; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void changeStatus(Status status)
    {
        if (status == null)
            throw new IllegalArgumentException(
                    "Trạng thái lịch hẹn không được để trống"
            );

        this.status = status;
    }

    public void transferTo(DoctorSchedule target, AppointmentSlot targetSlot)
    {
        if (status != Status.PENDING && status != Status.CONFIRMED)
            throw new IllegalStateException("Chỉ chuyển lịch hẹn chưa khám");
        if (target == null || !schedule.getWorkDate().equals(target.getWorkDate()) ||
                (slot != null && targetSlot == null) || (targetSlot != null && targetSlot.getSchedule() != target))
            throw new IllegalArgumentException("Lịch hoặc khung giờ thay thế không hợp lệ");
        schedule = target; slot = targetSlot;
    }
}
