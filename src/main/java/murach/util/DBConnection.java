package murach.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String USER = "sa";
    private static final String PASS = "thienphuc1310";

    // 2. Xóa bỏ hoàn toàn integratedSecurity và authenticationScheme
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=QuanLyDangKyKhamBenh;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "characterEncoding=UTF-8;"
                    + "loginTimeout=10;";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Không tìm thấy JDBC driver SQL Server.", e
            );
        }

        // 3. Truyền tài khoản và mật khẩu vào hàm lấy kết nối
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
