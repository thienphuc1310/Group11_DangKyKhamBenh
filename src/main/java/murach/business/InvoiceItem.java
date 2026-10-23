package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_price_id")
    private ServicePrice servicePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_order_item_id")
    private DiagnosticOrderItem diagnosticOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id")
    private PrescriptionItem prescriptionItem;

    @Nationalized
    @Column(name = "description_snapshot", nullable = false, length = 500)
    private String descriptionSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal;

    protected InvoiceItem() {}

    private InvoiceItem(Invoice invoice, String description, int quantity, BigDecimal unitPrice)
    {
        if (invoice == null || quantity < 1)
            throw new IllegalArgumentException("Thông tin dòng hóa đơn không hợp lệ");
        this.invoice = invoice;
        this.descriptionSnapshot = User.requireText(description, "Nội dung hóa đơn không được để trống");
        this.quantity = quantity;
        this.unitPrice = requireMoney(unitPrice);
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    static InvoiceItem consultation(Invoice invoice, ServicePrice price, BigDecimal fee)
    {
        InvoiceItem item = new InvoiceItem(invoice, price.getServiceName(), 1, fee);
        item.servicePrice = price;
        return item;
    }

    static InvoiceItem diagnostic(Invoice invoice, DiagnosticOrderItem orderItem)
    {
        ServicePrice price = orderItem.getService().getPrice();
        if (!price.isStatus())
            throw new IllegalStateException("Giá dịch vụ đã ngừng áp dụng");
        InvoiceItem item = new InvoiceItem(invoice, orderItem.getServiceNameSnapshot(), 1, price.getAmount());
        item.servicePrice = price;
        item.diagnosticOrderItem = orderItem;
        return item;
    }

    static InvoiceItem medicine(Invoice invoice, PrescriptionItem prescriptionItem)
    {
        InvoiceItem item = new InvoiceItem(invoice, prescriptionItem.getMedicineNameSnapshot(),
                prescriptionItem.getQuantity(), prescriptionItem.getMedicine().getPrice());
        item.prescriptionItem = prescriptionItem;
        return item;
    }

    private static BigDecimal requireMoney(BigDecimal value)
    {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Số tiền không được âm hoặc để trống");
        try
        {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        }
        catch (ArithmeticException e)
        {
            throw new IllegalArgumentException("Số tiền chỉ được có tối đa hai chữ số thập phân");
        }
    }

    // Getter
    public Long getInvoiceItemId() { return invoiceItemId; }
    public Invoice getInvoice() { return invoice; }
    public ServicePrice getServicePrice() { return servicePrice; }
    public DiagnosticOrderItem getDiagnosticOrderItem() { return diagnosticOrderItem; }
    public PrescriptionItem getPrescriptionItem() { return prescriptionItem; }
    public String getDescriptionSnapshot() { return descriptionSnapshot; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
