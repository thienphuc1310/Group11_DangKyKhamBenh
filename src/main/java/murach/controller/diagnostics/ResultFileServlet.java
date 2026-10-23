package murach.controller.diagnostics;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import murach.DAO.ResultFileDB;
import murach.business.*;
import murach.controller.clinical.ClinicalWeb;

@MultipartConfig(maxFileSize = 10485760, maxRequestSize = 11534336, fileSizeThreshold = 1048576)
public class ResultFileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final ResultFileDB fileDB = new ResultFileDB();

    private Path root()
    {
        String configured = System.getProperty("ucare.resultUploadDir", getServletContext().getInitParameter("resultUploadDir"));
        Path path = configured == null || configured.trim().isEmpty()
                ? Paths.get(System.getProperty("java.io.tmpdir"), "ucare-results") : Paths.get(configured);
        if (!path.isAbsolute()) throw new IllegalStateException("Thư mục kết quả phải là đường dẫn tuyệt đối ngoài webroot");
        Path normalized = path.normalize();
        String webroot = getServletContext().getRealPath("/");
        if (webroot != null && normalized.startsWith(Paths.get(webroot).toAbsolutePath().normalize()))
            throw new IllegalStateException("Không lưu kết quả xét nghiệm trong webroot");
        return normalized;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR, Account.Role.TECHNICIAN, Account.Role.PATIENT);
            if (userId == null) return;
            ResultFile file = fileDB.findAuthorized(ClinicalWeb.id(request, "fileId"), userId, ClinicalWeb.role(request));
            if (file == null) { response.sendError(404); return; }
            if (!file.getStorageKey().matches("[0-9a-f-]{36}\\.(png|jpg|pdf)")) throw new IllegalStateException("Khóa lưu trữ không hợp lệ");
            Path path = root().resolve(file.getStorageKey()).normalize();
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) { response.sendError(404, "Không tìm thấy tệp kết quả"); return; }
            response.setContentType(file.getContentType());
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Security-Policy", "default-src 'none'; sandbox");
            String encoded = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setHeader("Content-Disposition", "inline; filename=\"result\"; filename*=UTF-8''" + encoded);
            response.setContentLengthLong(Files.size(path));
            Files.copy(path, response.getOutputStream());
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Path created = null;
        boolean saved = false;
        Part part = null;
        try
        {
            Long userId = ClinicalWeb.requireUser(request, response, Account.Role.DOCTOR, Account.Role.TECHNICIAN);
            if (userId == null) return;
            ClinicalWeb.requireCsrf(request);
            Long itemId = ClinicalWeb.id(request, "itemId");
            // Kiểm tra quyền trước khi ghi tệp; DAO kiểm tra lại trong transaction lưu metadata
            DiagnosticOrderItem item = new murach.DAO.DiagnosticOrderItemDB().findForPerformer(itemId, userId);
            Long resultId = ClinicalWeb.id(request, "resultId");
            if (item == null || item.getResult() == null || !resultId.equals(item.getResult().getResultId()) ||
                    item.getResult().getStatus() != DiagnosticResult.Status.DRAFT)
                throw new IllegalStateException("Cần lưu kết quả nháp trước khi tải tệp");
            part = request.getPart("resultFile");
            if (part == null || part.getSize() < 1 || part.getSize() > 10485760)
                throw new IllegalArgumentException("Chọn ảnh PNG/JPEG hoặc PDF, tối đa 10 MB");
            byte[] header;
            try (InputStream input = part.getInputStream()) { header = input.readNBytes(8); }
            String extension;
            String contentType;
            if (header.length >= 8 && header[0] == (byte) 137 && header[1] == 80 && header[2] == 78 &&
                    header[3] == 71 && header[4] == 13 && header[5] == 10 && header[6] == 26 && header[7] == 10)
            { extension = ".png"; contentType = "image/png"; }
            else if (header.length >= 3 && header[0] == (byte) 255 && header[1] == (byte) 216 && header[2] == (byte) 255)
            { extension = ".jpg"; contentType = "image/jpeg"; }
            else if (header.length >= 5 && new String(header, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"))
            { extension = ".pdf"; contentType = "application/pdf"; }
            else throw new IllegalArgumentException("Chỉ chấp nhận nội dung ảnh PNG/JPEG hoặc PDF");
            String original = part.getSubmittedFileName();
            if (original == null) original = "ket-qua" + extension;
            original = original.replace('\\', '/');
            original = original.substring(original.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
            if (original.trim().isEmpty()) original = "ket-qua" + extension;
            if (original.length() > 200) original = original.substring(original.length() - 200);
            String storageKey = UUID.randomUUID() + extension;
            Path directory = root();
            Files.createDirectories(directory);
            created = directory.resolve(storageKey);
            try (InputStream input = part.getInputStream()) { Files.copy(input, created); }
            fileDB.insert(resultId, userId, original, storageKey, contentType, part.getSize());
            saved = true;
            response.sendRedirect(request.getContextPath() + "/diagnostics/result?itemId=" + itemId);
        }
        catch (RuntimeException e) { ClinicalWeb.fail(response, e); }
        finally
        {
            // Chỉ xóa tệp UUID vừa tạo nếu metadata chưa lưu thành công
            if (!saved && created != null) Files.deleteIfExists(created);
            if (part != null) part.delete();
        }
    }
}
