USE [QuanLyDangKyKhamBenh2];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM dbo.doctor_schedules
        WHERE doctor_id = 1 AND work_date = '2026-09-21' AND start_time = '07:30'
    )
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES
            (1, '2026-09-21', '07:30', '11:30', N'Phòng khám 101', SYSDATETIME());

    IF NOT EXISTS (
        SELECT 1 FROM dbo.doctor_schedules
        WHERE doctor_id = 2 AND work_date = '2026-09-23' AND start_time = '13:00'
    )
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES
            (2, '2026-09-23', '13:00', '17:00', N'Phòng khám 201', SYSDATETIME());

    IF NOT EXISTS (
        SELECT 1 FROM dbo.doctor_schedules
        WHERE doctor_id = 3 AND work_date = '2026-09-25' AND start_time = '08:00'
    )
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES
            (3, '2026-09-25', '08:00', '12:00', N'Phòng khám 301', SYSDATETIME());

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT
    schedule_id,
    doctor_id,
    work_date,
    start_time,
    end_time,
    room
FROM dbo.doctor_schedules
WHERE work_date BETWEEN '2026-09-21' AND '2026-09-27'
ORDER BY work_date, start_time;
GO
