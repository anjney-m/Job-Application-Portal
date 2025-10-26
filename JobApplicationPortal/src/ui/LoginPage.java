package ui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame implements ActionListener {
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton, registerButton;
    JLabel header;

    public LoginPage() {
        setTitle("Job Portal Login");
        setSize(420, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        header = new JLabel("Job Application Portal", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(42, 90, 217));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(420, 60));
        add(header, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(new Color(245, 247, 250));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);
        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM users WHERE username=? AND password=? AND status='Active'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, usernameField.getText());
                ps.setString(2, new String(passwordField.getPassword()));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String role = rs.getString("role");
                    dispose();
                    switch (role) {
                        case "Employer": new EmployerDashboard(usernameField.getText()); break;
                        case "Candidate": new CandidateDashboard(usernameField.getText()); break;
                        case "Admin": new AdminDashboard(); break;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials or inactive account!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == registerButton) {
            new RegistrationPage();
        }
    }
}