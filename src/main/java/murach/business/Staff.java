package murach.business;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "staffs")
public class Staff implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum JobType
    {
        RECEPTIONIST, CASHIER
    }

    //staff_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    //staff_code
    @Column(name = "staff_code", nullable = false, unique = true, length = 50)
    private String staffCode;

    //user_id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    //jobtype
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 40)
    private JobType jobType;

    //constructor
    protected Staff() {}

    // Hồ sơ nhân viên; khóa tài khoản bằng Account.status
    public Staff(String staffCode, User user, JobType jobType)
    {
        this.staffCode = User.requireText(staffCode, "Mã nhân viên không được để trống");
        if (user == null)
            throw new IllegalArgumentException("Thông tin người dùng không được để trống");
        this.user = user;
        changeJobType(jobType);
    }

    //setter
    public void changeJobType(JobType jobType)
    {
        if (jobType == null)
            throw new IllegalArgumentException("Công việc không được để trống");
        this.jobType = jobType;
    }

    //getter
    public Long getStaffId() { return staffId; }
    public String getStaffCode() { return staffCode; }
    public User getUser() { return user; }
    public JobType getJobType() { return jobType; }
    public Long getUserId() { return user.getUserId(); }
    public String getFullName() { return user.getFullName(); }
    public String getPhone() { return user.getPhone(); }
    public String getEmail() { return user.getEmail(); }
}
