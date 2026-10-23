package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "medicines")
public class Medicine implements Serializable
{
    private static final long serialVersionUID = 1L;

    //Medicine_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    //Medicine_code
    @Column(name = "medicine_code", nullable = false, unique = true, length = 80)
    private String medicineCode;

    @Nationalized
    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    // Thông tin tùy chọn giữ để tương thích code cũ, không bắt buộc nhập trên form mới
    @Nationalized
    @Column(name = "active_ingredient", length = 255)
    private String activeIngredient;

    @Nationalized
    @Column(name = "strength", length = 100)
    private String strength;

    @Nationalized
    @Column(name = "dosage_form", length = 100)
    private String dosageForm;

    @Nationalized
    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    // Tồn kho trực tiếp theo thuốc, không quản lý lô
    @ColumnDefault("0")
    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @ColumnDefault("0")
    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    // Ngày nhập gần nhất; lịch sử từng thao tác nằm trong StockMovement
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;

    //description
    @Nationalized
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    protected Medicine() {}

    // Form đơn giản: mã tự sinh, đơn vị cố định là viên; số lượng nhập qua StockMovement.receive()
    public Medicine(String medicineName, String description, BigDecimal price)
    {
        this("MED-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT),
                medicineName, null, null, null, "Viên", price, description);
    }

    public Medicine(String medicineCode, String medicineName, String activeIngredient,
                    String strength, String dosageForm, String unit, BigDecimal price, String description)
    {
        this.medicineCode = User.requireText(medicineCode, "Mã thuốc không được để trống");
        update(medicineName, activeIngredient, strength, dosageForm, unit, price, description);
    }

    public void update(String medicineName, String activeIngredient, String strength,
                       String dosageForm, String unit, BigDecimal price, String description)
    {
        String checkedName = User.requireText(medicineName, "Tên thuốc không được để trống");
        String checkedUnit = User.requireText(unit, "Đơn vị tính không được để trống");
        BigDecimal checkedPrice = requireMoney(price);
        if (checkedPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá thuốc phải lớn hơn 0");
        this.medicineName = checkedName;
        this.activeIngredient = normalize(activeIngredient);
        this.strength = normalize(strength);
        this.dosageForm = normalize(dosageForm);
        this.unit = checkedUnit;
        this.price = checkedPrice;
        this.description = normalize(description);
    }

    public void changeStatus(boolean status)
    {
        this.status = status;
    }

    // Cập nhật thông tin không được gán đè số lượng tồn hoặc số lượng đã giữ
    public void update(String medicineName, String description, BigDecimal price)
    {
        update(medicineName, activeIngredient, strength, dosageForm, unit, price, description);
    }

    // Chỉ gọi qua StockMovement để số lượng và lịch sử được lưu trong cùng transaction
    void receive(int quantity, LocalDateTime receivedAt)
    {
        requireQuantity(quantity);
        if (receivedAt == null || receivedAt.toLocalDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày nhập không được để trống hoặc ở tương lai");
        int updatedQuantity = Math.addExact(quantityOnHand, quantity);
        this.quantityOnHand = updatedQuantity;
        if (this.receivedAt == null || receivedAt.isAfter(this.receivedAt))
            this.receivedAt = receivedAt;
    }

    void reserve(int quantity)
    {
        requireQuantity(quantity);
        if (!status)
            throw new IllegalStateException("Thuốc đã ngừng sử dụng");
        if (quantity > getAvailableQuantity())
            throw new IllegalArgumentException("Thuốc không đủ số lượng khả dụng");
        this.quantityReserved = Math.addExact(quantityReserved, quantity);
    }

    void release(int quantity)
    {
        requireQuantity(quantity);
        if (quantity > quantityReserved)
            throw new IllegalArgumentException("Số lượng giải phóng vượt lượng đã giữ");
        this.quantityReserved -= quantity;
    }

    void dispense(int quantity)
    {
        requireQuantity(quantity);
        if (!status)
            throw new IllegalStateException("Thuốc đã ngừng sử dụng");
        if (quantity > quantityReserved || quantity > quantityOnHand)
            throw new IllegalArgumentException("Chưa giữ đủ thuốc hoặc không đủ tồn kho");
        this.quantityOnHand -= quantity;
        this.quantityReserved -= quantity;
    }

    private static void requireQuantity(int quantity)
    {
        if (quantity < 1)
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
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

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Getter
    public Long getMedicineId() { return medicineId; }
    public String getMedicineCode() { return medicineCode; }
    public String getMedicineName() { return medicineName; }
    public String getActiveIngredient() { return activeIngredient; }
    public String getStrength() { return strength; }
    public String getDosageForm() { return dosageForm; }
    public String getUnit() { return unit; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }
    public boolean isStatus() { return status; }
    public int getQuantityOnHand() { return quantityOnHand; }
    public int getQuantityReserved() { return quantityReserved; }
    public int getAvailableQuantity() { return quantityOnHand - quantityReserved; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public Long getVersion() { return version; }
}
