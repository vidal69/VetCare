package app;

import dao.ClientDAO;
import models.Client;
import utils.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.regex.Pattern;

public class ClientManager extends JPanel {
    private ClientDAO dao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;

    // Only letters and spaces for names
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    // Simple address pattern (alphanumeric, spaces, commas, dots)
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[\\w\\s,\\.\\-#]+$");

    public ClientManager() {
        setLayout(new BorderLayout());

        // Table setup
        model = new DefaultTableModel(new Object[]{
            "ClientID", "FirstName", "LastName", "Address", "ContactInfo", "Bills"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);
        add(buttons, BorderLayout.SOUTH);

        // Actions
        btnAdd.addActionListener(e -> showClientDialog(null));
        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                Client c = dao.getClientByID((String) model.getValueAt(i, 0));
                showClientDialog(c);
            } else {
                JOptionPane.showMessageDialog(this, "Select a client to edit.");
            }
        });
        btnDelete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected client?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deleteClient(id);
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a client to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<Client> list = dao.getAllClients();
        for (Client c : list) {
            model.addRow(new Object[]{
                c.getClientID(),
                c.getFirstName(),
                c.getLastName(),
                c.getAddress(),
                c.getContactInfo(),
                c.getBills()
            });
        }
    }

    private void showClientDialog(Client c) {
        JTextField txtID = new JTextField();
        JTextField txtFirst = new JTextField();
        JTextField txtLast = new JTextField();
        JTextField txtAddress = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtBills = new JTextField();

        if (c != null) {
            txtID.setText(c.getClientID());
            txtID.setEditable(false);
            txtFirst.setText(c.getFirstName());
            txtLast.setText(c.getLastName());
            txtAddress.setText(c.getAddress());
            txtContact.setText(c.getContactInfo());
            txtBills.setText(c.getBills());
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Client ID:")); panel.add(txtID);
        panel.add(new JLabel("First Name:")); panel.add(txtFirst);
        panel.add(new JLabel("Last Name:")); panel.add(txtLast);
        panel.add(new JLabel("Address:")); panel.add(txtAddress);
        panel.add(new JLabel("Contact Info:")); panel.add(txtContact);
        panel.add(new JLabel("Bills:")); panel.add(txtBills);

        int result = JOptionPane.showConfirmDialog(this, panel,
            c == null ? "Add Client" : "Edit Client",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Gather and trim values
            String idText = txtID.getText().trim();
            String firstText = txtFirst.getText().trim();
            String lastText = txtLast.getText().trim();
            String addressText = txtAddress.getText().trim();
            String contactText = txtContact.getText().trim();
            String billsText = txtBills.getText().trim();

            // ClientID
            if (!Validator.isValidID(idText)) {
                JOptionPane.showMessageDialog(this, "Client ID must follow pattern AAA-1234.");
                return;
            }
            // First Name
            if (!Validator.isNotEmpty(firstText) || !NAME_PATTERN.matcher(firstText).matches()) {
                JOptionPane.showMessageDialog(this, "First name must contain only letters and spaces.");
                return;
            }
            // Last Name
            if (!Validator.isNotEmpty(lastText) || !NAME_PATTERN.matcher(lastText).matches()) {
                JOptionPane.showMessageDialog(this, "Last name must contain only letters and spaces.");
                return;
            }
            // Address
            if (!Validator.isNotEmpty(addressText) || !ADDRESS_PATTERN.matcher(addressText).matches()) {
                JOptionPane.showMessageDialog(this, "Address contains invalid characters.");
                return;
            }
            // Contact Info (email or phone)
            if (!Validator.isValidEmail(contactText) && !Validator.isValidPhone(contactText)) {
                JOptionPane.showMessageDialog(this, "Contact Info must be a valid email or phone number.");
                return;
            }
            // Bills (numeric)
            if (!Validator.isNumeric(billsText)) {
                JOptionPane.showMessageDialog(this, "Bills must be a numeric value.");
                return;
            }

            Client newC = new Client(
                idText,
                firstText,
                lastText,
                addressText,
                contactText,
                billsText
            );
            if (c == null) {
                dao.addClient(newC);
            } else {
                dao.updateClient(newC);
            }
            loadData();
        }
    }
}
