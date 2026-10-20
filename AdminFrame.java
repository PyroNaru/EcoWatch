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

        ImageIcon originalIcon =
        new ImageIcon("assets/WhiteFramelessLogo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 25, Image.SCALE_SMOOTH);
        JLabel brandIcon = new JLabel(new ImageIcon(scaledImage));
        
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
        JButton deleteBtn = UITheme.createDangerButton("Delete Report");
        JButton changePassBtn = UITheme.createSecondaryButton("Change Password");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        bottomBar.add(reviewBtn);
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