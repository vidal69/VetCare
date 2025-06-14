package dao;

import dbhandler.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Doctor;

public class DoctorDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addDoctor(Doctor doctor) {
        String sql = "INSERT INTO Doctor (DoctorID, FirstName, LastName, DateOfBirth) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getDoctorID());
            ps.setString(2, doctor.getFirstName());
            ps.setString(3, doctor.getLastName());
            ps.setDate(4, Date.valueOf(doctor.getDateOfBirth()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM Doctor";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Doctor d = new Doctor(
                    rs.getString("DoctorID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getDate("DateOfBirth").toLocalDate()
                );
                list.add(d);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one by ID
    public Doctor getDoctorByID(String doctorID) {
        String sql = "SELECT * FROM Doctor WHERE DoctorID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                        rs.getString("DoctorID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getDate("DateOfBirth").toLocalDate()
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updateDoctor(Doctor original, Doctor updated) {
        String sql = "UPDATE Doctor SET DoctorID = ?, FirstName = ?, LastName = ?, DateOfBirth = ? WHERE DoctorID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getDoctorID());
            ps.setString(2, updated.getFirstName());
            ps.setString(3, updated.getLastName());
            ps.setDate(4, Date.valueOf(updated.getDateOfBirth()));
            ps.setString(5, original.getDoctorID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteDoctor(String doctorID) {
        String sql = "DELETE FROM Doctor WHERE DoctorID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasAppointment(String doctorID){
        String sql = "SELECT 1 FROM schedule_client WHERE DoctorID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, doctorID);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next(); 
            }
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasTransaction(String doctorID) {
        String sql = "SELECT 1 FROM transact_client WHERE DoctorID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if any transaction exists
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Search doctors by a given column (DoctorID, FirstName, LastName) and keyword.
     */
    public List<Doctor> searchDoctors(String column, String keyword) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM Doctor WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Doctor(
                        rs.getString("DoctorID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getDate("DateOfBirth").toLocalDate()
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Doctor> getAllDoctorsSorted(String column, String order) {
        List<Doctor> list = new ArrayList<>();

        // Sanitize inputs
        List<String> validColumns = List.of("DoctorID", "FirstName", "LastName", "DateOfBirth");
        List<String> validOrders = List.of("ASC", "DESC");

        if (!validColumns.contains(column)) column = "DoctorID";
        if (!validOrders.contains(order.toUpperCase())) order = "ASC";

        String sql = "SELECT * FROM Doctor ORDER BY " + column + " " + order;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Doctor(
                        rs.getString("DoctorID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getDate("DateOfBirth").toLocalDate()
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
