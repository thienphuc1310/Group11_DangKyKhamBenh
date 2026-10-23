package murach.business;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "prescription_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"prescription_id", "medicine_id"}))
public class PrescriptionItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_item_id")
    private Long prescriptionItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Nationalized
    @Column(name = "medicine_name_snapshot", nullable = false, length = 255)
    private String medicineNameSnapshot;

    @Nationalized
    @Column(name = "strength_snapshot", length = 100)
    private String strengthSnapshot;

    @Nationalized
    @Column(name = "unit_snapshot", nullable = false, length = 50)
    private String unitSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Nationalized
    @Column(name = "dosage", nullable = false, length = 255)
    private String dosage;

    @Nationalized
    @Column(name = "frequency", nullable = false, length = 255)
    private String frequency;

    @Nationalized
    @Column(name = "route", nullable = false, length = 100)
    private String route;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Nationalized
    @Column(name = "instruction", length = 1000)
    private String instruction;

    protected PrescriptionItem() {}

    // Tạo thông qua Prescription.addMedicine()
    protected PrescriptionItem(Prescription prescription, Medicine medicine, int quantity, String dosage,
                               String frequency, String route, int durationDays, String instruction)
    {
        if (prescription == null || medicine == null || !medicine.isStatus())
            throw new IllegalArgumentException("Đơn hoặc thuốc không hợp lệ");
        if (quantity < 1 || durationDays < 1)
            throw new IllegalArgumentException("Số lượng và số ngày phải lớn hơn 0");
        this.prescription = prescription;
        this.medicine = medicine;
        this.medicineNameSnapshot = medicine.getMedicineName();
        this.strengthSnapshot = medicine.getStrength();
        this.unitSnapshot = medicine.getUnit();
        this.quantity = quantity;
        this.dosage = User.requireText(dosage, "Liều dùng không được để trống");
        this.frequency = User.requireText(frequency, "Tần suất không được để trống");
        this.route = User.requireText(route, "Đường dùng không được để trống");
        this.durationDays = durationDays;
        this.instruction = normalize(instruction);
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Getter
    public Long getPrescriptionItemId() { return prescriptionItemId; }
    public Prescription getPrescription() { return prescription; }
    public Medicine getMedicine() { return medicine; }
    public String getMedicineNameSnapshot() { return medicineNameSnapshot; }
    public String getStrengthSnapshot() { return strengthSnapshot; }
    public String getUnitSnapshot() { return unitSnapshot; }
    public int getQuantity() { return quantity; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getRoute() { return route; }
    public int getDurationDays() { return durationDays; }
    public String getInstruction() { return instruction; }
}
