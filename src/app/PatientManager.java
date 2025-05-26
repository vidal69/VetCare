package app;

import dao.ClientDAO;
import dao.PatientDAO;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Patient;
import utils.Validator;

public class PatientManager extends JPanel {
    private String userRole;
    private PatientDAO dao = new PatientDAO();
    private ClientDAO clientDao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbFields;
    private JTextField txtSearch;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern SPECIES_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern BREED_PATTERN = Pattern.compile("^[A-Za-z0-9 ]+$");

    private List<Patient> patientList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private JButton prevBtn, nextBtn;
    private JTextField pageField;
    private JLabel totalPagesLabel;

    private mainGUI mainGUI;

    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    private JComboBox<String> cbSortBy;
    private JToggleButton btnSortOrder;

    public PatientManager(String role) {
        this.userRole = role;
        this.mainGUI = mainGUI;

        setLayout(new BorderLayout());

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbFields = new JComboBox<>(new String[]{
            "PatientID", "Name", "Gender", "Species", "Breed", "ClientID"
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
            "PatientID", "Name", "DateOfBirth", "Gender", "Species", "Breed", "Remarks", "ClientID"
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
                if (p >= 1 && p <= totalPages) {
                    currentPage = p;
                }
            } catch (NumberFormatException ex) { }
            updateTable();
        });

        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Buttons and Sort Panel in Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAdd = new JButton("Add");
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        if (!"admin".equalsIgnoreCase(userRole)) {
            btnAdd.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        cbSortBy = new JComboBox<>(new String[] {
            "PatientID", "Name", "DateOfBirth", "Gender", "Species", "Breed", "ClientID"
        });
        btnSortOrder = new JToggleButton("ASC");
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(cbSortBy);
        sortPanel.add(btnSortOrder);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // CRUDL button listeners
        btnAdd.addActionListener(e -> showPatientDialog(null));

        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                Patient p = patientList.get(i + (currentPage - 1) * PAGE_SIZE);
                showPatientDialog(p);
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to edit.");
            }
        });

        btnDelete.addActionListener(e -> {
            if (!"admin".equalsIgnoreCase(userRole)){
                JOptionPane.showMessageDialog(this, "You do not have permission to delete appointments.");
                return;
            }

            int i = table.getSelectedRow();
            if (i >= 0) {
                // Map selected row to actual index
                int index = (currentPage - 1) * PAGE_SIZE + i;
                Patient p = patientList.get(index);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deletePatient(p.getPatientID());
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to delete.");
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        // Search action
        btnSearch.addActionListener(e -> {
            String field = (String) cbFields.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                patientList = dao.searchPatients(field, keyword);
                currentPage = 1;
                updateTable();
            }
        });
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        cbSortBy.addActionListener(e -> applySorting());
        btnSortOrder.addActionListener(e -> {
            btnSortOrder.setText(btnSortOrder.isSelected() ? "DESC" : "ASC");
            applySorting();
        });

        loadData();
    }

    public void loadData() {
        patientList = dao.getAllPatients();
        currentPage = 1;
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        int total = patientList.size();
        totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            Patient p = patientList.get(i);
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

    private void showPatientDialog(Patient p) {
        JTextField txtID = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtDOB = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField txtSpecies = new JTextField();
        JTextField txtBreed = new JTextField();
        JTextField txtRemarks = new JTextField();

        // Populate client IDs
        Vector<String> clientIds = new Vector<>();
        clientDao.getAllClients().forEach(c -> clientIds.add(c.getClientID()));
        JComboBox<String> cbClientID = new JComboBox<>(clientIds);

        if (p != null) {
            txtID.setText(p.getPatientID());
            txtID.setEditable(true);
            txtName.setText(p.getName());
            txtDOB.setText(p.getDateOfBirth().toString());
            cbGender.setSelectedItem(p.getGender());
            txtSpecies.setText(p.getSpecies());
            txtBreed.setText(p.getBreed());
            txtRemarks.setText(p.getRemarks());
            cbClientID.setSelectedItem(p.getClientID());
        }

        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.add(new JLabel("Patient ID:")); panel.add(txtID);
        panel.add(new JLabel("Name:")); panel.add(txtName);
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):")); panel.add(txtDOB);
        panel.add(new JLabel("Gender:")); panel.add(cbGender);
        panel.add(new JLabel("Species:")); panel.add(txtSpecies);
        panel.add(new JLabel("Breed:")); panel.add(txtBreed);
        panel.add(new JLabel("Remarks:")); panel.add(txtRemarks);
        panel.add(new JLabel("Client ID:")); panel.add(cbClientID);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                    p == null ? "Add Patient" : "Edit Patient",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) break;

            // Gather inputs
            String idText = txtID.getText().trim();
            String nameText = txtName.getText().trim();
            String dobText = txtDOB.getText().trim();
            String genderText = (String) cbGender.getSelectedItem();
            String speciesText = txtSpecies.getText().trim();
            String breedText = txtBreed.getText().trim();
            String remarksText = txtRemarks.getText().trim();
            String clientIdText = (String) cbClientID.getSelectedItem();

            // Validate
            String errorMsg = validatePatientInput(idText, nameText, dobText, genderText, speciesText, breedText, clientIdText);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Patient newP = new Patient(
                    idText,
                    nameText,
                    LocalDate.parse(dobText),
                    genderText,
                    speciesText,
                    breedText,
                    remarksText,
                    clientIdText
            );

            boolean success;
            if (p == null) {
                success = dao.addPatient(newP);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Patient added successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add patient.", "Add Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                success = dao.updatePatient(p, newP);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Patient updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update patient.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }

            loadData();
            break;
        }
    }

    private String validatePatientInput(String id, String name, String dob, String gender,
                                        String species, String breed, String clientId) {
        if (!Validator.isValidID(id)) {
            return "Patient ID must follow pattern AAA-1234.";
        }
        if (!Validator.isNotEmpty(name) || !NAME_PATTERN.matcher(name).matches()) {
            return "Name must contain only letters and spaces.";
        }
        if (!Validator.isValidDate(dob)) {
            return "Date of Birth must be a valid date (YYYY-MM-DD).";
        }
        if (!Validator.isNotEmpty(gender) || !NAME_PATTERN.matcher(gender).matches()) {
            return "Gender must contain only letters.";
        }
        if (!Validator.isNotEmpty(species) || !SPECIES_PATTERN.matcher(species).matches()) {
            return "Species must contain only letters and spaces.";
        }
        if (!Validator.isNotEmpty(breed) || !BREED_PATTERN.matcher(breed).matches()) {
            return "Breed must contain only letters, numbers, and spaces.";
        }
        if (!Validator.isValidID(clientId)) {
            return "Client ID must follow pattern AAA-1234.";
        }
        return null;
    }


    private void applySorting() {
        String column = (String) cbSortBy.getSelectedItem();
        String order = btnSortOrder.isSelected() ? "DESC" : "ASC";
        patientList = dao.getAllPatientsSorted(column, order);
        currentPage = 1;
        updateTable();
    }

}


