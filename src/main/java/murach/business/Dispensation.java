package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "dispensations")
public class Dispensation implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        PENDING, DISPENSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispensation_id")
    private Long dispensationId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false, unique = true)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private Pharmacist pharmacist;

    @Column(name = "dispensed_at")
    private LocalDateTime dispensedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status;

    @Nationalized
    @Column(name = "note", length = 1000)
    private String note;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Dispensation() {}

    // DAO kiểm tra hóa đơn thuốc trước khi tạo phiếu cấp
    public Dispensation(Prescription prescription, Pharmacist pharmacist, String note)
    {
        if (prescription == null || prescription.getStatus() != Prescription.Status.CONFIRMED)
            throw new IllegalArgumentException("Đơn thuốc chưa xác nhận");
        if (pharmacist == null)
            throw new IllegalArgumentException("Dược sĩ không được để trống");
        this.prescription = prescription;
        this.pharmacist = pharmacist;
        this.note = normalize(note);
        this.status = Status.PENDING;
    }

    // DAO đối chiếu đủ từng dòng thuốc, hóa đơn PAID trước khi xác nhận
    public void complete()
    {
        if (status != Status.PENDING)
            throw new IllegalStateException("Đơn thuốc đã được cấp");
        this.dispensedAt = LocalDateTime.now();
        this.status = Status.DISPENSED;
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Getter
    public Long getDispensationId() { return dispensationId; }
    public Prescription getPrescription() { return prescription; }
    public Pharmacist getPharmacist() { return pharmacist; }
    public LocalDateTime getDispensedAt() { return dispensedAt; }
    public Status getStatus() { return status; }
    public String getNote() { return note; }
    public Long getVersion() { return version; }
}
