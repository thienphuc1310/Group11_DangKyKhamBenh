package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "stock_movements")
public class StockMovement implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum MovementType
    {
        INBOUND, RESERVE, RELEASE, DISPENSE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id")
    private PrescriptionItem prescriptionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensation_id")
    private Dispensation dispensation;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    private User performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Nationalized
    @Column(name = "note", length = 1000)
    private String note;

    protected StockMovement() {}

    private StockMovement(Medicine medicine, PrescriptionItem item, Dispensation dispensation,
                          MovementType movementType, int quantity, User performedBy, String note)
    {
        if (medicine == null || performedBy == null || movementType == null || quantity < 1)
            throw new IllegalArgumentException("Thông tin biến động kho không hợp lệ");
        if (movementType != MovementType.INBOUND)
        {
            if (item == null || quantity > item.getQuantity())
                throw new IllegalArgumentException("Dòng đơn thuốc hoặc số lượng không hợp lệ");
            Medicine prescribedMedicine = item.getMedicine();
            if (medicine != prescribedMedicine && (medicine.getMedicineId() == null ||
                    !medicine.getMedicineId().equals(prescribedMedicine.getMedicineId())))
                throw new IllegalArgumentException("Thuốc không khớp dòng đơn thuốc");
        }
        this.medicine = medicine;
        this.prescriptionItem = item;
        this.dispensation = dispensation;
        this.movementType = movementType;
        this.quantity = quantity;
        this.performedBy = performedBy;
        this.note = normalize(note);
    }

    public static StockMovement receive(Medicine medicine, int quantity, User user, String note)
    {
        return receive(medicine, quantity, LocalDateTime.now(), user, note);
    }

    // Form có thể truyền ngày nhập; createdAt vẫn là thời điểm hệ thống ghi nhận thao tác
    public static StockMovement receive(Medicine medicine, int quantity, LocalDateTime receivedAt,
                                        User user, String note)
    {
        StockMovement movement = new StockMovement(medicine, null, null, MovementType.INBOUND, quantity, user, note);
        medicine.receive(quantity, receivedAt);
        return movement;
    }

    // Trước đây: DAO kiểm tra tổng lượng đã giữ theo dòng đơn, kể cả khi chia nhiều lô
    // Hiện tại: DAO kiểm tra tổng lượng đã giữ của đúng dòng đơn trên Medicine
    public static StockMovement reserve(Medicine medicine, PrescriptionItem item, int quantity, User user)
    {
        StockMovement movement = new StockMovement(medicine, item, null, MovementType.RESERVE, quantity, user, null);
        if (item.getPrescription().getStatus() == Prescription.Status.CANCELLED)
            throw new IllegalStateException("Đơn thuốc đã hủy");
        medicine.reserve(quantity);
        return movement;
    }

    public static StockMovement release(Medicine medicine, PrescriptionItem item, int quantity, User user, String note)
    {
        StockMovement movement = new StockMovement(medicine, item, null, MovementType.RELEASE, quantity, user, note);
        medicine.release(quantity);
        return movement;
    }

    // Trước đây: DAO kiểm tra lượng giữ của đúng dòng/đúng lô và chống cấp hai lần
    // Hiện tại: DAO kiểm tra lượng giữ của đúng dòng/đúng thuốc và chống cấp hai lần
    public static StockMovement dispense(Medicine medicine, PrescriptionItem item, Dispensation slip,
                                         int quantity, User user)
    {
        StockMovement movement = new StockMovement(medicine, item, slip, MovementType.DISPENSE, quantity, user, null);
        if (slip == null || slip.getStatus() != Dispensation.Status.PENDING ||
                item.getPrescription().getStatus() != Prescription.Status.CONFIRMED ||
                !item.getPrescription().sameAs(slip.getPrescription()))
            throw new IllegalStateException("Phiếu cấp thuốc không phù hợp");
        medicine.dispense(quantity);
        return movement;
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
    public Long getMovementId() { return movementId; }
    public Medicine getMedicine() { return medicine; }
    public PrescriptionItem getPrescriptionItem() { return prescriptionItem; }
    public Dispensation getDispensation() { return dispensation; }
    public MovementType getMovementType() { return movementType; }
    public int getQuantity() { return quantity; }
    public User getPerformedBy() { return performedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getNote() { return note; }
}
