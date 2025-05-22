package app;

import dbhandler.DBConnection;
import java.sql.Connection;

public class test1 {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("MySQL connection successful!");
        } else {
            System.err.println("Failed to connect.");
        }
    }
}