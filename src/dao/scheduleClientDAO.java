package dao;

import dbhandler.DBConnection;
import models.ScheduleClient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class scheduleClientDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addAppointment(ScheduleClient sc) {
        String sql = "INSERT INTO Schedule_Client (DoctorID, ClientID, AppointmentType, AppointmentDate, AppointmentTime, Status, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sc.getDoctorID());
            ps.setString(2, sc.getClientID());
            ps.setString(3, sc.getAppointmentType());
            ps.setDate(4, Date.valueOf(sc.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(sc.getAppointmentTime()));
            ps.setString(6, sc.getStatus());
            ps.setString(7, sc.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<ScheduleClient> getAllAppointments() {
        List<ScheduleClient> list = new ArrayList<>();
        String sql = "SELECT * FROM Schedule_Client";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ScheduleClient sc = new ScheduleClient(
                    rs.getString("DoctorID"),
                    rs.getString("ClientID"),
                    rs.getString("AppointmentType"),
                    rs.getDate("AppointmentDate").toLocalDate(),
                    rs.getTime("AppointmentTime").toLocalTime(),
                    rs.getString("Status"),
                    rs.getString("Remarks")
                );
                list.add(sc);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one by composite key
    public ScheduleClient getAppointment(String doctorID, String clientID, LocalDate date, LocalTime time) {
        String sql = "SELECT * FROM Schedule_Client WHERE DoctorID = ? AND ClientID = ? AND AppointmentDate = ? AND AppointmentTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorID);
            ps.setString(2, clientID);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ScheduleClient(
                        rs.getString("DoctorID"),
                        rs.getString("ClientID"),
                        rs.getString("AppointmentType"),
                        rs.getDate("AppointmentDate").toLocalDate(),
                        rs.getTime("AppointmentTime").toLocalTime(),
                        rs.getString("Status"),
                        rs.getString("Remarks")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updateAppointment(ScheduleClient sc) {
        String sql = "UPDATE Schedule_Client SET AppointmentType = ?, Status = ?, Remarks = ? WHERE DoctorID = ? AND ClientID = ? AND AppointmentDate = ? AND AppointmentTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sc.getAppointmentType());
            ps.setString(2, sc.getStatus());
            ps.setString(3, sc.getRemarks());
            ps.setString(4, sc.getDoctorID());
            ps.setString(5, sc.getClientID());
            ps.setDate(6, Date.valueOf(sc.getAppointmentDate()));
            ps.setTime(7, Time.valueOf(sc.getAppointmentTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointment(ScheduleClient original, ScheduleClient updated) {
        String sql = "UPDATE Schedule_Client SET DoctorID = ?, ClientID = ?, AppointmentType = ?, AppointmentDate = ?, AppointmentTime = ?, Status = ?, Remarks = ? " +
                     "WHERE DoctorID = ? AND ClientID = ? AND AppointmentDate = ? AND AppointmentTime = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getDoctorID());
            ps.setString(2, updated.getClientID());
            ps.setString(3, updated.getAppointmentType());
            ps.setDate(4, Date.valueOf(updated.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(updated.getAppointmentTime()));
            ps.setString(6, updated.getStatus());
            ps.setString(7, updated.getRemarks());

            ps.setString(8, original.getDoctorID());
            ps.setString(9, original.getClientID());
            ps.setDate(10, Date.valueOf(original.getAppointmentDate()));
            ps.setTime(11, Time.valueOf(original.getAppointmentTime()));

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteAppointment(String doctorID, String clientID, LocalDate date, LocalTime time) {
        String sql = "DELETE FROM Schedule_Client WHERE DoctorID = ? AND ClientID = ? AND AppointmentDate = ? AND AppointmentTime = ?";
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

    public List<ScheduleClient> searchAppointments(String column, String keyword) {
        List<ScheduleClient> list = new ArrayList<>();
        String sql = "SELECT * FROM Schedule_Client WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ScheduleClient(
                            rs.getString("DoctorID"),
                            rs.getString("ClientID"),
                            rs.getString("AppointmentType"),
                            rs.getDate("AppointmentDate").toLocalDate(),
                            rs.getTime("AppointmentTime").toLocalTime(),
                            rs.getString("Status"),
                            rs.getString("Remarks")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<ScheduleClient> getAllAppointmentsSorted(String column, String order) {
        List<ScheduleClient> list = new ArrayList<>();

        // Whitelist to prevent SQL injection
        List<String> validColumns = List.of("DoctorID", "ClientID", "AppointmentType", "AppointmentDate", "AppointmentTime", "Status", "Remarks");
        List<String> validOrders = List.of("ASC", "DESC");

        if (!validColumns.contains(column)) column = "AppointmentDate";
        if (!validOrders.contains(order.toUpperCase())) order = "ASC";

        String sql = "SELECT * FROM Schedule_Client ORDER BY " + column + " " + order;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ScheduleClient sc = new ScheduleClient(
                        rs.getString("DoctorID"),
                        rs.getString("ClientID"),
                        rs.getString("AppointmentType"),
                        rs.getDate("AppointmentDate").toLocalDate(),
                        rs.getTime("AppointmentTime").toLocalTime(),
                        rs.getString("Status"),
                        rs.getString("Remarks")
                );
                list.add(sc);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
