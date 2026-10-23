package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.*;

@Entity
@Table(name = "appointment_slots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "start_time"}))
public class AppointmentSlot implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DoctorSchedule schedule;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_patients", nullable = false)
    private int maxPatients;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected AppointmentSlot() {}

    public AppointmentSlot(DoctorSchedule schedule, LocalTime startTime, LocalTime endTime, int maxPatients)
    {
        if (schedule == null) throw new IllegalArgumentException("Ca làm việc không được để trống");
        if (startTime == null || endTime == null || !endTime.isAfter(startTime))
            throw new IllegalArgumentException("Khung giờ không hợp lệ");
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
        changeMaxPatients(maxPatients);
        this.status = Status.OPEN;
    }

    public void changeMaxPatients(int maxPatients)
    {
        if (maxPatients < 1)
            throw new IllegalArgumentException("Số bệnh nhân tối đa phải lớn hơn 0");
        this.maxPatients = maxPatients;
    }

    public void changeStatus(Status status)
    {
        if (status == null) throw new IllegalArgumentException("Trạng thái không được để trống");
        this.status = status;
    }

    @PrePersist
    protected void onCreate()
    {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getSlotId() { return slotId; }
    public DoctorSchedule getSchedule() { return schedule; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getMaxPatients() { return maxPatients; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
