package murach.business;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "technician_schedules")
public class TechnicianSchedule implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Nationalized
    @Column(name = "room", nullable = false, length = 100)
    private String room;

    protected TechnicianSchedule() {}

    public TechnicianSchedule(Technician technician, LocalDate workDate, LocalTime startTime, LocalTime endTime, String room)
    {
        if (technician == null || workDate == null || startTime == null || endTime == null)
            throw new IllegalArgumentException("Thiếu chuyên viên, ngày hoặc giờ làm việc");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        this.technician = technician;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = User.requireText(room, "Phòng làm việc không được để trống");
        if (this.room.length() > 100) throw new IllegalArgumentException("Phòng làm việc tối đa 100 ký tự");
    }

    public Long getScheduleId() { return scheduleId; }
    public Technician getTechnician() { return technician; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public String getFullName() { return technician.getFullName(); }
}
