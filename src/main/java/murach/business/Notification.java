package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "notifications")
public class Notification implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "notification_id")
    private Long notificationId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "leave_request_id", nullable = false)
    private DoctorLeaveRequest leaveRequest;
    @Nationalized @Column(name = "message", nullable = false, length = 1000)
    private String message;
    @Column(name = "is_read", nullable = false)
    private boolean read;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Notification() {}
    public Notification(User recipient, DoctorLeaveRequest leaveRequest, String message)
    {
        if (recipient == null || leaveRequest == null) throw new IllegalArgumentException("Thông báo thiếu người nhận hoặc đơn nghỉ");
        this.message = User.requireText(message, "Thông báo không được để trống");
        if (this.message.length() > 1000) throw new IllegalArgumentException("Thông báo tối đa 1000 ký tự");
        this.recipient = recipient; this.leaveRequest = leaveRequest; this.createdAt = LocalDateTime.now();
    }
    public void markRead() { read = true; }
    public Long getNotificationId() { return notificationId; }
    public User getRecipient() { return recipient; }
    public DoctorLeaveRequest getLeaveRequest() { return leaveRequest; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
