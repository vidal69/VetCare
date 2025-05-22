package app;

import dao.scheduleClientDAO;
import dao.DoctorDAO;
import dao.ClientDAO;
import java.util.Vector;
import javax.swing.JComboBox;
import models.ScheduleClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import utils.Validator;

public class AppointmentManager extends JPanel {
    private scheduleClientDAO dao = new scheduleClientDAO();
    private DoctorDAO doctorDao = new DoctorDAO();
    private ClientDAO clientDao = new ClientDAO();
    private JTable table;
    private DefaultTableModel model;

    public AppointmentManager() {
        setLayout(new BorderLayout());

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
        btnAdd.addActionListener(e -> showAppointmentDialog(null));
        btnEdit.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                ScheduleClient sc = dao.getAppointment(
                    (String) model.getValueAt(i, 0),
                    (String) model.getValueAt(i, 1),
                    LocalDate.parse((String) model.getValueAt(i, 3)),
                    LocalTime.parse((String) model.getValueAt(i, 4))
                );
                showAppointmentDialog(sc);
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to edit.");
            }
        });
        btnDelete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                String docId = (String) model.getValueAt(i, 0);
                String clientId = (String) model.getValueAt(i, 1);
                LocalDate date = LocalDate.parse((String) model.getValueAt(i, 3));
                LocalTime time = LocalTime.parse((String) model.getValueAt(i, 4));
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.deleteAppointment(docId, clientId, date, time);
                    loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to delete.");
            }
        });
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<ScheduleClient> list = dao.getAllAppointments();
        for (ScheduleClient sc : list) {
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
            cbDoc.setEnabled(false);
            cbClient.setSelectedItem(sc.getClientID());
            cbClient.setEnabled(false);
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

        int result = JOptionPane.showConfirmDialog(this, panel,
            sc == null ? "Add Appointment" : "Edit Appointment",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Validation
            if (cbDoc.getSelectedItem() == null ||
                cbClient.getSelectedItem() == null ||
                !Validator.isNotEmpty(txtType.getText()) ||
                !Validator.isValidDate(txtDate.getText()) ||
                !Validator.isValidTime(txtTime.getText()) ||
                !Validator.isNotEmpty(txtStatus.getText())) {
                JOptionPane.showMessageDialog(this, "Please check your inputs.");
                return;
            }
            ScheduleClient newSc = new ScheduleClient(
                ((String) cbDoc.getSelectedItem()).trim(),
                ((String) cbClient.getSelectedItem()).trim(),
                txtType.getText().trim(),
                LocalDate.parse(txtDate.getText().trim()),
                LocalTime.parse(txtTime.getText().trim()),
                txtStatus.getText().trim(),
                txtRemarks.getText().trim()
            );
            if (sc == null) {
                dao.addAppointment(newSc);
            } else {
                dao.updateAppointment(newSc);
            }
            loadData();
        }
    }
}
