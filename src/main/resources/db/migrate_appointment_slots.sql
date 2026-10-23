USE QuanLyDangKyKhamBenh3;
GO

/* Mỗi dòng trong doctor_schedules là một ca làm việc.
   Bảng này chia ca làm việc thành các khung đặt khám tròn 60 phút. */
IF OBJECT_ID(N'dbo.appointment_slots', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.appointment_slots
    (
        slot_id       BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        schedule_id   BIGINT NOT NULL,
        start_time    TIME NOT NULL,
        end_time      TIME NOT NULL,
        max_patients  INT NOT NULL CONSTRAINT DF_appointment_slots_max_patients DEFAULT (4),
        status        VARCHAR(20) NOT NULL CONSTRAINT DF_appointment_slots_status DEFAULT ('OPEN'),
        created_at    DATETIME2 NOT NULL CONSTRAINT DF_appointment_slots_created_at DEFAULT (SYSDATETIME()),
        updated_at    DATETIME2 NULL,
        CONSTRAINT FK_appointment_slots_schedule
            FOREIGN KEY (schedule_id) REFERENCES dbo.doctor_schedules(schedule_id),
        CONSTRAINT UQ_appointment_slots_schedule_start UNIQUE (schedule_id, start_time),
        CONSTRAINT CK_appointment_slots_capacity CHECK (max_patients > 0),
        CONSTRAINT CK_appointment_slots_time CHECK (start_time < end_time)
    );
END;
GO

/* Giữ nullable để các lịch hẹn cũ vẫn tồn tại; lịch hẹn mới luôn phải gắn slot. */
IF COL_LENGTH(N'dbo.appointments', N'slot_id') IS NULL
    ALTER TABLE dbo.appointments ADD slot_id BIGINT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_appointments_slot'
      AND parent_object_id = OBJECT_ID(N'dbo.appointments')
)
BEGIN
    ALTER TABLE dbo.appointments
        ADD CONSTRAINT FK_appointments_slot
        FOREIGN KEY (slot_id) REFERENCES dbo.appointment_slots(slot_id);
END;
GO

/* Tạo slot cho các ca đã tồn tại. Phần lẻ dưới 60 phút không tạo slot. */
WITH hour_offsets AS
(
    SELECT n
    FROM (VALUES (0),(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11)) AS numbers(n)
)
INSERT INTO dbo.appointment_slots
    (schedule_id, start_time, end_time, max_patients, status, created_at, updated_at)
SELECT ds.schedule_id,
       CAST(DATEADD(HOUR, h.n, CAST(ds.start_time AS DATETIME2)) AS TIME),
       CAST(DATEADD(HOUR, h.n + 1, CAST(ds.start_time AS DATETIME2)) AS TIME),
       4,
       'OPEN',
       SYSDATETIME(),
       SYSDATETIME()
FROM dbo.doctor_schedules ds
CROSS JOIN hour_offsets h
WHERE CAST(DATEADD(HOUR, h.n + 1, CAST(ds.start_time AS DATETIME2)) AS TIME) <= ds.end_time
  AND NOT EXISTS
  (
      SELECT 1
      FROM dbo.appointment_slots aps
      WHERE aps.schedule_id = ds.schedule_id
        AND aps.start_time = CAST(DATEADD(HOUR, h.n, CAST(ds.start_time AS DATETIME2)) AS TIME)
  );
GO

SELECT aps.slot_id, aps.schedule_id, ds.work_date,
       aps.start_time, aps.end_time, aps.max_patients, aps.status
FROM dbo.appointment_slots aps
JOIN dbo.doctor_schedules ds ON ds.schedule_id = aps.schedule_id
ORDER BY ds.work_date, aps.start_time;
