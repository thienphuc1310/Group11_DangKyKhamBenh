package murach.model;

/** Dòng nhập từ form, không phải Entity và không tạo bảng mới. */
public class PrescriptionLineInput
{
    public final Long medicineId;
    public final int quantity;
    public final String dosage;
    public final String frequency;
    public final String route;
    public final int durationDays;
    public final String instruction;

    public PrescriptionLineInput(Long medicineId, int quantity, String dosage, String frequency,
                                 String route, int durationDays, String instruction)
    {
        if (medicineId == null || medicineId <= 0 || quantity < 1 || durationDays < 1)
            throw new IllegalArgumentException("Thuốc, số lượng và số ngày không hợp lệ");
        if (dosage == null || dosage.trim().isEmpty() || dosage.length() > 255 ||
                frequency == null || frequency.trim().isEmpty() || frequency.length() > 255 ||
                route == null || route.trim().isEmpty() || route.length() > 100 ||
                (instruction != null && instruction.length() > 1000))
            throw new IllegalArgumentException("Nhập đủ liều dùng, tần suất, đường dùng đúng độ dài");
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
        this.frequency = frequency;
        this.route = route;
        this.durationDays = durationDays;
        this.instruction = instruction;
    }
}
