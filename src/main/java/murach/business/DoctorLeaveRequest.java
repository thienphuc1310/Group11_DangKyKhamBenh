package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "doctor_leave_requests")
public class DoctorLeaveRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    public enum Status { PENDING_APPROVAL, REJECTED, WAITING_REPLACEMENT, REPLACED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id")
    private Long leaveRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DoctorSchedule schedule;

    @Nationalized @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // SQL Server: dùng unique index có lọc IS NOT NULL trong migration.
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "replacement_schedule_id")
    private DoctorSchedule replacementSchedule;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Version @Column(name = "version", nullable = false)
    private Long version;

    protected DoctorLeaveRequest() {}

    public DoctorLeaveRequest(DoctorSchedule schedule, String reason)
    {
        if (schedule == null || schedule.getStatus() != DoctorSchedule.Status.ACTIVE)
            throw new IllegalArgumentException("Ca không nhận đơn nghỉ");
        this.reason = User.requireText(reason, "Nhập lý do nghỉ");
        if (this.reason.length() > 1000) throw new IllegalArgumentException("Lý do tối đa 1000 ký tự");
        this.schedule = schedule;
        this.status = Status.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }

    public void review(User admin, boolean approved)
    {
        if (status != Status.PENDING_APPROVAL || admin == null) throw new IllegalStateException("Đơn không còn chờ duyệt");
        reviewedBy = admin; reviewedAt = LocalDateTime.now();
        status = approved ? Status.WAITING_REPLACEMENT : Status.REJECTED;
        if (approved) schedule.approveLeave();
    }

    public void accept(DoctorSchedule replacement)
    {
        if (status != Status.WAITING_REPLACEMENT || replacement == null || replacementSchedule != null)
            throw new IllegalStateException("Ca đã có người nhận hoặc chưa được duyệt");
        if (schedule.getDoctor().getDoctorId().equals(replacement.getDoctor().getDoctorId()) ||
                !schedule.getDoctor().getSpecialty().getSpecialtyId().equals(replacement.getDoctor().getSpecialty().getSpecialtyId()) ||
                !schedule.getWorkDate().equals(replacement.getWorkDate()) ||
                !schedule.getStartTime().equals(replacement.getStartTime()) || !schedule.getEndTime().equals(replacement.getEndTime()))
            throw new IllegalArgumentException("Ca thay thế không phù hợp");
        replacementSchedule = replacement; acceptedAt = LocalDateTime.now();
        schedule.replace(); status = Status.REPLACED;
    }

    public Long getLeaveRequestId() { return leaveRequestId; }
    public DoctorSchedule getSchedule() { return schedule; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getReviewedBy() { return reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public DoctorSchedule getReplacementSchedule() { return replacementSchedule; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public Long getVersion() { return version; }
}
