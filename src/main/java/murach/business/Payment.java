package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "payments")
public class Payment implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Method
    {
        CASH, VNPAY, MOMO, BANK_TRANSFER
    }

    public enum Status
    {
        PENDING, PAID, FAILED, REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 80)
    private String transactionCode;

    // Tương thích tạm với PaymentDB cũ: giao dịch cũ chưa có hóa đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // Tương thích tạm: giữ field appointment để các JPQL pm.appointment vẫn chạy
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 40)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Nationalized
    @Column(name = "note", length = 500)
    private String note;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;

    protected Payment() {}

    // Giao dịch mới trả toàn bộ một hóa đơn; lấy số tiền từ server
    public Payment(String transactionCode, Invoice invoice, Method method, String note)
    {
        this.transactionCode = User.requireText(transactionCode, "Mã giao dịch không được để trống");
        if (invoice == null || invoice.getStatus() != Invoice.Status.UNPAID)
            throw new IllegalArgumentException("Hóa đơn không ở trạng thái chờ thanh toán");
        if (method == null)
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống");
        this.invoice = invoice;
        this.appointment = invoice.getAppointment();
        this.amount = invoice.getTotalAmount();
        this.method = method;
        this.status = Status.PENDING;
        this.note = normalize(note);
    }

    // Tương thích tạm với PaymentDB cũ; xóa khi DAO đã chuyển sang Invoice
    @Deprecated
    public Payment(String transactionCode, Appointment appointment, BigDecimal amount,
                   Method method, Status status, String note)
    {
        this.transactionCode = User.requireText(transactionCode, "Mã giao dịch không được để trống");
        if (appointment == null)
            throw new IllegalArgumentException("Lịch hẹn không được để trống");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        if (method == null)
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống");
        if (status == null)
            throw new IllegalArgumentException("Trạng thái thanh toán không được để trống");
        this.appointment = appointment;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.note = normalize(note);
        if (status == Status.PAID)
            this.paidAt = LocalDateTime.now();
    }

    // Tương thích tạm; giao dịch có Invoice vẫn phải qua kiểm tra của luồng mới
    @Deprecated
    public void changeStatus(Status status)
    {
        if (status == null)
            throw new IllegalArgumentException("Trạng thái thanh toán không được để trống");
        if (invoice != null)
        {
            if (status == this.status)
                return;
            if (status == Status.PAID)
                markPaid();
            else if (status == Status.FAILED)
                markFailed(note == null ? "Thanh toán thất bại" : note);
            else
                throw new IllegalStateException("Không thể đổi trạng thái hóa đơn bằng API cũ");
            return;
        }
        this.status = status;
        if (status == Status.PAID && paidAt == null)
            this.paidAt = LocalDateTime.now();
    }

    public void changeMethod(Method method)
    {
        if (status != Status.PENDING || method == null)
            throw new IllegalStateException("Không thể đổi phương thức thanh toán");
        this.method = method;
    }

    // Chỉ gọi khi cơ chế xác nhận thanh toán của ứng dụng đã xác minh giao dịch
    public void markPaid()
    {
        if (status == Status.PAID)
            return;
        if (status != Status.PENDING || (invoice != null && (invoice.getStatus() != Invoice.Status.UNPAID || amount.compareTo(invoice.getTotalAmount()) != 0)))
            throw new IllegalStateException("Giao dịch hoặc hóa đơn không còn chờ thanh toán");

        this.status = Status.PAID; // <---------------------------------------------------------

        this.paidAt = LocalDateTime.now();
        if (invoice != null)
            invoice.markPaid(this);
    }

    public void markFailed(String reason)
    {
        if (status != Status.PENDING)
            throw new IllegalStateException("Giao dịch không còn ở trạng thái chờ");
        this.note = User.requireText(reason, "Cần ghi lý do thất bại");
        this.status = Status.FAILED;
    }

    public void changeNote(String note)
    {
        this.note = normalize(note);
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    // Getter
    public Long getPaymentId() { return paymentId; }
    public String getTransactionCode() { return transactionCode; }
    public Invoice getInvoice() { return invoice; }
    public BigDecimal getAmount() { return amount; }
    public Method getMethod() { return method; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getNote() { return note; }
    public Long getVersion() { return version; }
    public Appointment getAppointment() { return appointment; }
}
