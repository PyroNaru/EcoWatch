import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminFrame extends JFrame {
    private final Admin admin;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("EcoWatch - Administrator Control Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 640);
        UITheme.setupWindow(this, 1050, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_APP);

        // --- Top Navigation Header ---
        JPanel navBar = new JPanel(new BorderLayout(12, 0));
        navBar.setBackground(new Color(15, 23, 42)); // Modern deep slate
        navBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setOpaque(false);
        JLabel brandIcon = new JLabel("🛡️");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel brandTitle = new JLabel("EcoWatch");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(Color.WHITE);
        JLabel brandSub = new JLabel(" |  System Administration");
        brandSub.setFont(UITheme.FONT_SUBTITLE);
        brandSub.setForeground(new Color(148, 163, 184));

        brandPanel.add(brandIcon);
        brandPanel.add(brandTitle);
        brandPanel.add(brandSub);

        // Right User Profile & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + admin.getName());
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("ADMINISTRATOR");
        roleBadge.setFont(UITheme.FONT_BADGE);
        roleBadge.setForeground(new Color(15, 23, 42));
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(254, 243, 199)); // Amber badge
        roleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.setBorder(new EmptyBorder(5, 12, 5, 12));

        userPanel.add(userLabel);
        userPanel.add(roleBadge);
        userPanel.add(logoutBtn);

        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(userPanel, BorderLayout.EAST);

        // --- Action Toolbar (Bottom) ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottomBar.setBackground(UITheme.BG_APP);

        JButton reviewBtn = UITheme.createPrimaryButton("Review & Manage");
        JButton statusBtn = UITheme.createSecondaryButton("Update Status");
        JButton actionBtn = UITheme.createSecondaryButton("Add Action");
        JButton deleteBtn = UITheme.createDangerButton("Delete Report");
        JButton changePassBtn = UITheme.createSecondaryButton("Change Password");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        bottomBar.add(reviewBtn);
        bottomBar.add(statusBtn);
        bottomBar.add(actionBtn);
        bottomBar.add(deleteBtn);
        bottomBar.add(refreshBtn);
        bottomBar.add(changePassBtn);

        // --- Layout ---
        JPanel contentArea = new JPanel(new BorderLayout(10, 10));
        contentArea.setBackground(UITheme.BG_APP);
        contentArea.setBorder(new EmptyBorder(12, 16, 10, 16));
        contentArea.add(tablePanel, BorderLayout.CENTER);
        contentArea.add(bottomBar, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.add(navBar, BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);

        // --- Event Listeners ---
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

    private String selected() {
        String id = tablePanel.getSelectedReportID();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a report from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
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
        String choice = (String) JOptionPane.showInputDialog(this, "Select new status for " + id + ":",
                "Update Report Status", JOptionPane.PLAIN_MESSAGE, null, options, r.getStatus());
        if (choice == null || choice.equals(r.getStatus())) return;

        admin.updateStatus(id, choice);
        tablePanel.refresh();
        JOptionPane.showMessageDialog(this, "Report " + id + " updated to " + choice + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addAction() {
        String id = selected();
        if (id == null) return;
        JTextField action = new JTextField(20);
        JTextField remarks = new JTextField(20);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Action Taken:")); p.add(action);
        p.add(new JLabel("Remarks:"));      p.add(remarks);

        int choice = JOptionPane.showConfirmDialog(this, p, "Record Action for " + id,
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
        JOptionPane.showMessageDialog(this, "Action recorded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void delete() {
        String id = selected();
        if (id == null) return;
        int sure = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete report " + id + "?\nAll associated location data and action logs will be permanently removed.",
                "Confirm Cascade Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (sure != JOptionPane.YES_OPTION) return;

        admin.deleteReport(id);
        tablePanel.refresh();
        JOptionPane.showMessageDialog(this, "Report " + id + " has been deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
    }
}