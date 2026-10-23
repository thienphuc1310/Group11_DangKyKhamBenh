package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "initial_assessments")
public class InitialAssessment implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long assessmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false, unique = true)
    private MedicalEncounter encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nurse_id", nullable = false)
    private Nurse nurse;

    @Nationalized
    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "systolic_pressure")
    private Integer systolicPressure;

    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "spo2")
    private Double spo2;

    @Nationalized
    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected InitialAssessment() {}

    public InitialAssessment(MedicalEncounter encounter, Nurse nurse)
    {
        if (encounter == null || nurse == null)
            throw new IllegalArgumentException("Lượt khám và điều dưỡng không được để trống");
        if (encounter.getStatus() != MedicalEncounter.Status.WAITING_ASSESSMENT &&
                encounter.getStatus() != MedicalEncounter.Status.ASSESSING)
            throw new IllegalStateException("Lượt khám không ở bước đánh giá ban đầu");
        this.encounter = encounter;
        this.nurse = nurse;
    }

    public void update(String chiefComplaint, Double height, Double weight, Double temperature,
                       Integer systolicPressure, Integer diastolicPressure, Integer heartRate,
                       Integer respiratoryRate, Double spo2, String note)
    {
        if (completed)
            throw new IllegalStateException("Phiếu đánh giá đã được xác nhận");
        requirePositive(height, "Chiều cao");
        requirePositive(weight, "Cân nặng");
        requirePositive(temperature, "Nhiệt độ");
        requirePositive(systolicPressure, "Huyết áp tâm thu");
        requirePositive(diastolicPressure, "Huyết áp tâm trương");
        requirePositive(heartRate, "Nhịp tim");
        requirePositive(respiratoryRate, "Nhịp thở");
        if (spo2 != null && (!Double.isFinite(spo2) || spo2 < 0 || spo2 > 100))
            throw new IllegalArgumentException("SpO2 phải nằm trong khoảng 0 đến 100");
        this.chiefComplaint = normalize(chiefComplaint);
        this.height = height;
        this.weight = weight;
        this.temperature = temperature;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.spo2 = spo2;
        this.note = normalize(note);
    }

    public void complete()
    {
        if (completed)
            throw new IllegalStateException("Phiếu đánh giá đã được xác nhận");
        User.requireText(chiefComplaint, "Cần nhập triệu chứng/lý do khám");
        if (temperature == null || systolicPressure == null || diastolicPressure == null || heartRate == null)
            throw new IllegalArgumentException("Cần nhập đủ sinh hiệu bắt buộc");
        if (encounter.getStatus() != MedicalEncounter.Status.ASSESSING)
            throw new IllegalStateException("Lượt khám chưa bắt đầu đánh giá");
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    private static void requirePositive(Number value, String name)
    {
        if (value != null && (!Double.isFinite(value.doubleValue()) || value.doubleValue() <= 0))
            throw new IllegalArgumentException(name + " phải lớn hơn 0");
    }

    private static String normalize(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    // Getter
    public Long getAssessmentId() { return assessmentId; }
    public MedicalEncounter getEncounter() { return encounter; }
    public Nurse getNurse() { return nurse; }
    public String getChiefComplaint() { return chiefComplaint; }
    public Double getHeight() { return height; }
    public Double getWeight() { return weight; }
    public Double getTemperature() { return temperature; }
    public Integer getSystolicPressure() { return systolicPressure; }
    public Integer getDiastolicPressure() { return diastolicPressure; }
    public Integer getHeartRate() { return heartRate; }
    public Integer getRespiratoryRate() { return respiratoryRate; }
    public Double getSpo2() { return spo2; }
    public String getNote() { return note; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Long getVersion() { return version; }
}
