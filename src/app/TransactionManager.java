package app;
import javax.swing.JOptionPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import dao.transactClientDAO;
import models.TransactClient;
import utils.Validator;
import dao.DoctorDAO;
import dao.ClientDAO;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TransactionManager extends JPanel {
    private transactClientDAO dao = new transactClientDAO();
    private DoctorDAO doctorDao = new DoctorDAO();
    private ClientDAO clientDao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbFields;
    private JTextField txtSearch;

    private JComboBox<String> cbSortBy;
    private JToggleButton btnSortOrder;

    private List<TransactClient> transactionList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private JButton prevBtn, nextBtn;
    private JTextField pageField;
    private JLabel totalPagesLabel;

    public TransactionManager() {
        setLayout(new BorderLayout());

        // --- Search Panel ---
        cbFields = new JComboBox<>(new String[]{
            "DoctorID", "ClientID", "TotalBills", "Receipt", "TransactionDate", "TransactionTime"
        });
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnClear  = new JButton("Clear");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(cbFields);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        add(searchPanel, BorderLayout.NORTH);

        // Table model and table
        model = new DefaultTableModel(new Object[]{
            "DoctorID", "ClientID", "TotalBills", "Receipt", "TransactionDate", "TransactionTime"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent inline editing
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
                if (p >= 1 && p <= totalPages) currentPage = p;
            } catch (NumberFormatException ex) { }
            updateTable();
        });

        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel (CRUD buttons + Sort By)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        cbSortBy = new JComboBox<>(new String[] {
            "DoctorID", "ClientID", "TotalBills", "Receipt", "TransactionDate", "TransactionTime"
        });
        btnSortOrder = new JToggleButton("ASC");

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(cbSortBy);
        sortPanel.add(btnSortOrder);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        // Sorting logic listeners
        cbSortBy.addActionListener(e -> applySorting());
        btnSortOrder.addActionListener(e -> {
            btnSortOrder.setText(btnSortOrder.isSelected() ? "DESC" : "ASC");
            applySorting();
        });

        btnAdd.addActionListener(e -> showTransactionDialog(null));

        // Button actions

        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String docId = (String) model.getValueAt(i, 0);
                String clientId = (String) model.getValueAt(i, 1);
                LocalDate date = LocalDate.parse((String) model.getValueAt(i, 4));
                LocalTime time = LocalTime.parse((String) model.getValueAt(i, 5));
                TransactClient t = dao.getTransaction(docId, clientId, date, time);
                showTransactionDialog(t);
            } else {
                JOptionPane.showMessageDialog(this, "Select a transaction to edit.");
            }
        });
        btnDelete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String docId = (String) model.getValueAt(i, 0);
                String clientId = (String) model.getValueAt(i, 1);
                LocalDate date = LocalDate.parse((String) model.getValueAt(i, 4));
                LocalTime time = LocalTime.parse((String) model.getValueAt(i, 5));
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected transaction?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deleteTransaction(docId, clientId, date, time);
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a transaction to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        // Search action
        btnSearch.addActionListener(e -> {
            String field = (String) cbFields.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                transactionList = dao.searchTransactions(field, keyword);
                currentPage = 1;
                updateTable();
            }
        });
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        loadData();
    }

    public void loadData() {
        transactionList = dao.getAllTransactions();
        currentPage = 1;
        updateTable();
    }

    private void applySorting() {
        String column = (String) cbSortBy.getSelectedItem();
        String order = btnSortOrder.isSelected() ? "DESC" : "ASC";
        transactionList = dao.getAllTransactionsSorted(column, order);
        currentPage = 1;
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        int total = transactionList.size();
        totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            TransactClient t = transactionList.get(i);
            model.addRow(new Object[]{
                t.getDoctorID(),
                t.getClientID(),
                t.getTotalBills(),
                t.getReceipt(),
                t.getTransactionDate().toString(),
                t.getTransactionTime().toString()
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
       cbFields.setSelectedIndex(0);
       txtSearch.setText("");
       loadData();
   }

    private void showTransactionDialog(TransactClient t) {
        // Dropdowns for DoctorID and ClientID
        Vector<String> docIds = new Vector<>();
        doctorDao.getAllDoctors().forEach(d -> docIds.add(d.getDoctorID()));
        JComboBox<String> cbDoc = new JComboBox<>(docIds);

        Vector<String> clientIds = new Vector<>();
        clientDao.getAllClients().forEach(c -> clientIds.add(c.getClientID()));
        JComboBox<String> cbClient = new JComboBox<>(clientIds);

        JTextField txtBills = new JTextField();
        JComboBox<String> cbReceipt = new JComboBox<>(new String[]{"Sent", "Pending"});
        JTextField txtDate = new JTextField();
        JTextField txtTime = new JTextField();

        if (t != null) {
            cbDoc.setSelectedItem(t.getDoctorID());
            cbClient.setSelectedItem(t.getClientID());
            txtBills.setText(t.getTotalBills());
            cbReceipt.setSelectedItem(t.getReceipt());
            txtDate.setText(t.getTransactionDate().toString());
            txtTime.setText(t.getTransactionTime().toString());
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Doctor ID:")); panel.add(cbDoc);
        panel.add(new JLabel("Client ID:")); panel.add(cbClient);
        panel.add(new JLabel("Total Bills:")); panel.add(txtBills);
        panel.add(new JLabel("Receipt:")); panel.add(cbReceipt);
        panel.add(new JLabel("Date (YYYY-MM-DD):")); panel.add(txtDate);
        panel.add(new JLabel("Time (HH:MM:SS):")); panel.add(txtTime);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                    t == null ? "Add Transaction" : "Edit Transaction",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) break;

            String docID = (String) cbDoc.getSelectedItem();
            String clientID = (String) cbClient.getSelectedItem();
            String bills = txtBills.getText().trim();
            String receipt = (String) cbReceipt.getSelectedItem();
            String date = txtDate.getText().trim();
            String time = txtTime.getText().trim();

            String errorMsg = validateTransactionInput(docID, clientID, bills, receipt, date, time);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            TransactClient newT = new TransactClient(
                    docID.trim(),
                    clientID.trim(),
                    bills,
                    receipt.trim(),
                    LocalDate.parse(date),
                    LocalTime.parse(time)
            );

            boolean success;
            if (t == null) {
                success = dao.addTransaction(newT);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Transaction recorded successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add transaction.", "Add Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                success = dao.updateTransaction(t, newT);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Transaction updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update transaction.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }

            // Refresh list and table
            transactionList = dao.getAllTransactions();
            int total = transactionList.size();
            totalPages = Math.max(1, (int)Math.ceil(total / (double) PAGE_SIZE));
            if (currentPage > totalPages) currentPage = totalPages;
            updateTable();
            break;
        }
    }

    private String validateTransactionInput(String docID, String clientID, String bills, String receipt,
                                            String date, String time) {
        if (docID == null || docID.trim().isEmpty()) {
            return "Doctor ID must be selected.";
        }
        if (clientID == null || clientID.trim().isEmpty()) {
            return "Client ID must be selected.";
        }
        if (!Validator.isNumeric(bills)) {
            return "Total bills must be numeric.";
        }
        if (!Validator.isNotEmpty(receipt)) {
            return "Receipt status must be selected.";
        }
        if (!Validator.isValidDate(date)) {
            return "Date must be in YYYY-MM-DD format.";
        }
        if (!Validator.isValidTime(time)) {
            return "Time must be in HH:MM:SS format.";
        }
        return null;
    }

}
