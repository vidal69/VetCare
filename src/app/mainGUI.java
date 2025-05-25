package app;

import javax.swing.*;
import java.awt.*;

import dbhandler.DBConnection;
import java.sql.Connection;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JPanel;
import app.LoginDialog;


public class mainGUI extends JFrame {
    // Track the current logged-in user
    private static String currentUser = null;
    private JList<String> navList;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel statusBar;

    // Module panels as fields
    private DoctorManager doctorManager;
    private ClientManager clientManager;
    private PatientManager patientManager;
    private AppointmentManager appointmentManager;
    private TransactionManager transactionManager;

    // Helper stubs — ensure these methods exist in each manager:
    private void clearDoctorSearch() { doctorManager.clearSearch(); }
    private void clearClientSearch() { clientManager.clearSearch(); }
    private void clearPatientSearch() { patientManager.clearSearch(); }
    private void clearAppointmentSearch() { appointmentManager.clearSearch(); }
    private void clearTransactionSearch() { transactionManager.clearSearch(); }

    /**
     * Check if the currently logged-in user is an administrator.
     */
    public static boolean isAdmin() {
        return "admin".equals(currentUser);
    }

    private final String[] modules = {"Doctors", "Clients", "Patients", "Appointments", "Transactions"};

    public mainGUI() {
        super("VetCare Management System");

        // Menu bar with only Help menu
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "VetCare v1.0\nA simple veterinary clinic manager.", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());

        // Initialize DB connection and status bar
        Connection conn = DBConnection.getConnection();
        statusBar = new JLabel(conn != null ? "Connected to VetCare" : "Database connection failed");

        // Status bar and clock
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(dtf);
            statusBar.setText((conn != null ? "Connected to VetCare" : "DB connection failed") + "  |  " + time);
        });
        timer.start();

        // Navigation list
        navList = new JList<>(modules);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setFixedCellWidth(120);
        navList.setSelectedIndex(0);
        navList.setEnabled(true);

        // Initialize module panels
        doctorManager = new DoctorManager();
        clientManager = new ClientManager();
        patientManager = new PatientManager() {
            @Override
            public void loadData() {
                super.loadData();
                // Override row rendering to show "N/A" for null ClientID
                // (Assuming model is updated in super.loadData)
                // If you have direct row adding code, do null check there.
            }
        };
        appointmentManager = new AppointmentManager() {
            @Override
            public void loadData() {
                super.loadData();
                // For AttendPatientManager: N/A for null DoctorID or PatientID
                // If you have direct row adding code, do null check there.
            }
        };
        transactionManager = new TransactionManager() {
            @Override
            public void loadData() {
                super.loadData();
                // For Transact_Client: N/A for null DoctorID or ClientID
                // If you have direct row adding code, do null check there.
            }
        };

        // Card panel setup
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(doctorManager, "Doctors");
        cardPanel.add(clientManager, "Clients");
        cardPanel.add(patientManager, "Patients");
        cardPanel.add(appointmentManager, "Appointments");
        cardPanel.add(transactionManager, "Transactions");

        // Navigation listener
        navList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = navList.getSelectedValue();
                cardLayout.show(cardPanel, sel);
                switch (sel) {
                    case "Doctors":
                        clearDoctorSearch();
                        doctorManager.loadData();
                        break;
                    case "Clients":
                        clearClientSearch();
                        clientManager.loadData();
                        break;
                    case "Patients":
                        clearPatientSearch();
                        patientManager.loadData();
                        break;
                    case "Appointments":
                        clearAppointmentSearch();
                        appointmentManager.loadData();
                        break;
                    case "Transactions":
                        clearTransactionSearch();
                        transactionManager.loadData();
                        break;
                }
            }
        });

        // Split pane: navigation and content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(navList), cardPanel);
        splitPane.setDividerLocation(150);

        // Toolbar



        // Layout
        // getContentPane().setLayout(new BorderLayout());
        // getContentPane().add(splitPane, BorderLayout.CENTER);
        // btnRefreshAll.doClick();
        // navList.setSelectedIndex(0);

        // Bottom panel with status bar and login control
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusBar, BorderLayout.WEST);

        //Login 
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> {
            if (currentUser == null) {
                // Not logged in: show login dialog
                LoginDialog dialog = new LoginDialog(this);
                dialog.setVisible(true);
                String user = dialog.getLoggedInUser();
                if (user != null) {
                    currentUser = user;
                    btnLogin.setText("Logout (" + currentUser + ")");
                    // TODO: enable/disable modules based on role
                }
            } else {
                // Logged in: perform logout
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    currentUser = null;
                    btnLogin.setText("Login");
                    // TODO: disable protected modules or reset UI
                }
            }
        });
        bottomPanel.add(btnLogin, BorderLayout.EAST);
        

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        // Add the split pane (with nav and card panel) to the center
        getContentPane().add(splitPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            navList.setSelectedIndex(0); // Triggers view and data load
        });

        // Trigger a full refresh on startup
        // btnRefreshAll.doClick();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new mainGUI().setVisible(true);
        });
    }
}
