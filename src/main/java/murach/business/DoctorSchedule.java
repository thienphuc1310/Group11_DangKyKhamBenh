package murach.business;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "doctor_schedules")
public class DoctorSchedule implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Nationalized
    @Column(name = "room", nullable = false, length = 100)
    private String room;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status { ACTIVE, ON_LEAVE, REPLACED }

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.ACTIVE;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;

    public void approveLeave()
    {
        if (status != Status.ACTIVE) throw new IllegalStateException("Ca không còn hoạt động");
        status = Status.ON_LEAVE;
    }

    public void replace()
    {
        if (status != Status.ON_LEAVE) throw new IllegalStateException("Ca chưa được duyệt nghỉ");
        status = Status.REPLACED;
    }

    protected DoctorSchedule() {}

    public DoctorSchedule(Doctor doctor, LocalDate workDate, LocalTime startTime,
                          LocalTime endTime, String room)
    {
        assignDoctor(doctor);
        changeWorkDate(workDate);
        changeWorkingTime(startTime, endTime);
        changeRoom(room);
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    private void assignDoctor(Doctor doctor)
    {
        if (doctor == null)
            throw new IllegalArgumentException("Bác sĩ không được để trống");
        this.doctor = doctor;
    }

    public void changeWorkDate(LocalDate workDate)
    {
        if (workDate == null)
            throw new IllegalArgumentException("Ngày làm việc không được để trống");
        this.workDate = workDate;
    }

    public void changeWorkingTime(LocalTime startTime, LocalTime endTime)
    {
        if (startTime == null || endTime == null)
            throw new IllegalArgumentException("Giờ làm việc không được để trống");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void changeRoom(String room)
    {
        this.room = User.requireText(room, "Phòng khám không được để trống");
    }

    //getter
    public Long getScheduleId() { return scheduleId; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public Long getVersion() { return version; }
}
