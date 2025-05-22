package dao;

import dbhandler.DBConnection;
import models.AttendPatient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class attendPatientDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addAttendPatient(AttendPatient ap) {
        String sql = "INSERT INTO Attend_Patient (DoctorID, PatientID, ServeDate, ServeTime, ServiceType) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ap.getDoctorID());
            ps.setString(2, ap.getPatientID());
            ps.setDate(3, Date.valueOf(ap.getServeDate()));
            ps.setTime(4, Time.valueOf(ap.getServeTime()));
            ps.setString(5, ap.getServiceType());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<AttendPatient> getAllAttendPatients() {
        List<AttendPatient> list = new ArrayList<>();
        String sql = "SELECT * FROM Attend_Patient";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                AttendPatient ap = new AttendPatient(
                    rs.getString("DoctorID"),
                    rs.getString("PatientID"),
                    rs.getDate("ServeDate").toLocalDate(),
                    rs.getTime("ServeTime").toLocalTime(),
                    rs.getString("ServiceType")
                );
                list.add(ap);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one by composite key
    public AttendPatient getAttendPatient(String doctorID, String patientID, LocalDate serveDate, LocalTime serveTime) {
        String sql = "SELECT * FROM Attend_Patient WHERE DoctorID = ? AND PatientID = ? AND ServeDate = ? AND ServeTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            ps.setString(2, patientID);
            ps.setDate(3, Date.valueOf(serveDate));
            ps.setTime(4, Time.valueOf(serveTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AttendPatient(
                        rs.getString("DoctorID"),
                        rs.getString("PatientID"),
                        rs.getDate("ServeDate").toLocalDate(),
                        rs.getTime("ServeTime").toLocalTime(),
                        rs.getString("ServiceType")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updateAttendPatient(AttendPatient ap) {
        String sql = "UPDATE Attend_Patient SET ServiceType = ? WHERE DoctorID = ? AND PatientID = ? AND ServeDate = ? AND ServeTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ap.getServiceType());
            ps.setString(2, ap.getDoctorID());
            ps.setString(3, ap.getPatientID());
            ps.setDate(4, Date.valueOf(ap.getServeDate()));
            ps.setTime(5, Time.valueOf(ap.getServeTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteAttendPatient(String doctorID, String patientID, LocalDate serveDate, LocalTime serveTime) {
        String sql = "DELETE FROM Attend_Patient WHERE DoctorID = ? AND PatientID = ? AND ServeDate = ? AND ServeTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            ps.setString(2, patientID);
            ps.setDate(3, Date.valueOf(serveDate));
            ps.setTime(4, Time.valueOf(serveTime));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
