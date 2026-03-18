package util;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
/**
 * Thin JDBC helper for SQL Server.
 * Adjust HOST / PORT / DATABASE / USER / PASSWORD to match your environment.
 */
public class DBUtil {
 
    private static final String HOST     = "localhost";
    private static final String PORT     = "1433";
    private static final String DATABASE = "snack_store";   // ← your DB name
    private static final String USER     = "sa";
    private static final String PASSWORD = "123"; // ← change this
 
    private static final String URL =
            "jdbc:sqlserver://" + HOST + ":" + PORT
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