package app;

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

public class TransactionManager extends JPanel {
    private transactClientDAO dao = new transactClientDAO();
    private DoctorDAO doctorDao = new DoctorDAO();
    private ClientDAO clientDao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;

    public TransactionManager() {
        setLayout(new BorderLayout());

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
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons panel
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

        // Button actions
        btnAdd.addActionListener(e -> showTransactionDialog(null));
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

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<TransactClient> list = dao.getAllTransactions();
        for (TransactClient t : list) {
            model.addRow(new Object[]{
                t.getDoctorID(),
                t.getClientID(),
                t.getTotalBills(),
                t.getReceipt(),
                t.getTransactionDate().toString(),
                t.getTransactionTime().toString()
            });
        }
    }

    private void showTransactionDialog(TransactClient t) {
        // Dropdown for DoctorID
        Vector<String> docIds = new Vector<>();
        doctorDao.getAllDoctors().forEach(d -> docIds.add(d.getDoctorID()));
        JComboBox<String> cbDoc = new JComboBox<>(docIds);
        // Dropdown for ClientID
        Vector<String> clientIds = new Vector<>();
        clientDao.getAllClients().forEach(c -> clientIds.add(c.getClientID()));
        JComboBox<String> cbClient = new JComboBox<>(clientIds);
        JTextField txtBills = new JTextField();
        JTextField txtReceipt = new JTextField();
        JTextField txtDate = new JTextField();
        JTextField txtTime = new JTextField();

        if (t != null) {
            cbDoc.setSelectedItem(t.getDoctorID());
            cbDoc.setEnabled(false);
            cbClient.setSelectedItem(t.getClientID());
            cbClient.setEnabled(false);
            txtBills.setText(t.getTotalBills());
            txtReceipt.setText(t.getReceipt());
            txtDate.setText(t.getTransactionDate().toString());
            txtTime.setText(t.getTransactionTime().toString());
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Doctor ID:")); panel.add(cbDoc);
        panel.add(new JLabel("Client ID:")); panel.add(cbClient);
        panel.add(new JLabel("Total Bills:")); panel.add(txtBills);
        panel.add(new JLabel("Receipt:")); panel.add(txtReceipt);
        panel.add(new JLabel("Date (YYYY-MM-DD):")); panel.add(txtDate);
        panel.add(new JLabel("Time (HH:MM:SS):")); panel.add(txtTime);

        int result = JOptionPane.showConfirmDialog(this, panel,
            t == null ? "Add Transaction" : "Edit Transaction",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            if (cbDoc.getSelectedItem() == null ||
                cbClient.getSelectedItem() == null ||
                !Validator.isNumeric(txtBills.getText()) ||
                !Validator.isNotEmpty(txtReceipt.getText()) ||
                !Validator.isValidDate(txtDate.getText()) ||
                !Validator.isValidTime(txtTime.getText())) {
                JOptionPane.showMessageDialog(this, "Please check your inputs.");
                return;
            }
            TransactClient newT = new TransactClient(
                ((String) cbDoc.getSelectedItem()).trim(),
                ((String) cbClient.getSelectedItem()).trim(),
                txtBills.getText().trim(),
                txtReceipt.getText().trim(),
                LocalDate.parse(txtDate.getText().trim()),
                LocalTime.parse(txtTime.getText().trim())
            );
            if (t == null) {
                dao.addTransaction(newT);
            } else {
                dao.updateTransaction(newT);
            }
            loadData();
        }
    }
}
