package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "invoices")
public class Invoice implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum InvoiceType
    {
        CONSULTATION, DIAGNOSTIC, MEDICINE
    }

    public enum Status
    {
        DRAFT, UNPAID, PAID, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 60)
    private String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private MedicalEncounter encounter;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 40)
    private InvoiceType invoiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_order_id")
    private DiagnosticOrder diagnosticOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.PERSIST)
    private List<InvoiceItem> items = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Invoice() {}

    private Invoice(String invoiceCode, Appointment appointment, MedicalEncounter encounter, InvoiceType invoiceType)
    {
        this.invoiceCode = User.requireText(invoiceCode, "Mã hóa đơn không được để trống");
        if (appointment == null || invoiceType == null)
            throw new IllegalArgumentException("Lịch hẹn và loại hóa đơn không được để trống");
        if (appointment.getStatus() == Appointment.Status.CANCELLED)
            throw new IllegalStateException("Lịch hẹn đã bị hủy");
        this.appointment = appointment;
        this.encounter = encounter;
        this.invoiceType = invoiceType;
        this.status = Status.DRAFT;
    }

    // Phí khám lấy từ giá đã chốt trong lịch hẹn, không đọc lại giá mới
    public static Invoice forConsultation(String code, Appointment appointment, ServicePrice price)
    {
        if (price == null || !price.isStatus() ||
                !"DANG_KY_KHAM_BENH".equals(price.getServiceCode()))
            throw new IllegalArgumentException("Bảng giá đăng ký khám không hợp lệ");
        Invoice invoice = new Invoice(code, appointment, null, InvoiceType.CONSULTATION);
        invoice.addItem(InvoiceItem.consultation(invoice, price, appointment.getFee()));
        invoice.issue();
        return invoice;
    }

    public static Invoice forDiagnostics(String code, DiagnosticOrder order)
    {
        if (order == null || order.getStatus() != DiagnosticOrder.Status.CONFIRMED)
            throw new IllegalArgumentException("Phiếu chỉ định chưa xác nhận");
        Invoice invoice = new Invoice(code, order.getEncounter().getAppointment(),
                order.getEncounter(), InvoiceType.DIAGNOSTIC);
        invoice.diagnosticOrder = order;
        for (DiagnosticOrderItem item : order.getItems())
        {
            if (item.getStatus() == DiagnosticOrderItem.Status.CANCELLED)
                continue;
            if (item.getStatus() != DiagnosticOrderItem.Status.WAITING_PAYMENT)
                throw new IllegalStateException("Dịch vụ không ở trạng thái chờ thanh toán");
            invoice.addItem(InvoiceItem.diagnostic(invoice, item));
        }
        invoice.issue();
        return invoice;
    }

    public static Invoice forPrescription(String code, Prescription prescription)
    {
        if (prescription == null || prescription.getStatus() != Prescription.Status.CONFIRMED)
            throw new IllegalArgumentException("Đơn thuốc chưa xác nhận");
        Invoice invoice = new Invoice(code, prescription.getEncounter().getAppointment(),
                prescription.getEncounter(), InvoiceType.MEDICINE);
        invoice.prescription = prescription;
        for (PrescriptionItem item : prescription.getItems())
            invoice.addItem(InvoiceItem.medicine(invoice, item));
        invoice.issue();
        return invoice;
    }

    private void addItem(InvoiceItem item)
    {
        if (status != Status.DRAFT || item == null)
            throw new IllegalStateException("Không thể thêm dòng hóa đơn");
        this.items.add(item);
        this.totalAmount = totalAmount.add(item.getLineTotal());
    }

    private void issue()
    {
        if (status != Status.DRAFT || items.isEmpty() || totalAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("Hóa đơn chưa có đủ nội dung");
        this.issuedAt = LocalDateTime.now();
        this.status = Status.UNPAID;
    }

    public boolean sameAs(Invoice other)
    {
        return other != null && (this == other ||
                (invoiceId != null && invoiceId.equals(other.getInvoiceId())));
    }

    void markPaid(Payment payment)
    {
        if (status != Status.UNPAID || payment == null || !sameAs(payment.getInvoice()) ||
                payment.getAmount().compareTo(totalAmount) != 0 ||
                payment.getStatus() != Payment.Status.PAID)
            throw new IllegalStateException("Giao dịch không phù hợp với hóa đơn");
        this.status = Status.PAID;
    }

    // DAO xử lý nghĩa vụ liên quan trước khi hủy hóa đơn chưa thanh toán
    public void cancel()
    {
        if (status != Status.DRAFT && status != Status.UNPAID)
            throw new IllegalStateException("Không thể hủy hóa đơn đã thanh toán hoặc đã hủy");
        this.status = Status.CANCELLED;
    }

    // Getter
    public Long getInvoiceId() { return invoiceId; }
    public String getInvoiceCode() { return invoiceCode; }
    public Appointment getAppointment() { return appointment; }
    public MedicalEncounter getEncounter() { return encounter; }
    public InvoiceType getInvoiceType() { return invoiceType; }
    public DiagnosticOrder getDiagnosticOrder() { return diagnosticOrder; }
    public Prescription getPrescription() { return prescription; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Status getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public List<InvoiceItem> getItems() { return Collections.unmodifiableList(items); }
    public Long getVersion() { return version; }
    public Patient getPatient() { return appointment.getPatient(); }
}
