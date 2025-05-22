package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginDialog extends JDialog {
    private JTextField txtUser = new JTextField();
    private JPasswordField txtPass = new JPasswordField();
    private String loggedInUser = null;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        setLayout(new GridLayout(3, 2, 5, 5));
        add(new JLabel("Username:")); add(txtUser);
        add(new JLabel("Password:")); add(txtPass);

        JPanel panel = new JPanel();
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        panel.add(btnOk);
        panel.add(btnCancel);
        add(new JLabel());  // placeholder to align buttons
        add(panel);

        btnOk.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            // TODO: hook into real auth logic
            if ("admin".equals(u) && "admin".equals(p)) {
                loggedInUser = u;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }
}