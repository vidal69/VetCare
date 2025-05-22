package app;

import dao.PatientDAO;
import models.Patient;
import utils.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class PatientManager extends JPanel {
    private PatientDAO dao = new PatientDAO();
    private JTable table;
    private DefaultTableModel model;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern SPECIES_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern BREED_PATTERN = Pattern.compile("^[A-Za-z0-9 ]+$");

    public PatientManager() {
        setLayout(new BorderLayout());

        // Table setup
        model = new DefaultTableModel(new Object[]{
            "PatientID", "Name", "DateOfBirth", "Gender", "Species", "Breed", "Remarks", "ClientID"
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
        btnAdd.addActionListener(e -> showPatientDialog(null));
        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                Patient p = dao.getPatientByID(id);
                showPatientDialog(p);
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to edit.");
            }
        });
        btnDelete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deletePatient(id);
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<Patient> list = dao.getAllPatients();
        for (Patient p : list) {
            model.addRow(new Object[]{
                p.getPatientID(),
                p.getName(),
                p.getDateOfBirth().toString(),
                p.getGender(),
                p.getSpecies(),
                p.getBreed(),
                p.getRemarks(),
                p.getClientID()
            });
        }
    }

    private void showPatientDialog(Patient p) {
        JTextField txtID = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtDOB = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField txtSpecies = new JTextField();
        JTextField txtBreed = new JTextField();
        JTextField txtRemarks = new JTextField();
        JTextField txtClientID = new JTextField();

        if (p != null) {
            txtID.setText(p.getPatientID());
            txtID.setEditable(false);
            txtName.setText(p.getName());
            txtDOB.setText(p.getDateOfBirth().toString());
            cbGender.setSelectedItem(p.getGender());
            txtSpecies.setText(p.getSpecies());
            txtBreed.setText(p.getBreed());
            txtRemarks.setText(p.getRemarks());
            txtClientID.setText(p.getClientID());
        }

        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.add(new JLabel("Patient ID:")); panel.add(txtID);
        panel.add(new JLabel("Name:")); panel.add(txtName);
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):")); panel.add(txtDOB);
        panel.add(new JLabel("Gender:")); panel.add(cbGender);
        panel.add(new JLabel("Species:")); panel.add(txtSpecies);
        panel.add(new JLabel("Breed:")); panel.add(txtBreed);
        panel.add(new JLabel("Remarks:")); panel.add(txtRemarks);
        panel.add(new JLabel("Client ID:")); panel.add(txtClientID);

        int result = JOptionPane.showConfirmDialog(this, panel,
            p == null ? "Add Patient" : "Edit Patient",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String idText = txtID.getText().trim();
            String nameText = txtName.getText().trim();
            String dobText = txtDOB.getText().trim();
            String genderText = ((String) cbGender.getSelectedItem()).trim();
            String speciesText = txtSpecies.getText().trim();
            String breedText = txtBreed.getText().trim();
            String clientIdText = txtClientID.getText().trim();

            // PatientID
            if (!Validator.isValidID(idText)) {
                JOptionPane.showMessageDialog(this, "Patient ID must follow pattern AAA-1234.");
                return;
            }
            // Name
            if (!Validator.isNotEmpty(nameText) || !NAME_PATTERN.matcher(nameText).matches()) {
                JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces.");
                return;
            }
            // DateOfBirth
            if (!Validator.isValidDate(dobText)) {
                JOptionPane.showMessageDialog(this, "Date of Birth must be a valid date (YYYY-MM-DD).");
                return;
            }
            // Gender
            if (!Validator.isNotEmpty(genderText) || !NAME_PATTERN.matcher(genderText).matches()) {
                JOptionPane.showMessageDialog(this, "Gender must contain only letters.");
                return;
            }
            // Species
            if (!Validator.isNotEmpty(speciesText) || !SPECIES_PATTERN.matcher(speciesText).matches()) {
                JOptionPane.showMessageDialog(this, "Species must contain only letters and spaces.");
                return;
            }
            // Breed
            if (!Validator.isNotEmpty(breedText) || !BREED_PATTERN.matcher(breedText).matches()) {
                JOptionPane.showMessageDialog(this, "Breed must contain letters, numbers, and spaces only.");
                return;
            }
            // ClientID
            if (!Validator.isValidID(clientIdText)) {
                JOptionPane.showMessageDialog(this, "Client ID must follow pattern AAA-1234.");
                return;
            }

            Patient newP = new Patient(
                idText,
                nameText,
                LocalDate.parse(dobText),
                genderText,
                speciesText,
                breedText,
                txtRemarks.getText().trim(),
                clientIdText
            );
            if (p == null) {
                dao.addPatient(newP);
            } else {
                dao.updatePatient(newP);
            }
            loadData();
        }
    }
}
