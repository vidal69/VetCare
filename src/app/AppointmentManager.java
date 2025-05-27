package app;
import dao.ClientDAO;
import dao.DoctorDAO;
import dao.scheduleClientDAO;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.ScheduleClient;
import utils.Validator;

public class AppointmentManager extends JPanel {
    private String userRole;
    private scheduleClientDAO dao = new scheduleClientDAO();
    private DoctorDAO doctorDao = new DoctorDAO();
    private ClientDAO clientDao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbFields;
    private JTextField txtSearch;

    private List<ScheduleClient> appointmentList = new ArrayList<>();
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

    public AppointmentManager(String role) {
        setLayout(new BorderLayout());
        this.userRole = role;
       
        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbFields = new JComboBox<>(new String[]{
            "DoctorID",
            "ClientID",
            "AppointmentType",
            "AppointmentDate",
            "AppointmentTime",
            "Status"
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

        // Table model and table
        model = new DefaultTableModel(new Object[]{
            "DoctorID", "ClientID", "Type", "Date (YYYY-MM-DD)", "Time (HH:MM:SS)", "Status", "Remarks"
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

        // Buttons and sorting
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
            "DoctorID", "ClientID", "AppointmentType", "AppointmentDate", "AppointmentTime", "Status", "Remarks"
        });
        btnSortOrder = new JToggleButton("ASC");

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(cbSortBy);
        sortPanel.add(btnSortOrder);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // CRUD button listeners
        btnAdd.addActionListener(e -> showAppointmentDialog(null));

        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                // Edit the selected appointment
                showAppointmentDialog(appointmentList.get(i));
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to edit.");
            }
        });

        btnDelete.addActionListener(e -> {
            if (!"admin".equalsIgnoreCase(userRole)){
                JOptionPane.showMessageDialog(this, "You do not have permission to delete appointments.");
                return;
            }

            int i = table.getSelectedRow();
            if (i >= 0) {
                ScheduleClient sc = appointmentList.get(i);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                  dao.deleteAppointment(
                      sc.getDoctorID(),
                      sc.getClientID(),
                      sc.getAppointmentDate(),
                      sc.getAppointmentTime()
                  );
                    loadData();
                    JOptionPane.showMessageDialog(this, "Appointment has been deleted succesfully!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to delete.");
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        // Search action
        btnSearch.addActionListener(e -> {
            String field = (String) cbFields.getSelectedItem();
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                appointmentList = dao.searchAppointments(field, keyword);
                currentPage = 1;
                updateTable();
            }
        });
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            // Reset to full appointment list and refresh current page
            appointmentList = dao.getAllAppointments();
            currentPage = 1;
            updateTable();
        });

        cbSortBy.addActionListener(e -> applySorting());
        btnSortOrder.addActionListener(e -> {
            btnSortOrder.setText(btnSortOrder.isSelected() ? "DESC" : "ASC");
            applySorting();
        });

    }

    public void loadData() {
        appointmentList = dao.getAllAppointments();
        currentPage = 1;
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        int total = appointmentList.size();
        totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            ScheduleClient sc = appointmentList.get(i);
            model.addRow(new Object[]{
                sc.getDoctorID(),
                sc.getClientID(),
                sc.getAppointmentType(),
                sc.getAppointmentDate().toString(),
                sc.getAppointmentTime().toString(),
                sc.getStatus(),
                sc.getRemarks()
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

    private void applySorting() {
        String column = (String) cbSortBy.getSelectedItem();
        String order = btnSortOrder.isSelected() ? "DESC" : "ASC";
        appointmentList = dao.getAllAppointmentsSorted(column, order);
        currentPage = 1;
        updateTable();
    }

    private void showAppointmentDialog(ScheduleClient sc) {
        // Dropdown for DoctorID
        Vector<String> docIds = new Vector<>();
        doctorDao.getAllDoctors().forEach(d -> docIds.add(d.getDoctorID()));
        JComboBox<String> cbDoc = new JComboBox<>(docIds);
        // Dropdown for ClientID
        Vector<String> clientIds = new Vector<>();
        clientDao.getAllClients().forEach(c -> clientIds.add(c.getClientID()));
        JComboBox<String> cbClient = new JComboBox<>(clientIds);
        JTextField txtType = new JTextField();
        JTextField txtDate = new JTextField();
        JTextField txtTime = new JTextField();
        JTextField txtStatus = new JTextField();
        JTextField txtRemarks = new JTextField();

        if (sc != null) {
            cbDoc.setSelectedItem(sc.getDoctorID());
            cbClient.setSelectedItem(sc.getClientID());
            txtType.setText(sc.getAppointmentType());
            txtDate.setText(sc.getAppointmentDate().toString());
            txtTime.setText(sc.getAppointmentTime().toString());
            txtStatus.setText(sc.getStatus());
            txtRemarks.setText(sc.getRemarks());
        }

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.add(new JLabel("Doctor ID:")); panel.add(cbDoc);
        panel.add(new JLabel("Client ID:")); panel.add(cbClient);
        panel.add(new JLabel("Type:")); panel.add(txtType);
        panel.add(new JLabel("Date (YYYY-MM-DD):")); panel.add(txtDate);
        panel.add(new JLabel("Time (HH:MM:SS):")); panel.add(txtTime);
        panel.add(new JLabel("Status:")); panel.add(txtStatus);
        panel.add(new JLabel("Remarks:")); panel.add(txtRemarks);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                sc == null ? "Add Appointment" : "Edit Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                break; // user cancelled
            }

            String doctorID = (String) cbDoc.getSelectedItem();
            String clientID = (String) cbClient.getSelectedItem();
            String type = txtType.getText().trim();
            String date = txtDate.getText().trim();
            String time = txtTime.getText().trim();
            String status = txtStatus.getText().trim();
            String remarks = txtRemarks.getText().trim();

            // Validate inputs
            String errorMsg = validateAppointmentInput(doctorID, clientID, type, date, time, status);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            ScheduleClient newSc = new ScheduleClient(
                doctorID, clientID, type,
                LocalDate.parse(date),
                LocalTime.parse(time),
                status, remarks
            );

            if (sc == null) {
                dao.addAppointment(newSc);
                JOptionPane.showMessageDialog(this, "Appointment added successfully.");
            } else {
                boolean success = dao.updateAppointment(sc, newSc);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Appointment updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update appointment.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }

            loadData();
            break; 
            }
        }
        
    private String validateAppointmentInput(String doctorID, String clientID, String type, String date, String time, String status) {
        if (doctorID == null || doctorID.isEmpty()) {
            return "Doctor ID must be selected.";
        }
        if (clientID == null || clientID.isEmpty()) {
            return "Client ID must be selected.";
        }
        if (!Validator.isNotEmpty(type)) {
            return "Appointment type cannot be empty.";
        }
        if (!Validator.isValidDate(date)) {
            return "Date must be valid and follow format YYYY-MM-DD.";
        }
        if (!Validator.isValidTime(time)) {
            return "Time must be valid and follow format HH:MM:SS.";
        }
        if (!Validator.isNotEmpty(status)) {
            return "Status cannot be empty.";
        }
        return null;
    }
}
