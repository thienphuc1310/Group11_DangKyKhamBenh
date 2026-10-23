/* Người 4: thêm 2 khoa để demo phân loại chuyên khoa (ban đầu để CLINICAL,
   sau đó đổi sang PARACLINICAL bằng trang /admin/specialties). */
/* Không tự chạy. Chọn đúng database kiểm thử trong SSMS trước khi thực thi.
   Không cố định tên database theo máy của người 4. */
GO
IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = N'Xét nghiệm')
    INSERT INTO dbo.specialties (name, description, status, specialty_type)
    VALUES (N'Xét nghiệm', N'Khoa xét nghiệm', 1, N'CLINICAL');
IF NOT EXISTS (SELECT 1 FROM dbo.specialties WHERE name = N'Chẩn đoán hình ảnh')
    INSERT INTO dbo.specialties (name, description, status, specialty_type)
    VALUES (N'Chẩn đoán hình ảnh', N'Khoa chẩn đoán hình ảnh', 1, N'CLINICAL');
GO
