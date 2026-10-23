package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "specimens")
public class Specimen implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        NEW, COLLECTED, RECEIVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specimen_id")
    private Long specimenId;

    @Column(name = "specimen_code", nullable = false, unique = true, length = 80)
    private String specimenCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DiagnosticOrder order;

    @Nationalized
    @Column(name = "specimen_type", nullable = false, length = 100)
    private String specimenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by_user_id")
    private User collectedBy;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Nationalized
    @Column(name = "note", length = 1000)
    private String note;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Specimen() {}

    public Specimen(String specimenCode, DiagnosticOrder order, String specimenType)
    {
        this.specimenCode = User.requireText(specimenCode, "Mã mẫu không được để trống");
        if (order == null || order.getStatus() != DiagnosticOrder.Status.CONFIRMED)
            throw new IllegalArgumentException("Phiếu chỉ định chưa được xác nhận");
        this.order = order;
        this.specimenType = User.requireText(specimenType, "Loại mẫu không được để trống");
        this.status = Status.NEW;
    }

    public void collect(User collectedBy)
    {
        if (status != Status.NEW || collectedBy == null)
            throw new IllegalStateException("Không thể ghi nhận lấy mẫu");
        this.collectedBy = collectedBy;
        this.collectedAt = LocalDateTime.now();
        this.status = Status.COLLECTED;
    }

    public void receive()
    {
        if (status != Status.COLLECTED)
            throw new IllegalStateException("Mẫu chưa được lấy");
        this.receivedAt = LocalDateTime.now();
        this.status = Status.RECEIVED;
    }

    public void reject(String reason)
    {
        if (status == Status.REJECTED)
            throw new IllegalStateException("Mẫu đã bị từ chối");
        this.note = User.requireText(reason, "Cần ghi lý do từ chối mẫu");
        this.status = Status.REJECTED;
    }

    // Getter
    public Long getSpecimenId() { return specimenId; }
    public String getSpecimenCode() { return specimenCode; }
    public DiagnosticOrder getOrder() { return order; }
    public String getSpecimenType() { return specimenType; }
    public User getCollectedBy() { return collectedBy; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public Status getStatus() { return status; }
    public String getNote() { return note; }
    public Long getVersion() { return version; }
}
