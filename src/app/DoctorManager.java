package app;

import dao.DoctorDAO;
import models.Doctor;
import utils.Validator;

import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DoctorManager extends JPanel {
    private DoctorDAO dao = new DoctorDAO();
    private JTable table;
    private DefaultTableModel model;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");

    public DoctorManager() {
        setLayout(new BorderLayout());

        // Table setup
        model = new DefaultTableModel(new Object[]{
            "DoctorID", "FirstName", "LastName", "DateOfBirth"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent inline editing
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

        // Button actions
        btnAdd.addActionListener(e -> showDoctorDialog(null));
        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                Doctor d = dao.getDoctorByID(id);
                showDoctorDialog(d);
            } else {
                JOptionPane.showMessageDialog(this, "Select a doctor to edit.");
            }
        });
        btnDelete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected doctor?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deleteDoctor(id);
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a doctor to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<Doctor> list = dao.getAllDoctors();
        for (Doctor d : list) {
            model.addRow(new Object[]{
                d.getDoctorID(),
                d.getFirstName(),
                d.getLastName(),
                d.getDateOfBirth().toString()
            });
        }
    }

    private void showDoctorDialog(Doctor d) {
        JTextField txtID = new JTextField();
        JTextField txtFirst = new JTextField();
        JTextField txtLast = new JTextField();
        JTextField txtDOB = new JTextField();

        if (d != null) {
            txtID.setText(d.getDoctorID());
            txtID.setEditable(false);
            txtFirst.setText(d.getFirstName());
            txtLast.setText(d.getLastName());
            txtDOB.setText(d.getDateOfBirth().toString());
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Doctor ID:")); panel.add(txtID);
        panel.add(new JLabel("First Name:")); panel.add(txtFirst);
        panel.add(new JLabel("Last Name:")); panel.add(txtLast);
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):")); panel.add(txtDOB);

        int result = JOptionPane.showConfirmDialog(this, panel,
            d == null ? "Add Doctor" : "Edit Doctor",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String idText = txtID.getText().trim();
            String firstText = txtFirst.getText().trim();
            String lastText = txtLast.getText().trim();
            String dobText = txtDOB.getText().trim();

            // DoctorID
            if (!Validator.isValidID(idText)) {
                JOptionPane.showMessageDialog(this, "Doctor ID must follow pattern AAA-1234 (3 letters, hyphen, 4 digits).");
                return;
            }

            // FirstName
            if (!Validator.isNotEmpty(firstText) || !NAME_PATTERN.matcher(firstText).matches()) {
                JOptionPane.showMessageDialog(this, "First name must contain only letters and spaces.");
                return;
            }

            // LastName
            if (!Validator.isNotEmpty(lastText) || !NAME_PATTERN.matcher(lastText).matches()) {
                JOptionPane.showMessageDialog(this, "Last name must contain only letters and spaces.");
                return;
            }

            // DateOfBirth
            if (!Validator.isValidDate(dobText)) {
                JOptionPane.showMessageDialog(this, "Date of Birth must be a valid date (YYYY-MM-DD).");
                return;
            }

            Doctor newDoc = new Doctor(
                idText,
                firstText,
                lastText,
                LocalDate.parse(dobText)
            );
            boolean success;
            if (d == null) {
                success = dao.addDoctor(newDoc);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Doctor added successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Unable to add doctor.", "Add Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                success = dao.updateDoctor(newDoc);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Doctor updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Unable to update doctor.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            loadData();
        }
    }
}
