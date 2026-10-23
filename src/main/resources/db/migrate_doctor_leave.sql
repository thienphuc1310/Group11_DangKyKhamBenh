-- Xin nghỉ và nhận ca thay. Chạy trên database của ứng dụng trước khi deploy.
-- Chỉ thêm bảng/cột/index; không xóa hoặc chuyển dữ liệu lịch hẹn cũ.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
GO
IF COL_LENGTH('dbo.doctor_schedules', 'status') IS NULL
    ALTER TABLE dbo.doctor_schedules ADD status VARCHAR(30) NOT NULL
        CONSTRAINT DF_doctor_schedules_leave_status DEFAULT 'ACTIVE' WITH VALUES;
IF COL_LENGTH('dbo.doctor_schedules', 'version') IS NULL
    ALTER TABLE dbo.doctor_schedules ADD version BIGINT NOT NULL
        CONSTRAINT DF_doctor_schedules_leave_version DEFAULT 0 WITH VALUES;
GO
IF OBJECT_ID('dbo.doctor_leave_requests', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.doctor_leave_requests (
        leave_request_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        schedule_id BIGINT NOT NULL,
        reason NVARCHAR(1000) NOT NULL,
        status VARCHAR(40) NOT NULL,
        created_at DATETIME2 NOT NULL,
        reviewed_by BIGINT NULL,
        reviewed_at DATETIME2 NULL,
        replacement_schedule_id BIGINT NULL,
        accepted_at DATETIME2 NULL,
        version BIGINT NOT NULL CONSTRAINT DF_doctor_leave_version DEFAULT 0,
        CONSTRAINT FK_doctor_leave_schedule FOREIGN KEY (schedule_id) REFERENCES dbo.doctor_schedules(schedule_id),
        CONSTRAINT FK_doctor_leave_reviewer FOREIGN KEY (reviewed_by) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_doctor_leave_replacement FOREIGN KEY (replacement_schedule_id) REFERENCES dbo.doctor_schedules(schedule_id)
    );
END;
GO
-- SQL Server chỉ cho một NULL trong UNIQUE thông thường: dùng filtered index.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.doctor_leave_requests') AND name = 'UX_doctor_leave_replacement')
    CREATE UNIQUE INDEX UX_doctor_leave_replacement ON dbo.doctor_leave_requests(replacement_schedule_id)
        WHERE replacement_schedule_id IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.doctor_leave_requests') AND name = 'UX_doctor_leave_open_schedule')
    CREATE UNIQUE INDEX UX_doctor_leave_open_schedule ON dbo.doctor_leave_requests(schedule_id)
        WHERE status <> 'REJECTED';
GO
IF OBJECT_ID('dbo.notifications', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.notifications (
        notification_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        recipient_user_id BIGINT NOT NULL,
        leave_request_id BIGINT NOT NULL,
        message NVARCHAR(1000) NOT NULL,
        is_read BIT NOT NULL CONSTRAINT DF_notification_read DEFAULT 0,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT FK_notification_recipient FOREIGN KEY (recipient_user_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_notification_leave FOREIGN KEY (leave_request_id) REFERENCES dbo.doctor_leave_requests(leave_request_id)
    );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.notifications') AND name = 'IX_notification_recipient')
    CREATE INDEX IX_notification_recipient ON dbo.notifications(recipient_user_id, is_read, created_at DESC);
COMMIT TRANSACTION;
GO
