package ui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private JTable userTable;
    private DefaultTableModel model;
    private JButton toggleButton, refreshButton, backButton;

    public AdminDashboard() {
        setTitle("Admin Panel");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(244, 245, 250));
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("User Management Panel", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(42, 90, 217));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Username", "Role", "Status"}, 0);
        userTable = new JTable(model);
        userTable.setRowHeight(28);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        toggleButton = new JButton("Toggle Status");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("← Logout");
        bottom.add(toggleButton);
        bottom.add(refreshButton);
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);

        toggleButton.addActionListener(e -> toggleStatus());
        refreshButton.addActionListener(e -> loadUsers());
        backButton.addActionListener(e -> { dispose(); new LoginPage(); });

        loadUsers();
        setVisible(true);
    }

    private void loadUsers() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void toggleStatus() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first!");
            return;
        }
        String username = (String) model.getValueAt(row, 1);
        String current = (String) model.getValueAt(row, 3);
        String newStatus = current.equals("Active") ? "Inactive" : "Active";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET status=? WHERE username=?");
            ps.setString(1, newStatus);
            ps.setString(2, username);
            ps.executeUpdate();
            loadUsers();
        } catch (Exception e) { e.printStackTrace(); }
    }
}