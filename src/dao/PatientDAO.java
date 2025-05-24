package dao;

import dbhandler.DBConnection;
import models.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO Patient (PatientID, Name, DateOfBirth, Gender, Species, Breed, Remarks, ClientID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getPatientID());
            ps.setString(2, patient.getName());
            ps.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getSpecies());
            ps.setString(6, patient.getBreed());
            ps.setString(7, patient.getRemarks());
            ps.setString(8, patient.getClientID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient(
                    rs.getString("PatientID"),
                    rs.getString("Name"),
                    rs.getDate("DateOfBirth").toLocalDate(),
                    rs.getString("Gender"),
                    rs.getString("Species"),
                    rs.getString("Breed"),
                    rs.getString("Remarks"),
                    rs.getString("ClientID")
                );
                list.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Search patients by a given column and keyword.
     */
    public List<Patient> searchPatients(String column, String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Patient(
                        rs.getString("PatientID"),
                        rs.getString("Name"),
                        rs.getDate("DateOfBirth").toLocalDate(),
                        rs.getString("Gender"),
                        rs.getString("Species"),
                        rs.getString("Breed"),
                        rs.getString("Remarks"),
                        rs.getString("ClientID")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one by ID
    public Patient getPatientByID(String patientID) {
        String sql = "SELECT * FROM Patient WHERE PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getString("PatientID"),
                        rs.getString("Name"),
                        rs.getDate("DateOfBirth").toLocalDate(),
                        rs.getString("Gender"),
                        rs.getString("Species"),
                        rs.getString("Breed"),
                        rs.getString("Remarks"),
                        rs.getString("ClientID")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updatePatient(Patient original, Patient updated) {
        String sql = "UPDATE Patient SET PatientID = ?, Name = ?, DateOfBirth = ?, Gender = ?, Species = ?, Breed = ?, Remarks = ?, ClientID = ? WHERE PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getPatientID());
            ps.setString(2, updated.getName());
            ps.setDate(3, Date.valueOf(updated.getDateOfBirth()));
            ps.setString(4, updated.getGender());
            ps.setString(5, updated.getSpecies());
            ps.setString(6, updated.getBreed());
            ps.setString(7, updated.getRemarks());
            ps.setString(8, updated.getClientID());
            ps.setString(9, original.getPatientID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deletePatient(String patientID) {
        String sql = "DELETE FROM Patient WHERE PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
