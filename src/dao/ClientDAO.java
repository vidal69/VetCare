package dao;


import dbhandler.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Client;

public class ClientDAO {
    private final Connection conn = DBConnection.getConnection();

    // Create
    public boolean addClient(Client client) {
        String sql = "INSERT INTO Client (ClientID, FirstName, LastName, Address, ContactInfo, Bills) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getClientID());
            ps.setString(2, client.getFirstName());
            ps.setString(3, client.getLastName());
            ps.setString(4, client.getAddress());
            ps.setString(5, client.getContactInfo());
            ps.setString(6, client.getBills());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Read all
    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM Client";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client client = extractClient(rs);
                list.add(client);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Read one
    public Client getClientByID(String clientID) {
        String sql = "SELECT * FROM Client WHERE ClientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractClient(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updateClient(Client original, Client updated) {
        String sql = "UPDATE Client SET ClientID = ?, FirstName = ?, LastName = ?, Address = ?, ContactInfo = ?, Bills = ? WHERE ClientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getClientID());
            ps.setString(2, updated.getFirstName());
            ps.setString(3, updated.getLastName());
            ps.setString(4, updated.getAddress());
            ps.setString(5, updated.getContactInfo());
            ps.setString(6, updated.getBills());
            ps.setString(7, original.getClientID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteClient(String clientID) {
        String nullify = "UPDATE Patient SET ClientID = NULL WHERE ClientID = ?";
        String delete   = "DELETE FROM Client WHERE ClientID = ?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(nullify)) {
                p1.setString(1, clientID);
                p1.executeUpdate();
            }
            boolean ok;
            try (PreparedStatement p2 = conn.prepareStatement(delete)) {
                p2.setString(1, clientID);
                ok = p2.executeUpdate() > 0;
            }
            conn.commit();
            return ok;
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            ex.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    public boolean hasAppointment(String clientID){
        String sql = "SELECT 1 FROM schedule_client WHERE ClientID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, clientID);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next(); 
            }
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasTransaction(String clientID){
        String sql = "SELECT 1 FROM transact_client WHERE ClientID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, clientID);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next(); 
            }
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean hasPet(String clientID){
        String sql = "SELECT 1 FROM patient WHERE ClientID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, clientID);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next(); 
            }
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    

    /**
     * Search clients by a given column and keyword.
     */
    public List<Client> searchClients(String column, String keyword) {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM Client WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Client c = extractClient(rs);
                    list.add(c);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private Client extractClient(ResultSet rs) throws SQLException {
        return new Client(
            rs.getString("ClientID"),
            rs.getString("FirstName"),
            rs.getString("LastName"),
            rs.getString("Address"),
            rs.getString("ContactInfo"),
            rs.getString("Bills")
        );
    }

    public List<Client> getAllClientsSorted(String column, String order) {
        List<Client> list = new ArrayList<>();

        // Whitelist to prevent SQL injection
        List<String> validColumns = List.of("ClientID", "FirstName", "LastName", "Address", "ContactInfo", "Bills");
        List<String> validOrders = List.of("ASC", "DESC");

        if (!validColumns.contains(column)) column = "ClientID";
        if (!validOrders.contains(order.toUpperCase())) order = "ASC";

        String sql = "SELECT * FROM Client ORDER BY " + column + " " + order;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractClient(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
