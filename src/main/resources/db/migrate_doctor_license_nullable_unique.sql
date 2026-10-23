/* Sửa lỗi tạo bác sĩ: UNIQUE thường của license_number không cho nhiều NULL.
   Dừng ứng dụng, sao lưu và kiểm tra đúng database trước khi chạy thủ công.
   Không xóa bác sĩ hoặc sửa bất kỳ số giấy phép nào.
   Cần triển khai cùng Doctor.java đã bỏ unique=true trên license_number,
   nếu không Hibernate schema update có thể tạo lại UNIQUE cũ. */
USE [QuanLyDangKyKhamBenh3];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tableId INT = OBJECT_ID(N'dbo.doctors', N'U');
    DECLARE @columnId INT = COLUMNPROPERTY(@tableId, N'license_number', 'ColumnId');
    DECLARE @oldIndexId INT;

    IF @tableId IS NULL OR @columnId IS NULL
        THROW 50050, N'Không tìm thấy dbo.doctors.license_number. Kiểm tra đúng database.', 1;
    IF COLUMNPROPERTY(@tableId, N'license_number', 'AllowsNull') <> 1
        THROW 50051, N'license_number phải cho phép NULL. Kiểm tra schema trước khi chạy.', 1;

    -- Tên lấy từ lỗi và đối chiếu với tên UNIQUE mà Hibernate sinh cho license_number.
    SELECT @oldIndexId = unique_index_id
    FROM sys.key_constraints
    WHERE parent_object_id = @tableId AND type = 'UQ'
      AND name = N'UK_1xu5x0jae737xae254t4rgcd1';

    IF @oldIndexId IS NOT NULL
    BEGIN
        IF (SELECT COUNT(*) FROM sys.index_columns
            WHERE object_id = @tableId AND index_id = @oldIndexId AND key_ordinal > 0) <> 1
           OR NOT EXISTS (SELECT 1 FROM sys.index_columns
                          WHERE object_id = @tableId AND index_id = @oldIndexId
                            AND column_id = @columnId AND key_ordinal = 1)
            THROW 50052, N'Constraint không chỉ áp dụng cho license_number; dừng để tránh sửa nhầm.', 1;
        IF EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE referenced_object_id = @tableId AND key_index_id = @oldIndexId)
            THROW 50053, N'Có khóa ngoại tham chiếu UNIQUE cũ. Cần kiểm tra riêng trước khi thay đổi.', 1;
    END;

    IF EXISTS (SELECT license_number FROM dbo.doctors WHERE license_number IS NOT NULL
               GROUP BY license_number HAVING COUNT(*) > 1)
        THROW 50054, N'Có số giấy phép đã nhập bị trùng. Script không tự sửa hoặc xóa dữ liệu.', 1;

    -- Tạo ràng buộc thay thế trước; lỗi ở bất kỳ bước nào sẽ rollback toàn bộ.
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = @tableId
                   AND name = N'UX_doctors_license_number_not_null')
        CREATE UNIQUE INDEX UX_doctors_license_number_not_null
        ON dbo.doctors(license_number)
        WHERE license_number IS NOT NULL;
    ELSE IF NOT EXISTS (
        SELECT 1 FROM sys.indexes ix
        WHERE ix.object_id = @tableId AND ix.name = N'UX_doctors_license_number_not_null'
          AND ix.is_unique = 1 AND ix.is_disabled = 0 AND ix.has_filter = 1
          AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER(ix.filter_definition),
                    N'[', N''), N']', N''), N'(', N''), N')', N''), N' ', N'') = N'LICENSE_NUMBERISNOTNULL'
          AND (SELECT COUNT(*) FROM sys.index_columns ic WHERE ic.object_id = ix.object_id
               AND ic.index_id = ix.index_id AND ic.key_ordinal > 0) = 1
          AND EXISTS (SELECT 1 FROM sys.index_columns ic WHERE ic.object_id = ix.object_id
                      AND ic.index_id = ix.index_id AND ic.column_id = @columnId AND ic.key_ordinal = 1))
        THROW 50055, N'Index thay thế đã tồn tại nhưng khác định nghĩa mong đợi. Cần kiểm tra thủ công.', 1;

    IF @oldIndexId IS NOT NULL
        ALTER TABLE dbo.doctors DROP CONSTRAINT UK_1xu5x0jae737xae254t4rgcd1;

    -- Không tự xóa các UNIQUE khác do người dùng tạo riêng.
    IF EXISTS (
        SELECT 1 FROM sys.indexes ix
        WHERE ix.object_id = @tableId AND ix.is_unique = 1 AND ix.has_filter = 0
          AND ix.is_disabled = 0 AND ix.is_hypothetical = 0
          AND (SELECT COUNT(*) FROM sys.index_columns ic WHERE ic.object_id = ix.object_id
               AND ic.index_id = ix.index_id AND ic.key_ordinal > 0) = 1
          AND EXISTS (SELECT 1 FROM sys.index_columns ic WHERE ic.object_id = ix.object_id
                      AND ic.index_id = ix.index_id AND ic.column_id = @columnId AND ic.key_ordinal = 1))
        THROW 50056, N'Vẫn còn UNIQUE thường khác trên license_number. Toàn bộ thay đổi sẽ rollback; kiểm tra thủ công.', 1;

    COMMIT TRANSACTION;
    PRINT N'Đã cho phép nhiều license_number NULL; số giấy phép đã nhập vẫn không được trùng.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
