import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {
    private final Admin admin;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("EcoWatch - Administrator Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 540);
        setLocationRelativeTo(null);

        // Header info bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getName() + " (Administrator)");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Action Toolbar
        JButton reviewBtn = new JButton("Review & Manage");
        JButton statusBtn = new JButton("Update Status");
        JButton actionBtn = new JButton("Add Action");
        JButton deleteBtn = new JButton("Delete Report");
        JButton changePassBtn = new JButton("Change Password");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(reviewBtn);
        buttons.add(statusBtn);
        buttons.add(actionBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(changePassBtn);
        buttons.add(logoutBtn);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(tablePanel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        // Listeners
        reviewBtn.addActionListener(e -> review());
        statusBtn.addActionListener(e -> updateStatus());
        actionBtn.addActionListener(e -> addAction());
        deleteBtn.addActionListener(e -> delete());
        refreshBtn.addActionListener(e -> tablePanel.refresh());
        tablePanel.setOnRowDoubleClicked(this::showReviewDialog);

        changePassBtn.addActionListener(e -> {
            new ChangePasswordDialog(this, admin).setVisible(true);
        });

        logoutBtn.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    /** @return selected report ID, or null (and shows a message) if nothing is selected */
    private String selected() {
        String id = tablePanel.getSelectedReportID();
        if (id == null) JOptionPane.showMessageDialog(this, "Please select a report first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return id;
    }

    private void review() {
        String id = selected();
        if (id == null) return;
        showReviewDialog(id);
    }

    private void showReviewDialog(String reportID) {
        EnvironmentalReport r = FileManager.findReport(reportID);
        if (r == null) return;
        ReportDetailsDialog dialog = new ReportDetailsDialog(this, r, admin);
        dialog.setVisible(true);
        if (dialog.hasChanged()) {
            tablePanel.refresh();
        }
    }

    private void updateStatus() {
        String id = selected();
        if (id == null) return;
        EnvironmentalReport r = FileManager.findReport(id);
        if (r == null) return;

        String[] options = {"PENDING", "UNDER REVIEW", "RESOLVED"};
        String choice = (String) JOptionPane.showInputDialog(this, "New status for " + id + ":",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, options, r.getStatus());
        if (choice == null || choice.equals(r.getStatus())) return;
        admin.updateStatus(id, choice);
        tablePanel.refresh();
        JOptionPane.showMessageDialog(this, "Status updated to " + choice + ".");
    }

    private void addAction() {
        String id = selected();
        if (id == null) return;
        JTextField action = new JTextField(20);
        JTextField remarks = new JTextField(20);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Action taken:")); p.add(action);
        p.add(new JLabel("Remarks:"));      p.add(remarks);
        int choice = JOptionPane.showConfirmDialog(this, p, "Add action to " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String a = FileManager.clean(action.getText());
        String rem = FileManager.clean(remarks.getText());
        if (a.isEmpty() || rem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        admin.addAction(id, a, rem);
        tablePanel.refresh();
        JOptionPane.showMessageDialog(this, "Action recorded.");
    }

    private void delete() {
        String id = selected();
        if (id == null) return;
        int sure = JOptionPane.showConfirmDialog(this,
                "Delete " + id + " and all its follow-up actions and location data?", "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (sure != JOptionPane.YES_OPTION) return;
        admin.deleteReport(id);
        tablePanel.refresh();
        JOptionPane.showMessageDialog(this, "Report " + id + " deleted successfully.");
    }
}