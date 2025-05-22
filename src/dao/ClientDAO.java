package dao;


import dbhandler.DBConnection;
import models.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                Client client = new Client(
                    rs.getString("ClientID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("Address"),
                    rs.getString("ContactInfo"),
                    rs.getString("Bills")
                );
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
                    return new Client(
                        rs.getString("ClientID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Address"),
                        rs.getString("ContactInfo"),
                        rs.getString("Bills")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean updateClient(Client client) {
        String sql = "UPDATE Client SET FirstName = ?, LastName = ?, Address = ?, ContactInfo = ?, Bills = ? WHERE ClientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getLastName());
            ps.setString(3, client.getAddress());
            ps.setString(4, client.getContactInfo());
            ps.setString(5, client.getBills());
            ps.setString(6, client.getClientID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deleteClient(String clientID) {
        String sql = "DELETE FROM Client WHERE ClientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
