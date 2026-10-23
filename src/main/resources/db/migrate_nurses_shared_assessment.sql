/* Chạy thủ công sau khi sao lưu database và dừng ứng dụng cũ.
   Entity Nurse mới KHÔNG còn specialty_id. Hibernate update không tự xóa cột cũ.
   Script chỉ bỏ phân khoa của điều dưỡng, không xóa Nurse/User/Account/phiếu đánh giá.
   Các giá trị phân khoa cũ trong nurses.specialty_id sẽ bị xóa khi DROP COLUMN.
   Kiểm tra tên database dưới đây trước khi thực hiện. */
USE [QuanLyDangKyKhamBenh3];
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @nursesObjectId INT = OBJECT_ID(N'dbo.nurses', N'U');
    DECLARE @specialtyColumnId INT = COLUMNPROPERTY(@nursesObjectId, N'specialty_id', 'ColumnId');

    IF @specialtyColumnId IS NOT NULL
    BEGIN
        -- Không tự xóa index hoặc quan hệ ghép do người dùng thêm riêng.
        IF EXISTS (SELECT 1 FROM sys.index_columns
                   WHERE object_id = @nursesObjectId AND column_id = @specialtyColumnId)
            THROW 50001, N'specialty_id đang thuộc index. Hãy kiểm tra index trước khi bỏ cột.', 1;

        IF EXISTS (SELECT 1 FROM sys.foreign_key_columns
                   WHERE referenced_object_id = @nursesObjectId AND referenced_column_id = @specialtyColumnId)
            THROW 50002, N'Có bảng khác tham chiếu nurses.specialty_id. Cần kiểm tra thủ công.', 1;

        IF EXISTS (SELECT 1 FROM sys.foreign_key_columns fkc
                   WHERE fkc.parent_object_id = @nursesObjectId AND fkc.parent_column_id = @specialtyColumnId
                   AND (SELECT COUNT(*) FROM sys.foreign_key_columns c
                        WHERE c.constraint_object_id = fkc.constraint_object_id) > 1)
            THROW 50003, N'specialty_id đang thuộc khóa ngoại ghép. Cần kiểm tra thủ công.', 1;

        -- Tên khóa ngoại Hibernate sinh ra có thể khác trên mỗi database.
        DECLARE @dropConstraints NVARCHAR(MAX) = N'';
        SELECT @dropConstraints = @dropConstraints + N'ALTER TABLE dbo.nurses DROP CONSTRAINT '
                + QUOTENAME(fk.name) + N';'
        FROM sys.foreign_keys fk
        JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
        WHERE fkc.parent_object_id = @nursesObjectId AND fkc.parent_column_id = @specialtyColumnId;

        SELECT @dropConstraints = @dropConstraints + N'ALTER TABLE dbo.nurses DROP CONSTRAINT '
                + QUOTENAME(dc.name) + N';'
        FROM sys.default_constraints dc
        WHERE dc.parent_object_id = @nursesObjectId AND dc.parent_column_id = @specialtyColumnId;

        IF LEN(@dropConstraints) > 0
            EXEC sys.sp_executesql @dropConstraints;

        ALTER TABLE dbo.nurses DROP COLUMN specialty_id;
        PRINT N'Đã bỏ phân chuyên khoa của điều dưỡng.';
    END
    ELSE
        PRINT N'Không có nurses.specialty_id; không cần cập nhật.';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
