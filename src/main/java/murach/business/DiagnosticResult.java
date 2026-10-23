package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "diagnostic_results")
public class DiagnosticResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        DRAFT, RELEASED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private DiagnosticOrderItem orderItem;

    @Nationalized
    @Column(name = "findings", columnDefinition = "NVARCHAR(MAX)")
    private String findings;

    @Nationalized
    @Column(name = "conclusion", columnDefinition = "NVARCHAR(MAX)")
    private String conclusion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_by_user_id")
    private User releasedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected DiagnosticResult() {}

    public DiagnosticResult(DiagnosticOrderItem orderItem, User recordedBy)
    {
        if (orderItem == null || recordedBy == null)
            throw new IllegalArgumentException("Chỉ định và người nhập không được để trống");
        this.orderItem = orderItem;
        this.recordedBy = recordedBy;
        this.status = Status.DRAFT;
        orderItem.attachResult(this);
    }

    public void update(String findings, String conclusion)
    {
        if (status != Status.DRAFT)
            throw new IllegalStateException("Kết quả đã phát hành");
        this.findings = normalize(findings);
        this.conclusion = normalize(conclusion);
    }

    public void release(User releasedBy)
    {
        if (status != Status.DRAFT || releasedBy == null)
            throw new IllegalStateException("Không thể phát hành kết quả");
        if (findings == null && conclusion == null)
            throw new IllegalArgumentException("Cần nhập nội dung kết quả hoặc kết luận");
        if (orderItem.getStatus() != DiagnosticOrderItem.Status.IN_PROGRESS)
            throw new IllegalStateException("Chỉ định không ở bước xử lý");
        this.releasedBy = releasedBy;
        this.releasedAt = LocalDateTime.now();
        this.status = Status.RELEASED;
        orderItem.complete(this);
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
    public Long getResultId() { return resultId; }
    public DiagnosticOrderItem getOrderItem() { return orderItem; }
    public String getFindings() { return findings; }
    public String getConclusion() { return conclusion; }
    public User getRecordedBy() { return recordedBy; }
    public User getReleasedBy() { return releasedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public Status getStatus() { return status; }
    public Long getVersion() { return version; }
}
