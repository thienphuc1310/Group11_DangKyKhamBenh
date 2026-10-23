/*
 * Chuẩn hóa các cột có thể chứa tiếng Việt sang NVARCHAR.
 * Chạy trên database QuanLyDangKyKhamBenh2 sau khi sao lưu dữ liệu.
 * Các cột mã kỹ thuật, email, hash và enum tiếp tục dùng VARCHAR.
 */

DECLARE @accountsUsernameConstraint SYSNAME;
SELECT TOP (1) @accountsUsernameConstraint = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic
  ON ic.object_id = kc.parent_object_id
 AND ic.index_id = kc.unique_index_id
JOIN sys.columns c
  ON c.object_id = ic.object_id
 AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID(N'dbo.accounts')
  AND kc.type = 'UQ'
  AND c.name = N'username';

IF @accountsUsernameConstraint IS NOT NULL
    EXEC(N'ALTER TABLE dbo.accounts DROP CONSTRAINT '
         + QUOTENAME(@accountsUsernameConstraint));

DECLARE @specialtiesNameConstraint SYSNAME;
SELECT TOP (1) @specialtiesNameConstraint = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic
  ON ic.object_id = kc.parent_object_id
 AND ic.index_id = kc.unique_index_id
JOIN sys.columns c
  ON c.object_id = ic.object_id
 AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID(N'dbo.specialties')
  AND kc.type = 'UQ'
  AND c.name = N'name';

IF @specialtiesNameConstraint IS NOT NULL
    EXEC(N'ALTER TABLE dbo.specialties DROP CONSTRAINT '
         + QUOTENAME(@specialtiesNameConstraint));

ALTER TABLE dbo.users
    ALTER COLUMN full_name NVARCHAR(100) NOT NULL;
ALTER TABLE dbo.users
    ALTER COLUMN gender NVARCHAR(10) NULL;
ALTER TABLE dbo.users
    ALTER COLUMN address NVARCHAR(255) NULL;

-- Loại bỏ NCHAR độ dài cố định gây lỗi bind tham số đăng nhập.
ALTER TABLE dbo.accounts
    ALTER COLUMN username NVARCHAR(255) NOT NULL;

ALTER TABLE dbo.specialties
    ALTER COLUMN name NVARCHAR(255) NOT NULL;
ALTER TABLE dbo.specialties
    ALTER COLUMN description NVARCHAR(MAX) NULL;

ALTER TABLE dbo.doctors
    ALTER COLUMN qualification NVARCHAR(255) NULL;
ALTER TABLE dbo.doctors
    ALTER COLUMN description NVARCHAR(MAX) NULL;

ALTER TABLE dbo.doctor_schedules
    ALTER COLUMN room NVARCHAR(100) NOT NULL;

ALTER TABLE dbo.appointments
    ALTER COLUMN reason NVARCHAR(500) NULL;

ALTER TABLE dbo.payments
    ALTER COLUMN note NVARCHAR(500) NULL;

ALTER TABLE dbo.medical_records
    ALTER COLUMN diagnosis NVARCHAR(MAX) NULL;
ALTER TABLE dbo.medical_records
    ALTER COLUMN clinical_note NVARCHAR(MAX) NULL;
ALTER TABLE dbo.medical_records
    ALTER COLUMN treatment NVARCHAR(MAX) NULL;
ALTER TABLE dbo.medical_records
    ALTER COLUMN prescription NVARCHAR(MAX) NULL;

ALTER TABLE dbo.accounts
    ADD CONSTRAINT UQ_accounts_username UNIQUE (username);
ALTER TABLE dbo.specialties
    ADD CONSTRAINT UQ_specialties_name UNIQUE (name);
