/* Chạy thủ công sau khi sao lưu và kiểm tra đúng database.
   Chỉ bổ sung bảng lịch tiếp tân, không xóa hoặc thay đổi dữ liệu hiện có.
   Tiếp tân: accounts.role = 'STAFF', staffs.job_type = 'RECEPTIONIST'. */
USE [QuanLyDangKyKhamBenh3];
GO

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.staffs', N'U') IS NULL
        THROW 50040, N'Cần có bảng staffs trước khi tạo lịch tiếp tân.', 1;

    IF OBJECT_ID(N'dbo.staff_schedules', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.staff_schedules
        (
            schedule_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            staff_id BIGINT NOT NULL,
            work_date DATE NOT NULL,
            start_time TIME NOT NULL,
            end_time TIME NOT NULL,
            room NVARCHAR(100) NOT NULL,
            CONSTRAINT FK_staff_schedules_staff FOREIGN KEY (staff_id) REFERENCES dbo.staffs(staff_id),
            CONSTRAINT CK_staff_schedules_time CHECK (end_time > start_time)
        );
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.staff_schedules')
                   AND name = N'IX_staff_schedules_worker_date')
        CREATE INDEX IX_staff_schedules_worker_date ON dbo.staff_schedules(staff_id, work_date);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
