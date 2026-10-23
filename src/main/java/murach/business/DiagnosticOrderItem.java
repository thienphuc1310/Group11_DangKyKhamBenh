package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "diagnostic_order_items")
public class DiagnosticOrderItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        DRAFT, WAITING_PAYMENT, WAITING_ACCEPTANCE, ACCEPTED,
        SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DiagnosticOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private DiagnosticService service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performing_specialty_id", nullable = false)
    private Specialty performingSpecialty;

    @Nationalized
    @Column(name = "service_name_snapshot", nullable = false, length = 255)
    private String serviceNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type_snapshot", nullable = false, length = 40)
    private DiagnosticService.ServiceType serviceTypeSnapshot;

    @Column(name = "requires_specimen", nullable = false)
    private boolean requiresSpecimen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private Technician assignedTechnician;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private int estimatedDurationMinutes;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expected_result_at")
    private LocalDateTime expectedResultAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private DiagnosticResult result;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected DiagnosticOrderItem() {}

    // Tạo thông qua DiagnosticOrder.addService()
    protected DiagnosticOrderItem(DiagnosticOrder order, DiagnosticService service)
    {
        if (order == null || service == null || !service.isStatus() || !service.getPrice().isStatus())
            throw new IllegalArgumentException("Dịch vụ chưa sẵn sàng");
        this.order = order;
        this.service = service;
        this.performingSpecialty = service.getSpecialty();
        this.serviceNameSnapshot = service.getServiceName();
        this.serviceTypeSnapshot = service.getServiceType();
        this.requiresSpecimen = service.isRequiresSpecimen();
        this.estimatedDurationMinutes = service.getEstimatedDurationMinutes();
        this.status = Status.DRAFT;
    }

    void submit()
    {
        requireStatus(Status.DRAFT);
        this.status = Status.WAITING_PAYMENT;
    }

    // Chỉ gọi sau khi DAO đã xác minh hóa đơn của chỉ định được thanh toán
    public void markPaid()
    {
        requireStatus(Status.WAITING_PAYMENT);
        this.status = Status.WAITING_ACCEPTANCE;
    }

    public void assignDoctor(Doctor doctor)
    {
        requireStatus(Status.WAITING_ACCEPTANCE);
        if (serviceTypeSnapshot != DiagnosticService.ServiceType.IMAGING || doctor == null ||
                !sameSpecialty(doctor.getSpecialty(), performingSpecialty))
            throw new IllegalArgumentException("Bác sĩ không phù hợp với dịch vụ hình ảnh");
        this.assignedDoctor = doctor;
        this.assignedTechnician = null;
        this.acceptedAt = LocalDateTime.now();
        this.status = Status.ACCEPTED;
    }

    public void assignTechnician(Technician technician)
    {
        requireStatus(Status.WAITING_ACCEPTANCE);
        if (serviceTypeSnapshot != DiagnosticService.ServiceType.LABORATORY || technician == null ||
                !sameSpecialty(technician.getSpecialty(), performingSpecialty) ||
                !technician.canPerform(service))
            throw new IllegalArgumentException("Kỹ thuật viên không phù hợp với xét nghiệm");
        this.assignedTechnician = technician;
        this.assignedDoctor = null;
        this.acceptedAt = LocalDateTime.now();
        this.status = Status.ACCEPTED;
    }

    // DAO kiểm tra các mẫu liên quan trước khi xác nhận bước này
    public void markSampleCollected(LocalDateTime collectedAt)
    {
        requireStatus(Status.ACCEPTED);
        if (!requiresSpecimen || collectedAt == null)
            throw new IllegalArgumentException("Thông tin lấy mẫu không hợp lệ");
        this.expectedResultAt = collectedAt.plusMinutes(estimatedDurationMinutes);
        this.status = Status.SAMPLE_COLLECTED;
    }

    public void startProcessing()
    {
        if ((requiresSpecimen && status != Status.SAMPLE_COLLECTED) ||
                (!requiresSpecimen && status != Status.ACCEPTED))
            throw new IllegalStateException("Chỉ định chưa sẵn sàng thực hiện");
        this.startedAt = LocalDateTime.now();
        if (expectedResultAt == null)
            this.expectedResultAt = startedAt.plusMinutes(estimatedDurationMinutes);
        this.status = Status.IN_PROGRESS;
    }

    // DAO chỉ gọi sau khi đã từ chối mẫu cũ; cho phép lấy mẫu thay thế trước phân tích
    public void requestNewSpecimen()
    {
        requireStatus(Status.SAMPLE_COLLECTED);
        if (!requiresSpecimen) throw new IllegalStateException("Chỉ định không cần mẫu");
        this.expectedResultAt = null;
        this.status = Status.ACCEPTED;
    }

    void attachResult(DiagnosticResult result)
    {
        requireStatus(Status.IN_PROGRESS);
        if (result == null || this.result != null)
            throw new IllegalStateException("Kết quả không hợp lệ hoặc đã tồn tại");
        this.result = result;
    }

    void complete(DiagnosticResult result)
    {
        requireStatus(Status.IN_PROGRESS);
        if (this.result != result || result.getStatus() != DiagnosticResult.Status.RELEASED)
            throw new IllegalStateException("Kết quả chưa phát hành");
        this.completedAt = result.getReleasedAt();
        this.status = Status.COMPLETED;
    }

    public void cancel()
    {
        if (status != Status.DRAFT && status != Status.WAITING_PAYMENT && status != Status.WAITING_ACCEPTANCE)
            throw new IllegalStateException("Không thể hủy chỉ định đã được tiếp nhận");
        this.status = Status.CANCELLED;
    }

    private void requireStatus(Status expected)
    {
        if (status != expected)
            throw new IllegalStateException("Trạng thái chỉ định không phù hợp");
    }

    private static boolean sameSpecialty(Specialty first, Specialty second)
    {
        return first != null && second != null && (first == second ||
                (first.getSpecialtyId() != null && first.getSpecialtyId().equals(second.getSpecialtyId())));
    }

    // Getter
    public Long getOrderItemId() { return orderItemId; }
    public DiagnosticOrder getOrder() { return order; }
    public DiagnosticService getService() { return service; }
    public Specialty getPerformingSpecialty() { return performingSpecialty; }
    public String getServiceNameSnapshot() { return serviceNameSnapshot; }
    public DiagnosticService.ServiceType getServiceTypeSnapshot() { return serviceTypeSnapshot; }
    public boolean isRequiresSpecimen() { return requiresSpecimen; }
    public Doctor getAssignedDoctor() { return assignedDoctor; }
    public Technician getAssignedTechnician() { return assignedTechnician; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getExpectedResultAt() { return expectedResultAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Status getStatus() { return status; }
    public DiagnosticResult getResult() { return result; }
    public Long getVersion() { return version; }
}
