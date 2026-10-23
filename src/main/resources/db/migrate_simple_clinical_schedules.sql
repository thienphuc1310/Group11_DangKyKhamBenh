/* Chạy thủ công sau khi sao lưu database và dừng ứng dụng cũ.
   Mô hình mới chỉ dùng khoa + diagnostic_services.service_type.
   Không dùng technician_services hoặc technical_fields nữa.
   Script xóa dữ liệu phân công/lĩnh vực cũ; không xóa nhân sự, dịch vụ, chỉ định, kết quả.
   Kiểm tra tên database trước khi chạy. Không chạy các migration lĩnh vực/phân công cũ sau script này. */
USE [QuanLyDangKyKhamBenh3];
GO

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.nurses', N'U') IS NULL OR OBJECT_ID(N'dbo.technicians', N'U') IS NULL
        THROW 50020, N'Cần có bảng nurses và technicians trước khi tạo lịch làm việc.', 1;

    IF OBJECT_ID(N'dbo.nurse_schedules', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.nurse_schedules
        (
            schedule_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            nurse_id BIGINT NOT NULL,
            work_date DATE NOT NULL,
            start_time TIME NOT NULL,
            end_time TIME NOT NULL,
            room NVARCHAR(100) NOT NULL,
            CONSTRAINT FK_nurse_schedules_nurse FOREIGN KEY (nurse_id) REFERENCES dbo.nurses(nurse_id),
            CONSTRAINT CK_nurse_schedules_time CHECK (end_time > start_time)
        );
        CREATE INDEX IX_nurse_schedules_worker_date ON dbo.nurse_schedules(nurse_id, work_date);
    END;

    IF OBJECT_ID(N'dbo.technician_schedules', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.technician_schedules
        (
            schedule_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            technician_id BIGINT NOT NULL,
            work_date DATE NOT NULL,
            start_time TIME NOT NULL,
            end_time TIME NOT NULL,
            room NVARCHAR(100) NOT NULL,
            CONSTRAINT FK_technician_schedules_technician FOREIGN KEY (technician_id) REFERENCES dbo.technicians(technician_id),
            CONSTRAINT CK_technician_schedules_time CHECK (end_time > start_time)
        );
        CREATE INDEX IX_technician_schedules_worker_date ON dbo.technician_schedules(technician_id, work_date);
    END;

    -- Đúng các cột đã gỡ khỏi Entity; không chạm specialty_id của Doctor/Technician/DiagnosticService.
    DECLARE @obsoleteColumns TABLE (table_name SYSNAME, column_name SYSNAME);
    INSERT INTO @obsoleteColumns VALUES
        (N'diagnostic_services', N'technical_field_id'),
        (N'diagnostic_order_items', N'required_technical_field_id'),
        (N'technicians', N'technical_field_id'),
        (N'nurses', N'specialty_id');

    DECLARE @tableName SYSNAME, @columnName SYSNAME, @objectId INT, @columnId INT, @sql NVARCHAR(MAX);
    DECLARE obsolete_cursor CURSOR LOCAL FAST_FORWARD FOR SELECT table_name, column_name FROM @obsoleteColumns;
    OPEN obsolete_cursor;
    FETCH NEXT FROM obsolete_cursor INTO @tableName, @columnName;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @objectId = OBJECT_ID(N'dbo.' + QUOTENAME(@tableName), N'U');
        SET @columnId = COLUMNPROPERTY(@objectId, @columnName, 'ColumnId');
        IF @columnId IS NOT NULL
        BEGIN
            -- Không tự xóa index/khóa ghép do người dùng tạo riêng.
            IF EXISTS (SELECT 1 FROM sys.index_columns WHERE object_id = @objectId AND column_id = @columnId)
                THROW 50021, N'Cột cần bỏ đang thuộc index. Kiểm tra index thủ công; toàn bộ script sẽ rollback.', 1;
            IF EXISTS (SELECT 1 FROM sys.foreign_key_columns fkc WHERE fkc.parent_object_id = @objectId AND fkc.parent_column_id = @columnId
                AND (SELECT COUNT(*) FROM sys.foreign_key_columns c WHERE c.constraint_object_id = fkc.constraint_object_id) > 1)
                THROW 50022, N'Cột cần bỏ thuộc khóa ngoại ghép. Cần kiểm tra thủ công.', 1;

            SET @sql = N'';
            SELECT @sql = @sql + N'ALTER TABLE dbo.' + QUOTENAME(@tableName) + N' DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';'
            FROM sys.foreign_keys fk JOIN sys.foreign_key_columns c ON c.constraint_object_id = fk.object_id
            WHERE c.parent_object_id = @objectId AND c.parent_column_id = @columnId;
            SELECT @sql = @sql + N'ALTER TABLE dbo.' + QUOTENAME(@tableName) + N' DROP CONSTRAINT ' + QUOTENAME(dc.name) + N';'
            FROM sys.default_constraints dc WHERE dc.parent_object_id = @objectId AND dc.parent_column_id = @columnId;
            SET @sql = @sql + N'ALTER TABLE dbo.' + QUOTENAME(@tableName) + N' DROP COLUMN ' + QUOTENAME(@columnName) + N';';
            EXEC sys.sp_executesql @sql;
        END;
        FETCH NEXT FROM obsolete_cursor INTO @tableName, @columnName;
    END;
    CLOSE obsolete_cursor;
    DEALLOCATE obsolete_cursor;

    IF OBJECT_ID(N'dbo.technician_services', N'U') IS NOT NULL DROP TABLE dbo.technician_services;
    IF OBJECT_ID(N'dbo.technical_fields', N'U') IS NOT NULL DROP TABLE dbo.technical_fields;

    COMMIT TRANSACTION;
    PRINT N'Đã chuyển sang loại dịch vụ + khoa và tạo lịch điều dưỡng/chuyên viên. Hãy phân lịch trong admin.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
