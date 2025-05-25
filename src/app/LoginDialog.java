package app;

import java.awt.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);
    private String loggedInUser = null;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        btnOk.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            if ("admin".equals(u) && "admin".equals(p)) {
                loggedInUser = u;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        // Layout using GroupLayout
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(lblUser)
                .addComponent(lblPass))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(txtUser)
                .addComponent(txtPass)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnOk)
                    .addComponent(btnCancel)))
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblUser)
                .addComponent(txtUser))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblPass)
                .addComponent(txtPass))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnOk)
                .addComponent(btnCancel))
        );

        add(panel);
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }
}
