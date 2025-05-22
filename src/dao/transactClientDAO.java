package dao;

import dbhandler.DBConnection;
import models.TransactClient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // Update
    public boolean updateTransaction(TransactClient tc) {
        String sql = "UPDATE Transact_Client SET TotalBills = ?, Receipt = ? WHERE DoctorID = ? AND ClientID = ? AND TransactionDate = ? AND TransactionTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tc.getTotalBills());
            ps.setString(2, tc.getReceipt());
            ps.setString(3, tc.getDoctorID());
            ps.setString(4, tc.getClientID());
            ps.setDate(5, Date.valueOf(tc.getTransactionDate()));
            ps.setTime(6, Time.valueOf(tc.getTransactionTime()));
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
