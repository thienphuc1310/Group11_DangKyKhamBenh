USE [QuanLyDangKyKhamBenh2];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
    THROW 50001, N'Chưa có bảng users. Hãy chạy /test-jpa trước.', 1;

IF OBJECT_ID(N'dbo.doctor_schedules', N'U') IS NULL
    THROW 50003, N'Chưa có bảng doctor_schedules. Hãy chạy /test-jpa trước.', 1;

/* Nâng schema cũ trước, ở batch riêng để các INSERT phía sau nhận diện được cột room. */
IF COL_LENGTH(N'dbo.doctor_schedules', N'room') IS NULL
BEGIN
    EXEC sys.sp_executesql
        N'ALTER TABLE dbo.doctor_schedules ADD room NVARCHAR(100) NULL;';
    EXEC sys.sp_executesql
        N'UPDATE dbo.doctor_schedules
          SET room = N''Chưa phân phòng''
          WHERE room IS NULL;';
    EXEC sys.sp_executesql
        N'ALTER TABLE dbo.doctor_schedules
          ALTER COLUMN room NVARCHAR(100) NOT NULL;';
END

/* Loại bỏ cột cũ vì DoctorSchedule hiện là ca làm việc, không phải slot đặt khám. */
IF COL_LENGTH(N'dbo.doctor_schedules', N'is_booked') IS NOT NULL
BEGIN
    DECLARE @LegacyDefaultConstraint SYSNAME;
    SELECT @LegacyDefaultConstraint = dc.name
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.default_object_id = dc.object_id
    WHERE c.object_id = OBJECT_ID(N'dbo.doctor_schedules')
      AND c.name = N'is_booked';

    IF @LegacyDefaultConstraint IS NOT NULL
    BEGIN
        DECLARE @DropBookedConstraintSql NVARCHAR(MAX);
        SET @DropBookedConstraintSql = N'ALTER TABLE dbo.doctor_schedules DROP CONSTRAINT '
            + QUOTENAME(@LegacyDefaultConstraint) + N';';
        EXEC sys.sp_executesql @DropBookedConstraintSql;
    END

    ALTER TABLE dbo.doctor_schedules DROP COLUMN is_booked;
END

/* Loại bỏ cột max_patients nếu database đã được tạo từ Entity trước đó. */
IF COL_LENGTH(N'dbo.doctor_schedules', N'max_patients') IS NOT NULL
BEGIN
    DECLARE @LegacyMaxPatientsConstraint SYSNAME;
    SELECT @LegacyMaxPatientsConstraint = dc.name
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.default_object_id = dc.object_id
    WHERE c.object_id = OBJECT_ID(N'dbo.doctor_schedules')
      AND c.name = N'max_patients';

    IF @LegacyMaxPatientsConstraint IS NOT NULL
    BEGIN
        DECLARE @DropMaxPatientsConstraintSql NVARCHAR(MAX);
        SET @DropMaxPatientsConstraintSql = N'ALTER TABLE dbo.doctor_schedules DROP CONSTRAINT '
            + QUOTENAME(@LegacyMaxPatientsConstraint) + N';';
        EXEC sys.sp_executesql @DropMaxPatientsConstraintSql;
    END

    ALTER TABLE dbo.doctor_schedules DROP COLUMN max_patients;
END
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* ================================================================
       1. USERS
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'minhanh.doctor@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Nguyen Minh Anh', N'0901000001', N'minhanh.doctor@test.local',
             '1985-03-12', N'Nam', N'Quan 1, TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'hoangnam.doctor@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Tran Hoang Nam', N'0901000002', N'hoangnam.doctor@test.local',
             '1981-08-20', N'Nam', N'Quan 3, TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'thuha.doctor@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Le Thu Ha', N'0901000003', N'thuha.doctor@test.local',
             '1988-11-05', N'Nu', N'Quan Binh Thanh, TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'van.a.patient@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Nguyen Van A', N'0901234567', N'van.a.patient@test.local',
             '1995-06-10', N'Nam', N'Thanh pho Thu Duc, TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'thi.b.patient@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Tran Thi B', N'0978765432', N'thi.b.patient@test.local',
             '1992-02-18', N'Nu', N'Quan 7, TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'admin@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Quan tri he thong', N'0901999999', N'admin@test.local',
             '1990-01-01', N'Khac', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    /* Đồng bộ dữ liệu demo cũ sang chuỗi không dấu. */
    UPDATE dbo.users
    SET full_name = N'Nguyen Minh Anh',
        gender = N'Nam',
        address = N'Quan 1, TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'minhanh.doctor@test.local';

    UPDATE dbo.users
    SET full_name = N'Tran Hoang Nam',
        gender = N'Nam',
        address = N'Quan 3, TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'hoangnam.doctor@test.local';

    UPDATE dbo.users
    SET full_name = N'Le Thu Ha',
        gender = N'Nu',
        address = N'Quan Binh Thanh, TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'thuha.doctor@test.local';

    UPDATE dbo.users
    SET full_name = N'Nguyen Van A',
        gender = N'Nam',
        address = N'Thanh pho Thu Duc, TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'van.a.patient@test.local';

    UPDATE dbo.users
    SET full_name = N'Tran Thi B',
        gender = N'Nu',
        address = N'Quan 7, TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'thi.b.patient@test.local';

    UPDATE dbo.users
    SET full_name = N'Quan tri he thong',
        gender = N'Khac',
        address = N'TP.HCM',
        updated_at = SYSDATETIME()
    WHERE email = N'admin@test.local';

    /* ================================================================
       2. ACCOUNTS
       Password dưới đây chỉ là dữ liệu hiển thị/test, chưa phải BCrypt.
       ================================================================ */
    DECLARE @DemoPasswordHash NVARCHAR(255) = N'DEMO_ONLY_NOT_HASHED_Password123!';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.minhanh')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.minhanh', @DemoPasswordHash, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'minhanh.doctor@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.hoangnam')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.hoangnam', @DemoPasswordHash, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'hoangnam.doctor@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'doctor.thuha')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'doctor.thuha', @DemoPasswordHash, N'DOCTOR', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'thuha.doctor@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'patient.vana')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'patient.vana', @DemoPasswordHash, N'PATIENT', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'van.a.patient@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'patient.thib')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'patient.thib', @DemoPasswordHash, N'PATIENT', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'thi.b.patient@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'admin.demo')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status, created_at, updated_at, last_login_at)
        SELECT user_id, N'admin.demo', @DemoPasswordHash, N'ADMIN', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users WHERE email = N'admin@test.local';

    /* ================================================================
       3. SPECIALTIES
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = 'Nội tổng quát')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Nội tổng quát', N'Khám và điều trị bệnh nội khoa tổng quát.', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = 'Tim mạch')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Tim mạch', N'Khám và điều trị các bệnh lý tim mạch.', 1);

    IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = 'Da liễu')
        INSERT INTO dbo.specialties (name, description, status)
        VALUES (N'Da liễu', N'Khám và điều trị bệnh về da.', 1);

    /* ================================================================
       4. DOCTORS, PATIENTS, ADMINS
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC001')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC001', u.user_id, s.specialty_id, N'LIC-DOC-001',
               N'Bác sĩ chuyên khoa I', 10, N'Bác sĩ Nội tổng quát.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'minhanh.doctor@test.local'
          AND s.name = 'Nội tổng quát';

    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC002')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC002', u.user_id, s.specialty_id, N'LIC-DOC-002',
               N'Thạc sĩ Y khoa', 15, N'Bác sĩ chuyên khoa Tim mạch.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'hoangnam.doctor@test.local'
          AND s.name = 'Tim mạch';

    IF NOT EXISTS (SELECT 1 FROM dbo.doctors WHERE doctor_code = N'DOC003')
        INSERT INTO dbo.doctors
            (doctor_code, user_id, specialty_id, license_number,
             qualification, experience_years, description)
        SELECT N'DOC003', u.user_id, s.specialty_id, N'LIC-DOC-003',
               N'Bác sĩ chuyên khoa I', 8, N'Bác sĩ chuyên khoa Da liễu.'
        FROM dbo.users u CROSS JOIN dbo.specialties s
        WHERE u.email = N'thuha.doctor@test.local'
          AND s.name = 'Da liễu';

    IF NOT EXISTS (SELECT 1 FROM dbo.patients WHERE patient_code = N'PAT001')
        INSERT INTO dbo.patients (patient_code, user_id)
        SELECT N'PAT001', user_id FROM dbo.users
        WHERE email = N'van.a.patient@test.local';

    IF NOT EXISTS (SELECT 1 FROM dbo.patients WHERE patient_code = N'PAT002')
        INSERT INTO dbo.patients (patient_code, user_id)
        SELECT N'PAT002', user_id FROM dbo.users
        WHERE email = N'thi.b.patient@test.local';

    IF NOT EXISTS (
        SELECT 1 FROM dbo.admins a
        JOIN dbo.users u ON u.user_id = a.user_id
        WHERE u.email = N'admin@test.local'
    )
        INSERT INTO dbo.admins (user_id)
        SELECT user_id FROM dbo.users WHERE email = N'admin@test.local';

    /* ================================================================
       5. DOCTOR SCHEDULES
       Mỗi dòng là một CA LÀM VIỆC của bác sĩ, không phải một lịch hẹn.
       Ca sáng: 08:00-12:00; ca chiều: 13:30-17:00.
       ================================================================ */
    DECLARE @Doctor1 BIGINT = (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC001');
    DECLARE @Doctor2 BIGINT = (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC002');
    DECLARE @Doctor3 BIGINT = (SELECT doctor_id FROM dbo.doctors WHERE doctor_code = N'DOC003');

    IF @Doctor1 IS NULL OR @Doctor2 IS NULL OR @Doctor3 IS NULL
        THROW 50002, N'Không tạo được bác sĩ. Hãy kiểm tra dữ liệu users và specialties.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor1 AND work_date = '2026-09-15' AND start_time = '07:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor1, '2026-09-15', '07:00', '11:30', N'Phòng khám 101', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor1 AND work_date = '2026-09-16' AND start_time = '13:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor1, '2026-09-16', '13:00', '17:00', N'Phòng khám 102', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor1 AND work_date = '2026-09-18' AND start_time = '07:30')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor1, '2026-09-18', '07:30', '11:30', N'Phòng khám 101', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor2 AND work_date = '2026-09-14' AND start_time = '08:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor2, '2026-09-14', '08:00', '12:00', N'Phòng khám 201', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor2 AND work_date = '2026-09-17' AND start_time = '13:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor2, '2026-09-17', '13:00', '17:00', N'Phòng khám 202', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor3 AND work_date = '2026-09-16' AND start_time = '08:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor3, '2026-09-16', '08:00', '12:00', N'Phòng khám 301', SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.doctor_schedules WHERE doctor_id = @Doctor3 AND work_date = '2026-09-19' AND start_time = '08:00')
        INSERT INTO dbo.doctor_schedules
            (doctor_id, work_date, start_time, end_time, room, created_at)
        VALUES (@Doctor3, '2026-09-19', '08:00', '11:30', N'Phòng khám 301', SYSDATETIME());

    /* ================================================================
       6. APPOINTMENTS
       ================================================================ */
    DECLARE @Patient1 BIGINT = (SELECT patient_id FROM dbo.patients WHERE patient_code = N'PAT001');
    DECLARE @Patient2 BIGINT = (SELECT patient_id FROM dbo.patients WHERE patient_code = N'PAT002');
    DECLARE @Schedule1 BIGINT = (
        SELECT schedule_id FROM dbo.doctor_schedules
        WHERE doctor_id = @Doctor1 AND work_date = '2026-09-15' AND start_time = '07:00'
    );
    DECLARE @Schedule2 BIGINT = (
        SELECT schedule_id FROM dbo.doctor_schedules
        WHERE doctor_id = @Doctor2 AND work_date = '2026-09-14' AND start_time = '08:00'
    );
    DECLARE @Schedule3 BIGINT = (
        SELECT schedule_id FROM dbo.doctor_schedules
        WHERE doctor_id = @Doctor3 AND work_date = '2026-09-16' AND start_time = '08:00'
    );

    IF NOT EXISTS (SELECT 1 FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-001')
        INSERT INTO dbo.appointments
            (appointment_code, patient_id, schedule_id, reason, fee, status, created_at, updated_at)
        VALUES
            (N'APP-DEMO-001', @Patient1, @Schedule1, N'Đau đầu và mệt mỏi.', 300000, N'CONFIRMED', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-002')
        INSERT INTO dbo.appointments
            (appointment_code, patient_id, schedule_id, reason, fee, status, created_at, updated_at)
        VALUES
            (N'APP-DEMO-002', @Patient2, @Schedule2, N'Khám tim định kỳ.', 500000, N'COMPLETED', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-003')
        INSERT INTO dbo.appointments
            (appointment_code, patient_id, schedule_id, reason, fee, status, created_at, updated_at)
        VALUES
            (N'APP-DEMO-003', @Patient1, @Schedule3, N'Khám da dị ứng.', 350000, N'PENDING', SYSDATETIME(), SYSDATETIME());

    /* ================================================================
       7. PAYMENTS
       ================================================================ */
    IF NOT EXISTS (SELECT 1 FROM dbo.payments WHERE transaction_code = N'TXN-DEMO-001')
        INSERT INTO dbo.payments
            (transaction_code, appointment_id, amount, method, status, created_at, paid_at, note)
        SELECT N'TXN-DEMO-001', appointment_id, 300000, N'CASH', N'PENDING',
               SYSDATETIME(), NULL, N'Thanh toán tại quầy.'
        FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-001';

    IF NOT EXISTS (SELECT 1 FROM dbo.payments WHERE transaction_code = N'TXN-DEMO-002')
        INSERT INTO dbo.payments
            (transaction_code, appointment_id, amount, method, status, created_at, paid_at, note)
        SELECT N'TXN-DEMO-002', appointment_id, 500000, N'BANK_TRANSFER', N'PAID',
               SYSDATETIME(), SYSDATETIME(), N'Đã thanh toán chuyển khoản.'
        FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-002';

    IF NOT EXISTS (SELECT 1 FROM dbo.payments WHERE transaction_code = N'TXN-DEMO-003')
        INSERT INTO dbo.payments
            (transaction_code, appointment_id, amount, method, status, created_at, paid_at, note)
        SELECT N'TXN-DEMO-003', appointment_id, 350000, N'MOMO', N'PENDING',
               SYSDATETIME(), NULL, N'Chờ thanh toán qua ví điện tử.'
        FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-003';

    /* ================================================================
       8. MEDICAL RECORDS
       ================================================================ */
    IF NOT EXISTS (
        SELECT 1 FROM dbo.medical_records mr
        JOIN dbo.appointments a ON a.appointment_id = mr.appointment_id
        WHERE a.appointment_code = N'APP-DEMO-002'
    )
        INSERT INTO dbo.medical_records
            (appointment_id, diagnosis, clinical_note, treatment,
             prescription, status, created_at, updated_at)
        SELECT appointment_id,
               N'Huyết áp cao nhẹ',
               N'Theo dõi huyết áp trong 14 ngày.',
               N'Điều chỉnh chế độ ăn và nghỉ ngơi.',
               N'Uống thuốc theo đơn của bác sĩ.',
               N'COMPLETED', SYSDATETIME(), SYSDATETIME()
        FROM dbo.appointments WHERE appointment_code = N'APP-DEMO-002';

    COMMIT TRANSACTION;

    SELECT N'Đã nạp dữ liệu kiểm thử thành công.' AS result;
    SELECT doctor_id, doctor_code FROM dbo.doctors ORDER BY doctor_id;
    SELECT schedule_id, doctor_id, work_date, start_time, end_time, room
    FROM dbo.doctor_schedules ORDER BY doctor_id, work_date, start_time;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
