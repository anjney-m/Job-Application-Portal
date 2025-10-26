package ui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class CandidateDashboard extends JFrame implements ActionListener {
    private JTable jobTable, appsTable;
    private JButton applyButton, refreshButton, backButton;
    private DefaultTableModel model, appsModel;
    private String username;

    public CandidateDashboard(String username) {
        this.username = username;

        setTitle("Candidate Dashboard");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(244, 245, 250));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // ----- JOB LIST TAB -----
        JPanel jobPanel = new JPanel(new BorderLayout(10, 10));
        model = new DefaultTableModel(new Object[]{"ID", "Title", "Description"}, 0);
        jobTable = new JTable(model);
        jobTable.setRowHeight(28);
        jobPanel.add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        applyButton = new JButton("Apply to Selected");
        refreshButton = new JButton("Refresh");
        bottom.add(applyButton);
        bottom.add(refreshButton);
        jobPanel.add(bottom, BorderLayout.SOUTH);
        tabs.addTab("Available Jobs", jobPanel);

        // ----- MY APPLICATIONS TAB -----
        JPanel appsPanel = new JPanel(new BorderLayout(10, 10));
        appsModel = new DefaultTableModel(new Object[]{"App ID", "Job Title", "Status"}, 0);
        appsTable = new JTable(appsModel);
        appsTable.setRowHeight(28);
        appsPanel.add(new JScrollPane(appsTable), BorderLayout.CENTER);
        tabs.addTab("My Applications", appsPanel);

        // ----- HEADER & BACK -----
        JPanel top = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Welcome, " + username, SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(42, 90, 217));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        backButton = new JButton("← Logout");
        top.add(backButton, BorderLayout.WEST);
        top.add(header, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        applyButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(e -> { dispose(); new LoginPage(); });

        loadJobs();
        loadApplications();
        setVisible(true);
    }

    private void loadJobs() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM jobs")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), rs.getString("description")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadApplications() {
        appsModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT a.id, j.title, a.status FROM applications a JOIN jobs j ON a.job_id=j.id WHERE a.candidate=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appsModel.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), rs.getString("status")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            int row = jobTable.getSelectedRow();
            if (row == -1) return;
            int jobId = (int) model.getValueAt(row, 0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO applications (job_id, candidate, status) VALUES (?, ?, 'Applied')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, jobId);
                ps.setString(2, username);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Application Submitted!");
                loadApplications();
            } catch (Exception ex) { ex.printStackTrace(); }
        } else if (e.getSource() == refreshButton) {
            loadJobs();
        }
    }
}