package murach.business;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "diagnostic_services")
public class DiagnosticService implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum ServiceType
    {
        LABORATORY, IMAGING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "service_code", nullable = false, unique = true, length = 100)
    private String serviceCode;

    @Nationalized
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    private ServiceType serviceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_id", nullable = false, unique = true)
    private ServicePrice price;

    @Column(name = "requires_specimen", nullable = false)
    private boolean requiresSpecimen;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private int estimatedDurationMinutes;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    protected DiagnosticService() {}

    public DiagnosticService(String serviceCode, String serviceName, ServiceType serviceType,
                             Specialty specialty, ServicePrice price,
                             boolean requiresSpecimen, int estimatedDurationMinutes)
    {
        this.serviceCode = User.requireText(serviceCode, "Mã dịch vụ không được để trống");
        update(serviceName, serviceType, specialty, price,
                requiresSpecimen, estimatedDurationMinutes);
    }

    public void update(String serviceName, ServiceType serviceType, Specialty specialty,
                       ServicePrice price,
                       boolean requiresSpecimen, int estimatedDurationMinutes)
    {
        String checkedName = User.requireText(serviceName, "Tên dịch vụ không được để trống");
        if (serviceType == null)
            throw new IllegalArgumentException("Loại dịch vụ không được để trống");
        if (specialty == null || specialty.getSpecialtyType() != Specialty.SpecialtyType.PARACLINICAL)
            throw new IllegalArgumentException("Dịch vụ phải thuộc khoa cận lâm sàng");
        if (serviceType == ServiceType.IMAGING && requiresSpecimen)
            throw new IllegalArgumentException("Dịch vụ hình ảnh trong mô hình này không lấy mẫu");
        if (price == null || !serviceCode.equals(price.getServiceCode()))
            throw new IllegalArgumentException("Bảng giá phải có cùng mã dịch vụ");
        if (estimatedDurationMinutes < 1)
            throw new IllegalArgumentException("Thời gian dự kiến phải lớn hơn 0");
        this.serviceName = checkedName;
        this.serviceType = serviceType;
        this.specialty = specialty;
        this.price = price;
        this.requiresSpecimen = requiresSpecimen;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public void changeStatus(boolean status)
    {
        this.status = status;
    }

    // Getter
    public Long getServiceId() { return serviceId; }
    public String getServiceCode() { return serviceCode; }
    public String getServiceName() { return serviceName; }
    public ServiceType getServiceType() { return serviceType; }
    public Specialty getSpecialty() { return specialty; }
    public ServicePrice getPrice() { return price; }
    public boolean isRequiresSpecimen() { return requiresSpecimen; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public boolean isStatus() { return status; }
}
