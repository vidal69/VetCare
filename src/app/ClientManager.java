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
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientManager extends JPanel {
    private ClientDAO dao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbFields;
    private JTextField txtSearch;
    // Sorting controls
    private JComboBox<String> cbSortBy;
    private JToggleButton btnSortOrder;

    // Only letters and spaces for names
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    // Simple address pattern (alphanumeric, spaces, commas, dots)
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[\\w\\s,\\.\\-#]+$");

    private List<Client> clientList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private JButton prevBtn, nextBtn;
    private JTextField pageField;
    private JLabel totalPagesLabel;


    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;




    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbFields = new JComboBox<>(new String[]{
            "ClientID", "FirstName", "LastName", "Address", "ContactInfo", "Bills"
        });
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnClear  = new JButton("Clear");
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(cbFields);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        add(searchPanel, BorderLayout.NORTH);

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

        // Center panel with table and pagination
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Pagination controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevBtn = new JButton("Prev");
        nextBtn = new JButton("Next");
        pageField = new JTextField(3);
        pageField.setHorizontalAlignment(JTextField.CENTER);
        totalPagesLabel = new JLabel(" of " + totalPages);
        paginationPanel.add(prevBtn);
        paginationPanel.add(new JLabel("Page "));
        paginationPanel.add(pageField);
        paginationPanel.add(totalPagesLabel);
        paginationPanel.add(nextBtn);

        // Pagination actions
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTable();
            }
        });
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTable();
            }
        });
        pageField.addActionListener(e -> {
            try {
                int p = Integer.parseInt(pageField.getText().trim());
                if (p >= 1 && p <= totalPages) {
                    currentPage = p;
                }
            } catch (NumberFormatException ex) { }
            updateTable();
        });

        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Panel for Buttons and Sorting Controls ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAdd = new JButton("Add");
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        cbSortBy = new JComboBox<>(new String[] {
            "ClientID", "FirstName", "LastName", "Address", "ContactInfo", "Bills"
        });
        btnSortOrder = new JToggleButton("ASC");
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(cbSortBy);
        sortPanel.add(btnSortOrder);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

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

        // Search action
        btnSearch.addActionListener(e -> {
            String field = (String) cbFields.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                clientList = dao.searchClients(field, keyword);
                currentPage = 1;
                updateTable();
            }
        });
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        // Sorting actions
        cbSortBy.addActionListener(e -> applySorting());
        btnSortOrder.addActionListener(e -> {
            btnSortOrder.setText(btnSortOrder.isSelected() ? "DESC" : "ASC");
            applySorting();
        });

        loadData();
    }

    public void loadData() {
        clientList = dao.getAllClients();
        currentPage = 1;
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        int total = clientList.size();
        totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            Client c = clientList.get(i);
            model.addRow(new Object[]{
                c.getClientID(),
                c.getFirstName(),
                c.getLastName(),
                c.getAddress(),
                c.getContactInfo(),
                c.getBills()
            });
        }
        pageField.setText(String.valueOf(currentPage));
        totalPagesLabel.setText(" of " + totalPages);
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < totalPages);
    }

    /**
     * Reset search controls and reload full table.
     */
    public void clearSearch() {
        if (cbFields != null) cbFields.setSelectedIndex(0);
        if (txtSearch != null) txtSearch.setText("");
        loadData();
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
            txtID.setEditable(true);
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

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                c == null ? "Add Client" : "Edit Client",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) break;

            String idText = txtID.getText().trim();
            String firstText = txtFirst.getText().trim();
            String lastText = txtLast.getText().trim();
            String addressText = txtAddress.getText().trim();
            String contactText = txtContact.getText().trim();
            String billsText = txtBills.getText().trim();

            String errorMsg = validateClientInput(idText, firstText, lastText, addressText, contactText, billsText);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Client newC = new Client(idText, firstText, lastText, addressText, contactText, billsText);
            boolean success;
            if (c == null) {
                success = dao.addClient(newC);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Client added successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add client.", "Add Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                success = dao.updateClient(c, newC);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Client updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update client.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }
            loadData();
            break;
        }
    }

    private String validateClientInput(String id, String first, String last, String address, String contact, String bills) {
        if (!Validator.isValidID(id)) {
            return "Client ID must follow the pattern AAA-1234.";
        }
        if (!Validator.isNotEmpty(first) || !NAME_PATTERN.matcher(first).matches()) {
            return "First name must contain only letters and spaces.";
        }
        if (!Validator.isNotEmpty(last) || !NAME_PATTERN.matcher(last).matches()) {
            return "Last name must contain only letters and spaces.";
        }
        if (!Validator.isNotEmpty(address) || !ADDRESS_PATTERN.matcher(address).matches()) {
            return "Address contains invalid characters.";
        }
        if (!Validator.isValidEmail(contact) && !Validator.isValidPhone(contact)) {
            return "Contact info must be a valid email or phone number.";
        }
        if (!Validator.isNumeric(bills)) {
            return "Bills must be a numeric value.";
        }
        return null;
    }

    public ClientManager() {
        initComponents(); // critical for building table, scroll pane, etc.
    }

    // Sorting logic
    private void applySorting() {
        String column = (String) cbSortBy.getSelectedItem();
        String order = btnSortOrder.isSelected() ? "DESC" : "ASC";
        clientList = dao.getAllClientsSorted(column, order);
        currentPage = 1;
        updateTable();
    }
}
