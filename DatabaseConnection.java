import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/tourism_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Krishna@1234";

    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to register MySQL JDBC Driver: " + e.getMessage());
            System.err.println("Please ensure mysql-connector-j-8.1.0.jar is in your classpath");
            throw new RuntimeException(
                    "MySQL JDBC Driver not found. Make sure mysql-connector-j-8.1.0.jar is in your classpath", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting to connect to database at " + URL);
        try {
            // Test if MySQL server is running
            try {
                Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD);
                testConn.close();
            } catch (SQLException e) {
                String error = "MySQL server is not running or not accessible. Please start MySQL server.";
                System.err.println(error);
                throw new SQLException(error, e);
            }

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            if (conn == null) {
                String error = "Failed to establish database connection - connection object is null";
                System.err.println(error);
                throw new SQLException(error);
            }
            // Test the connection
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("USE `tourism_db`");
                System.out.println("Successfully connected to tourism_db database");
            } catch (SQLException e) {
                String error = "Error accessing tourism_db database: " + e.getMessage();
                System.err.println(error);
                throw new SQLException(error, e);
            }
            return conn;
        } catch (SQLException e) {
            String error = "Database connection error. Please verify database is running and credentials are correct";
            System.err.println(error + ": " + e.getMessage());
            throw new SQLException(error, e);
        }
    }

    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            System.out.println("Database connection test successful!");
            conn.close();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}