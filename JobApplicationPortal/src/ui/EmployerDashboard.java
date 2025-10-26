package ui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class EmployerDashboard extends JFrame implements ActionListener {
    private JTextField titleField;
    private JTextArea descArea;
    private JButton postButton, refreshButton, backButton;
    private JTable jobTable, applicantsTable;
    private DefaultTableModel model, appModel;
    private String username;

    public EmployerDashboard(String username) {
        this.username = username;

        setTitle("Employer Dashboard");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(244, 245, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Employer Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setOpaque(true);
        header.setBackground(new Color(42, 90, 217));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(900, 60));
        backButton = new JButton("← Logout");
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(header, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(tabs, BorderLayout.CENTER);

        // ============= TAB 1: Post Job =============
        JPanel postPanel = new JPanel(new BorderLayout(10, 10));
        postPanel.setBackground(new Color(244, 245, 250));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        formPanel.setBackground(new Color(244, 245, 250));

        titleField = new JTextField();
        descArea = new JTextArea(3, 20);
        postButton = new JButton("Post Job");
        refreshButton = new JButton("Refresh");

        formPanel.add(new JLabel("Job Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descArea));
        formPanel.add(postButton);
        formPanel.add(refreshButton);
        postPanel.add(formPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Title", "Description"}, 0);
        jobTable = new JTable(model);
        jobTable.setRowHeight(28);
        jobTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jobTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        postPanel.add(new JScrollPane(jobTable), BorderLayout.CENTER);
        tabs.addTab("Post & Manage Jobs", postPanel);

        // ============= TAB 2: View Applicants =============
        JPanel applicantsPanel = new JPanel(new BorderLayout(10, 10));
        appModel = new DefaultTableModel(new Object[]{"Job Title", "Candidate", "Status"}, 0);
        applicantsTable = new JTable(appModel);
        applicantsTable.setRowHeight(28);
        applicantsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        applicantsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        applicantsPanel.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);
        tabs.addTab("View Applicants", applicantsPanel);

        // Button actions
        postButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        // Load data
        loadJobs();
        loadApplicants();

        setVisible(true);
    }

    private void loadJobs() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM jobs WHERE employer=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadApplicants() {
        appModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT j.title, a.candidate, a.status 
                FROM applications a
                JOIN jobs j ON a.job_id = j.id
                WHERE j.employer = ?
                ORDER BY j.title;
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appModel.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getString("candidate"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == postButton) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO jobs (title, description, employer) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, titleField.getText());
                ps.setString(2, descArea.getText());
                ps.setString(3, username);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Job Posted Successfully!");
                titleField.setText("");
                descArea.setText("");
                loadJobs();
                loadApplicants();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == refreshButton) {
            loadJobs();
            loadApplicants();
        }
    }
}