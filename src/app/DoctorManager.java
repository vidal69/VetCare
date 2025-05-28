package app;

import dao.DoctorDAO;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Doctor;
import utils.Validator;

public class DoctorManager extends JPanel {
    private String userRole;
    private DoctorDAO dao = new DoctorDAO();
    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbFields;
    private JTextField txtSearch;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");

    private List<Doctor> doctorList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private JButton prevBtn, nextBtn;
    private JTextField pageField;
    private JLabel totalPagesLabel;

    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    private JComboBox<String> cbSortBy;
    private JToggleButton btnSortOrder;

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Search Panel ---
        cbFields = new JComboBox<>(new String[]{
            "DoctorID", "FirstName", "LastName", "DateOfBirth"
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

        // Wire pagination actions
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
            } catch (NumberFormatException ex) {}
            updateTable();
        });

        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

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
            "DoctorID", "FirstName", "LastName", "DateOfBirth"
        });
        btnSortOrder = new JToggleButton("ASC");
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(cbSortBy);
        sortPanel.add(btnSortOrder);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        btnAdd.addActionListener(e -> {
            showDoctorDialog(null);
        });
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
            if (!"admin".equalsIgnoreCase(userRole)){
                JOptionPane.showMessageDialog(this, "You do not have permission to delete appointments.");
                return;
            }

            int i = table.getSelectedRow();
            if (i >= 0) {
                String id = (String) model.getValueAt(i, 0);
                
                // Check if there's existing Appointments
                if (dao.hasAppointment(id)){
                    JOptionPane.showMessageDialog(this, "Cannot delete Doctor. This Doctor is associated with existing appointment(s).",
                        "Delete Blocked", JOptionPane.WARNING_MESSAGE);
                    
                    return;
                }    

                //Check if there's existing Transactions
                if (dao.hasTransaction(id)){
                    JOptionPane.showMessageDialog(this, "Cannot delete Doctor. This Doctor is associated with existing transaction(s).",
                        "Delete Blocked", JOptionPane.WARNING_MESSAGE);
                    
                    return;
                }



                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected doctor?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deleteDoctor(id);
                    loadData();
                    JOptionPane.showMessageDialog(this, "Doctor has been deleted succesfully!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a doctor to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        // Search action
        btnSearch.addActionListener(e -> {
            String field = (String) cbFields.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                doctorList = dao.searchDoctors(field, keyword);
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
        doctorList = dao.getAllDoctors();
        currentPage = 1;
        updateTable();
    }

    /**
     * Populate the table model for the current page of doctorList.
     */
    private void updateTable() {
        model.setRowCount(0);
        int total = doctorList.size();
        totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            Doctor d = doctorList.get(i);
            model.addRow(new Object[]{
                d.getDoctorID(),
                d.getFirstName(),
                d.getLastName(),
                d.getDateOfBirth().toString()
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
        if (cbFields != null) {
            cbFields.setSelectedIndex(0);
        }
        if (txtSearch != null) {
            txtSearch.setText("");
        }
        loadData();
    }

    private void applySorting() {
        String column = (String) cbSortBy.getSelectedItem();
        String order = btnSortOrder.isSelected() ? "DESC" : "ASC";
        doctorList = dao.getAllDoctorsSorted(column, order);
        currentPage = 1;
        updateTable();
    }

    private void showDoctorDialog(Doctor d) {
        JTextField txtID = new JTextField();
        JTextField txtFirst = new JTextField();
        JTextField txtLast = new JTextField();
        JTextField txtDOB = new JTextField();

        if (d != null) {
            txtID.setText(d.getDoctorID());
            txtID.setEditable(true);
            txtFirst.setText(d.getFirstName());
            txtLast.setText(d.getLastName());
            txtDOB.setText(d.getDateOfBirth().toString());
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Doctor ID:")); panel.add(txtID);
        panel.add(new JLabel("First Name:")); panel.add(txtFirst);
        panel.add(new JLabel("Last Name:")); panel.add(txtLast);
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):")); panel.add(txtDOB);
        
        while(true){
            int result = JOptionPane.showConfirmDialog(this, panel,
                d == null ? "Add Doctor" : "Edit Doctor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION){
                break;
            }

        
            String idText = txtID.getText().trim();
            String firstText = txtFirst.getText().trim();
            String lastText = txtLast.getText().trim();
            String dobText = txtDOB.getText().trim();

            String errorMsg = validateDoctorInput(idText, firstText, lastText, dobText, d == null);
            if (errorMsg != null){
                JOptionPane.showMessageDialog(this, errorMsg, "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            Doctor newDoc = new Doctor(
                idText,
                firstText,
                lastText,
                LocalDate.parse(dobText)
            );

            boolean success;
            if (d==null){
                success = dao.addDoctor(newDoc);
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Error: Unable to add doctor.", "Add Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                JOptionPane.showMessageDialog(this, "Doctor added successfully.");
                } 
                else {
                    success = dao.updateDoctor(d, newDoc);
                    if (!success) {
                        JOptionPane.showMessageDialog(this, "Error: Unable to update doctor.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    JOptionPane.showMessageDialog(this, "Doctor updated successfully.");
                }
                loadData();
                break;
        }
    }

    private String validateDoctorInput(String idText, String firstText, String lastText, String dobText, boolean isAdd) {
        if (isAdd && !Validator.isValidID(idText)) {
            return "Doctor ID must follow pattern AAA-1234 (3 letters, hyphen, 4 digits).";
        }
        if (!Validator.isNotEmpty(firstText) || !NAME_PATTERN.matcher(firstText).matches()) {
            return "First name must contain only letters and spaces.";
        }
        if (!Validator.isNotEmpty(lastText) || !NAME_PATTERN.matcher(lastText).matches()) {
            return "Last name must contain only letters and spaces.";
        }
        if (!Validator.isValidDate(dobText)) {
            return "Date of Birth must be a valid date (YYYY-MM-DD).";
        }
        return null; // no errors
    }
         
    public DoctorManager(String role) {
        this.userRole = role;
        initComponents(); // critical for building table, scroll pane, etc.
    }

}
