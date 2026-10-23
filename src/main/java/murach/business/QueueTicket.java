package murach.business;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "queue_tickets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"queue_date", "doctor_id", "queue_number"}))
public class QueueTicket implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        WAITING, CALLED, ON_HOLD, FINISHED, SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false, unique = true)
    private MedicalEncounter encounter;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "queue_number", nullable = false)
    private int queueNumber;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected QueueTicket() {}

    // DAO cấp số bằng khóa/transaction; không dùng MAX + 1 mà không khóa
    public QueueTicket(MedicalEncounter encounter, int queueNumber)
    {
        if (encounter == null)
            throw new IllegalArgumentException("Lượt khám không được để trống");
        if (queueNumber < 1)
            throw new IllegalArgumentException("Số thứ tự phải lớn hơn 0");
        this.encounter = encounter;
        this.doctor = encounter.getDoctor();
        this.queueDate = encounter.getCheckedInAt().toLocalDate();
        this.queueNumber = queueNumber;
        this.issuedAt = LocalDateTime.now();
        this.status = Status.WAITING;
    }

    public void call()
    {
        if (status != Status.WAITING && status != Status.ON_HOLD)
            throw new IllegalStateException("Phiếu không ở hàng đợi");
        if (encounter.getStatus() != MedicalEncounter.Status.WAITING_DOCTOR &&
                encounter.getStatus() != MedicalEncounter.Status.READY_FOR_REVIEW)
            throw new IllegalStateException("Bệnh nhân chưa đủ điều kiện gọi khám");
        this.status = Status.CALLED;
        this.calledAt = LocalDateTime.now();
    }

    public void transferTo(Doctor replacement, int number)
    {
        if (status != Status.WAITING || replacement == null || number < 1 || encounter.getStartedAt() != null)
            throw new IllegalStateException("Số thứ tự không đủ điều kiện chuyển");
        doctor = replacement; queueNumber = number;
    }

    public void putOnHold()
    {
        if (status != Status.CALLED)
            throw new IllegalStateException("Chỉ tạm dừng phiếu đang được gọi");
        this.status = Status.ON_HOLD;
    }

    public void finish()
    {
        if (status != Status.CALLED)
            throw new IllegalStateException("Phiếu chưa được gọi");
        this.status = Status.FINISHED;
    }

    public void skip()
    {
        if (status == Status.FINISHED)
            throw new IllegalStateException("Phiếu đã hoàn tất");
        this.status = Status.SKIPPED;
    }

    // Getter
    public Long getTicketId() { return ticketId; }
    public MedicalEncounter getEncounter() { return encounter; }
    public LocalDate getQueueDate() { return queueDate; }
    public Doctor getDoctor() { return doctor; }
    public int getQueueNumber() { return queueNumber; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getCalledAt() { return calledAt; }
    public Status getStatus() { return status; }
    public Long getVersion() { return version; }
}
