package dbhandler;  // adjust if your package is different

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL      = "jdbc:mysql://localhost:3306/vetcare?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "mysql123!";

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to VetCare MySQL!");
            } catch (SQLException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
        return connection;
    }
}