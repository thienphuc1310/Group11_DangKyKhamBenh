package murach.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "service_prices")
public class ServicePrice implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "service_code", nullable = false, unique = true, length = 100)
    private String serviceCode;

    @Nationalized
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    protected ServicePrice() {}

    public ServicePrice(String serviceCode, String serviceName, BigDecimal amount, boolean status, User updatedBy)
    {
        this.serviceCode = requireText(serviceCode, "Mã dịch vụ không được để trống");
        this.serviceName = requireText(serviceName, "Tên dịch vụ không được để trống");
        this.amount = requireAmount(amount);
        this.status = status;
        this.updatedBy = updatedBy;
    }

    //đổi giá
    public void changePrice(BigDecimal amount, User updatedBy)
    {
        this.amount = requireAmount(amount);
        this.updatedBy = updatedBy;
    }

    //đổi tên dv
    public void rename(String serviceName, User updatedBy)
    {
        this.serviceName = requireText(serviceName, "Tên dịch vụ không được để trống");
        this.updatedBy = updatedBy;
    }

    public void activate(User updatedBy)
    {
        this.status = true;
        this.updatedBy = updatedBy;
    }

    //set false vs dịch vụ ngừng sử dụng
    //nhưng hiện tại project chỉ là đăng ký khám nên chưa thực  sự cần thiết cho vấn đề này, vì chỉ có giá đăng ký khám bệnh
    public void deactivate(User updatedBy)
    {
        this.status = false;
        this.updatedBy = updatedBy;
    }

    @PrePersist
    protected void onCreate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    private static String requireText(String value, String errorMessage)
    {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(errorMessage);

        return value.trim();
    }

    //check lớn hơn 0, kiểm tra thoi
    private static BigDecimal requireAmount(BigDecimal amount)
    {
        if (amount == null)
            throw new IllegalArgumentException("Giá dịch vụ không được để trống");

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá dịch vụ phải lớn hơn 0");

        return amount;
    }

    public Long getPriceId() { return priceId; }
    public String getServiceCode() { return serviceCode; }
    public String getServiceName() { return serviceName; }
    public BigDecimal getAmount() { return amount; }
    public boolean isStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public User getUpdatedBy() { return updatedBy; }
}