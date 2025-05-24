package dao;

import dbhandler.DBConnection;
import models.TransactClient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class transactClientDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addTransaction(TransactClient tc) {
        String sql = "INSERT INTO Transact_Client (DoctorID, ClientID, TotalBills, Receipt, TransactionDate, TransactionTime) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tc.getDoctorID());
            ps.setString(2, tc.getClientID());
            ps.setString(3, tc.getTotalBills());
            ps.setString(4, tc.getReceipt());
            ps.setDate(5, Date.valueOf(tc.getTransactionDate()));
            ps.setTime(6, Time.valueOf(tc.getTransactionTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<TransactClient> getAllTransactions() {
        List<TransactClient> list = new ArrayList<>();
        String sql = "SELECT * FROM Transact_Client";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                TransactClient tc = new TransactClient(
                    rs.getString("DoctorID"),
                    rs.getString("ClientID"),
                    rs.getString("TotalBills"),
                    rs.getString("Receipt"),
                    rs.getDate("TransactionDate").toLocalDate(),
                    rs.getTime("TransactionTime").toLocalTime()
                );
                list.add(tc);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one by composite key
    public TransactClient getTransaction(String doctorID, String clientID, LocalDate date, LocalTime time) {
        String sql = "SELECT * FROM Transact_Client WHERE DoctorID = ? AND ClientID = ? AND TransactionDate = ? AND TransactionTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            ps.setString(2, clientID);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TransactClient(
                        rs.getString("DoctorID"),
                        rs.getString("ClientID"),
                        rs.getString("TotalBills"),
                        rs.getString("Receipt"),
                        rs.getDate("TransactionDate").toLocalDate(),
                        rs.getTime("TransactionTime").toLocalTime()
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Search transactions by a given column and keyword.
     */
    public List<TransactClient> searchTransactions(String column, String keyword) {
        List<TransactClient> list = new ArrayList<>();
        String sql = "SELECT * FROM Transact_Client WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactClient(
                        rs.getString("DoctorID"),
                        rs.getString("ClientID"),
                        rs.getString("TotalBills"),
                        rs.getString("Receipt"),
                        rs.getDate("TransactionDate").toLocalDate(),
                        rs.getTime("TransactionTime").toLocalTime()
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Update
    public boolean updateTransaction(TransactClient original, TransactClient updated) {
        String sql = "UPDATE Transact_Client SET DoctorID = ?, ClientID = ?, TotalBills = ?, Receipt = ?, TransactionDate = ?, TransactionTime = ? " +
                     "WHERE DoctorID = ? AND ClientID = ? AND TransactionDate = ? AND TransactionTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getDoctorID());
            ps.setString(2, updated.getClientID());
            ps.setString(3, updated.getTotalBills());
            ps.setString(4, updated.getReceipt());
            ps.setDate(5, Date.valueOf(updated.getTransactionDate()));
            ps.setTime(6, Time.valueOf(updated.getTransactionTime()));

            ps.setString(7, original.getDoctorID());
            ps.setString(8, original.getClientID());
            ps.setDate(9, Date.valueOf(original.getTransactionDate()));
            ps.setTime(10, Time.valueOf(original.getTransactionTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteTransaction(String doctorID, String clientID, LocalDate date, LocalTime time) {
        String sql = "DELETE FROM Transact_Client WHERE DoctorID = ? AND ClientID = ? AND TransactionDate = ? AND TransactionTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            ps.setString(2, clientID);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
