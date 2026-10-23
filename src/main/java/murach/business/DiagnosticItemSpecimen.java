package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "diagnostic_item_specimens",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_item_id", "specimen_id"}))
public class DiagnosticItemSpecimen implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private DiagnosticOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specimen_id", nullable = false)
    private Specimen specimen;

    protected DiagnosticItemSpecimen() {}

    public DiagnosticItemSpecimen(DiagnosticOrderItem orderItem, Specimen specimen)
    {
        if (orderItem == null || specimen == null || !orderItem.isRequiresSpecimen())
            throw new IllegalArgumentException("Chỉ định và mẫu không hợp lệ");
        if (!orderItem.getOrder().sameAs(specimen.getOrder()))
            throw new IllegalArgumentException("Mẫu không thuộc cùng phiếu chỉ định");
        this.orderItem = orderItem;
        this.specimen = specimen;
    }

    // Getter
    public Long getLinkId() { return linkId; }
    public DiagnosticOrderItem getOrderItem() { return orderItem; }
    public Specimen getSpecimen() { return specimen; }
}
