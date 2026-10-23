package murach.business;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "result_files")
public class ResultFile implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private DiagnosticResult result;

    @Nationalized
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    //constructor rỗng
    protected ResultFile() {}

    public ResultFile(DiagnosticResult result, String originalName, String storageKey,
                      String contentType, long fileSize, User uploadedBy)
    {
        if (result == null || uploadedBy == null)
            throw new IllegalArgumentException("Kết quả và người tải lên không được để trống");
        if (result.getStatus() != DiagnosticResult.Status.DRAFT)
            throw new IllegalStateException("Không đính kèm thêm vào kết quả đã phát hành");
        if (fileSize < 1)
            throw new IllegalArgumentException("Tệp phải có dữ liệu");
        this.result = result;
        this.originalName = User.requireText(originalName, "Tên tệp không được để trống");
        this.storageKey = User.requireText(storageKey, "Khóa lưu trữ không được để trống");
        this.contentType = User.requireText(contentType, "Loại tệp không được để trống");
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getter
    public Long getFileId() { return fileId; }
    public DiagnosticResult getResult() { return result; }
    public String getOriginalName() { return originalName; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public User getUploadedBy() { return uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
