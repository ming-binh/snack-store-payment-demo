package util;

/**
 * Author: HE190438 Thân Bình Minh 
 * Created: 2026-03-19
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String HOST = "localhost";
    private static final String PORT = "1433";
    private static final String DATABASE = "snack_store";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    private static final String URL
            = "jdbc:sqlserver://" + HOST + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=false"
            + ";trustServerCertificate=true";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("SQL Server JDBC driver not on classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
