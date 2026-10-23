USE [QuanLyDangKyKhamBenh3];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* Mật khẩu đang lưu dạng text vì LoginDB hiện so sánh trực tiếp password_hash. */
    DECLARE @Password NVARCHAR(255) = N'Test@123';
    DECLARE @Today DATE = CAST(GETDATE() AS DATE);
    DECLARE @Date1 DATE = DATEADD(DAY, 1, @Today);
    DECLARE @Date2 DATE = DATEADD(DAY, 2, @Today);
    DECLARE @Date3 DATE = DATEADD(DAY, 3, @Today);

    /* ================================================================
       1. SPECIALTIES
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = N'Nội tổng quát')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Nội tổng quát', N'Khám và điều trị bệnh nội khoa tổng quát.', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = N'Tim mạch')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Tim mạch', N'Khám và điều trị các bệnh lý tim mạch.', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = N'Da liễu')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Da liễu', N'Khám và điều trị các bệnh về da.', 1);

    /* ================================================================
       2. USERS CỦA BÁC SĨ VÀ ADMIN
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'doctor.noi1@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Nguyễn Minh Anh', N'0901000101', N'doctor.noi1@test.local',
             '1985-03-12', N'Nam', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'doctor.noi2@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Phạm Thu Hương', N'0901000102', N'doctor.noi2@test.local',
             '1988-07-21', N'Nữ', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'doctor.tim@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Trần Hoàng Nam', N'0901000201', N'doctor.tim@test.local',
             '1981-08-20', N'Nam', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'doctor.da@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Lê Thu Hà', N'0901000301', N'doctor.da@test.local',
             '1988-11-05', N'Nữ', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'admin.booking@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Quản trị hệ thống', N'0901999999', N'admin.booking@test.local',
             '1990-01-01', N'Khác', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    /* ================================================================
       3. ACCOUNTS
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.noi1')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.noi1', @Password, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'doctor.noi1@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.noi2')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.noi2', @Password, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'doctor.noi2@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.tim')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.tim', @Password, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'doctor.tim@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.da')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.da', @Password, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'doctor.da@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'admin.booking')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'admin.booking', @Password, N'ADMIN', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'admin.booking@test.local';

    /* ================================================================
       4. DOCTORS
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-NOI-01')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC-TEST-NOI-01', u.user_id, s.specialty_id, N'LIC-TEST-NOI-01',
               N'Bác sĩ chuyên khoa I', 10, N'Bác sĩ Nội tổng quát.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'doctor.noi1@test.local'
          AND s.name = N'Nội tổng quát';

    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-NOI-02')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC-TEST-NOI-02', u.user_id, s.specialty_id, N'LIC-TEST-NOI-02',
               N'Thạc sĩ Y khoa', 12, N'Bác sĩ Nội tổng quát.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'doctor.noi2@test.local'
          AND s.name = N'Nội tổng quát';

    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-TIM-01')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC-TEST-TIM-01', u.user_id, s.specialty_id, N'LIC-TEST-TIM-01',
               N'Thạc sĩ Y khoa', 15, N'Bác sĩ chuyên khoa Tim mạch.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'doctor.tim@test.local'
          AND s.name = N'Tim mạch';

    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-DA-01')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC-TEST-DA-01', u.user_id, s.specialty_id, N'LIC-TEST-DA-01',
               N'Bác sĩ chuyên khoa I', 8, N'Bác sĩ chuyên khoa Da liễu.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'doctor.da@test.local'
          AND s.name = N'Da liễu';

    /* ================================================================
       5. ADMIN PROFILE
       ================================================================ */
    IF NOT EXISTS (
        SELECT 1
        FROM dbo.admins a
        JOIN dbo.users u ON u.user_id = a.user_id
        WHERE u.email = N'admin.booking@test.local'
    )
        INSERT INTO dbo.admins (user_id)
        SELECT user_id FROM dbo.users
        WHERE email = N'admin.booking@test.local';

    /* ================================================================
       6. DOCTOR SCHEDULES
       Dùng ngày động: ngày mai, ngày kia và ba ngày sau.
       ================================================================ */
    DECLARE @DoctorNoi1 BIGINT =
        (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-NOI-01');
    DECLARE @DoctorNoi2 BIGINT =
        (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-NOI-02');
    DECLARE @DoctorTim BIGINT =
        (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-TIM-01');
    DECLARE @DoctorDa BIGINT =
        (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC-TEST-DA-01');

    IF @DoctorNoi1 IS NULL OR @DoctorNoi2 IS NULL
       OR @DoctorTim IS NULL OR @DoctorDa IS NULL
        THROW 50001, N'Không tạo được đầy đủ hồ sơ bác sĩ.', 1;

    /* Ngày mai: hai bác sĩ Nội tổng quát và một bác sĩ Tim mạch. */
    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorNoi1 AND work_date = @Date1 AND start_time = '08:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorNoi1, @Date1, '08:00', '09:00', N'Phòng 101', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorNoi1 AND work_date = @Date1 AND start_time = '09:30')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorNoi1, @Date1, '09:30', '10:30', N'Phòng 101', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorNoi2 AND work_date = @Date1 AND start_time = '13:30')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorNoi2, @Date1, '13:30', '14:30', N'Phòng 102', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorTim AND work_date = @Date1 AND start_time = '08:30')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorTim, @Date1, '08:30', '09:30', N'Phòng 201', SYSDATETIME());

    /* Ngày kia: Tim mạch và Da liễu. */
    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorTim AND work_date = @Date2 AND start_time = '14:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorTim, @Date2, '14:00', '15:00', N'Phòng 201', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorDa AND work_date = @Date2 AND start_time = '09:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorDa, @Date2, '09:00', '10:00', N'Phòng 301', SYSDATETIME());

    /* Ba ngày sau: Da liễu có hai ca. */
    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorDa AND work_date = @Date3 AND start_time = '08:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorDa, @Date3, '08:00', '09:00', N'Phòng 301', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules
                   WHERE doctor_id = @DoctorDa AND work_date = @Date3 AND start_time = '10:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@DoctorDa, @Date3, '10:00', '11:00', N'Phòng 301', SYSDATETIME());

    COMMIT TRANSACTION;

    SELECT N'Đã nạp dữ liệu kiểm thử.' AS result;
    SELECT N'admin.booking' AS username, @Password AS password, N'ADMIN' AS role;
    SELECT N'doctor.noi1' AS username, @Password AS password, N'DOCTOR' AS role
    UNION ALL SELECT N'doctor.noi2', @Password, N'DOCTOR'
    UNION ALL SELECT N'doctor.tim', @Password, N'DOCTOR'
    UNION ALL SELECT N'doctor.da', @Password, N'DOCTOR';

    SELECT ds.work_date, s.name AS specialty_name, u.full_name AS doctor_name,
           ds.start_time, ds.end_time, ds.room
    FROM dbo.doctor_schedules ds
    JOIN dbo.doctors d ON d.doctor_id = ds.doctor_id
    JOIN dbo.users u ON u.user_id = d.user_id
    JOIN dbo.specialties s ON s.specialty_id = d.specialty_id
    WHERE ds.work_date IN (@Date1, @Date2, @Date3)
    ORDER BY ds.work_date, s.name, u.full_name, ds.start_time;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
