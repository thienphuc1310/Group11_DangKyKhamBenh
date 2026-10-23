USE [QuanLyDangKyKhamBenh3];
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'staff.test@test.local')
        INSERT INTO dbo.users
            (full_name, phone, email, date_of_birth, gender, address, created_at, updated_at)
        VALUES
            (N'Nhân viên kiểm thử', N'0901888888', N'staff.test@test.local',
             '2000-01-01', N'Khác', N'TP.HCM', SYSDATETIME(), SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts WHERE username = N'staff.test')
        INSERT INTO dbo.accounts
            (user_id, username, password_hash, role, status,
             created_at, updated_at, last_login_at)
        SELECT user_id, N'staff.test', N'Test@123', N'STAFF', N'ACTIVE',
               SYSDATETIME(), SYSDATETIME(), NULL
        FROM dbo.users
        WHERE email = N'staff.test@test.local';

    /* STAFF dùng chung hồ sơ trong bảng admins với ADMIN. */
    IF NOT EXISTS (
        SELECT 1
        FROM dbo.admins a
        JOIN dbo.users u ON u.user_id = a.user_id
        WHERE u.email = N'staff.test@test.local'
    )
        INSERT INTO dbo.admins (user_id)
        SELECT user_id
        FROM dbo.users
        WHERE email = N'staff.test@test.local';

    COMMIT TRANSACTION;

    SELECT N'staff.test' AS username,
           N'Test@123' AS password,
           N'STAFF' AS role;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
