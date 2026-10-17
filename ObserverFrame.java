import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ObserverFrame extends JFrame {
    private final Observer observer;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public ObserverFrame(Observer observer) {
        this.observer = observer;
        setTitle("EcoWatch - Observer Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1040, 620);
        UITheme.setupWindow(this, 1000, 580);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_APP);

        // --- Top Navigation Header ---
        JPanel navBar = new JPanel(new BorderLayout(12, 0));
        navBar.setBackground(UITheme.PRIMARY);
        navBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setOpaque(false);
        JLabel brandIcon = new JLabel("🌱");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel brandTitle = new JLabel("EcoWatch");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(Color.WHITE);
        JLabel brandSub = new JLabel(" |  Community Observer Portal");
        brandSub.setFont(UITheme.FONT_SUBTITLE);
        brandSub.setForeground(new Color(220, 252, 231));

        brandPanel.add(brandIcon);
        brandPanel.add(brandTitle);
        brandPanel.add(brandSub);

        // Right User Profile & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + observer.getName());
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("OBSERVER");
        roleBadge.setFont(UITheme.FONT_BADGE);
        roleBadge.setForeground(UITheme.PRIMARY);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(Color.WHITE);
        roleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.setBorder(new EmptyBorder(5, 12, 5, 12));

        userPanel.add(userLabel);
        userPanel.add(roleBadge);
        userPanel.add(logoutBtn);

        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(userPanel, BorderLayout.EAST);

        // --- Action Toolbar (Bottom) ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(UITheme.BG_APP);

        JButton submitBtn = UITheme.createPrimaryButton("+ Submit New Report");
        JButton viewDetailsBtn = UITheme.createSecondaryButton("View Details & Actions");
        JButton changePassBtn = UITheme.createSecondaryButton("Change Password");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        bottomBar.add(submitBtn);
        bottomBar.add(viewDetailsBtn);
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
        submitBtn.addActionListener(e -> {
            SubmitReportDialog d = new SubmitReportDialog(this, observer);
            d.setVisible(true);
            if (d.wasSubmitted()) tablePanel.refresh();
        });

        viewDetailsBtn.addActionListener(e -> {
            String selectedID = tablePanel.getSelectedReportID();
            if (selectedID == null) {
                JOptionPane.showMessageDialog(this, "Please select a report from the table to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showDetails(selectedID);
        });

        tablePanel.setOnRowDoubleClicked(this::showDetails);

        changePassBtn.addActionListener(e -> {
            new ChangePasswordDialog(this, observer).setVisible(true);
        });

        refreshBtn.addActionListener(e -> tablePanel.refresh());

        logoutBtn.addActionListener(e -> {
            observer.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void showDetails(String reportID) {
        EnvironmentalReport report = FileManager.findReport(reportID);
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Could not load report details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ReportDetailsDialog dialog = new ReportDetailsDialog(this, report, observer);
        dialog.setVisible(true);
        if (dialog.hasChanged()) {
            tablePanel.refresh();
        }
    }
}