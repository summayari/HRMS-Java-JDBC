package hrms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String URL =
        "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=HRMS_DB;"
        + "encrypt=false;trustServerCertificate=true;";
    private static String USER     = "sa";
    private static String PASSWORD = "Admin123";

    private static Connection connection = null;

    private DBConnection() {}

    public static void configure(String server, String db, String user, String pass) {
        URL = "jdbc:sqlserver://" + server + ";databaseName=" + db
            + ";encrypt=false;trustServerCertificate=true;";
        USER     = user;
        PASSWORD = pass;
        connection = null; // reset
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver not found! Put mssql-jdbc jar in lib/ folder.\n" + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
