package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "diagnostic_orders")
public class DiagnosticOrder implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        DRAFT, CONFIRMED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false)
    private MedicalEncounter encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordered_by_doctor_id", nullable = false)
    private Doctor orderedBy;

    @Nationalized
    @Column(name = "clinical_indication", columnDefinition = "NVARCHAR(MAX)")
    private String clinicalIndication;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<DiagnosticOrderItem> items = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected DiagnosticOrder() {}

    public DiagnosticOrder(String orderCode, MedicalEncounter encounter, Doctor orderedBy, String clinicalIndication)
    {
        this.orderCode = User.requireText(orderCode, "Mã chỉ định không được để trống");
        if (encounter == null || orderedBy == null)
            throw new IllegalArgumentException("Lượt khám và bác sĩ không được để trống");
        if (encounter.getStatus() != MedicalEncounter.Status.IN_CONSULTATION)
            throw new IllegalStateException("Chỉ lập chỉ định khi đang khám");
        if (encounter.getDoctor() != orderedBy &&
                (orderedBy.getDoctorId() == null ||
                 !orderedBy.getDoctorId().equals(encounter.getDoctor().getDoctorId())))
            throw new IllegalArgumentException("Bác sĩ không phụ trách lượt khám");
        this.encounter = encounter;
        this.orderedBy = orderedBy;
        this.clinicalIndication = normalize(clinicalIndication);
        this.orderedAt = LocalDateTime.now();
        this.status = Status.DRAFT;
    }

    public boolean sameAs(DiagnosticOrder other)
    {
        return other != null && (this == other ||
                (orderId != null && orderId.equals(other.getOrderId())));
    }

    public DiagnosticOrderItem addService(DiagnosticService service)
    {
        if (status != Status.DRAFT)
            throw new IllegalStateException("Chỉ thêm dịch vụ vào phiếu nháp");
        if (service == null)
            throw new IllegalArgumentException("Dịch vụ không được để trống");
        for (DiagnosticOrderItem item : items)
            if (item.getService() == service || (service.getServiceId() != null &&
                    service.getServiceId().equals(item.getService().getServiceId())))
                throw new IllegalArgumentException("Dịch vụ đã có trong phiếu");
        DiagnosticOrderItem item = new DiagnosticOrderItem(this, service);
        items.add(item);
        return item;
    }

    public void confirm()
    {
        if (status != Status.DRAFT || items.isEmpty())
            throw new IllegalStateException("Phiếu phải ở dạng nháp và có dịch vụ");
        for (DiagnosticOrderItem item : items)
            if (!item.getService().isStatus() || !item.getService().getPrice().isStatus())
                throw new IllegalStateException("Có dịch vụ hoặc bảng giá đã ngừng áp dụng");
        for (DiagnosticOrderItem item : items)
            item.submit();
        this.status = Status.CONFIRMED;
    }

    public void cancel()
    {
        if (status == Status.CANCELLED)
            throw new IllegalStateException("Phiếu đã hủy");
        for (DiagnosticOrderItem item : items)
            if (item.getStatus() != DiagnosticOrderItem.Status.DRAFT &&
                item.getStatus() != DiagnosticOrderItem.Status.WAITING_PAYMENT &&
                item.getStatus() != DiagnosticOrderItem.Status.WAITING_ACCEPTANCE &&
                item.getStatus() != DiagnosticOrderItem.Status.CANCELLED)
                throw new IllegalStateException("Có dịch vụ đã được tiếp nhận hoặc thực hiện");
        for (DiagnosticOrderItem item : items)
            if (item.getStatus() != DiagnosticOrderItem.Status.CANCELLED)
                item.cancel();
        this.status = Status.CANCELLED;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Getter
    public Long getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public MedicalEncounter getEncounter() { return encounter; }
    public Doctor getOrderedBy() { return orderedBy; }
    public String getClinicalIndication() { return clinicalIndication; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public Status getStatus() { return status; }
    public List<DiagnosticOrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Long getVersion() { return version; }
}
