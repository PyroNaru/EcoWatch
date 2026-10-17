import javax.swing.*;
import java.awt.*;

public class ObserverFrame extends JFrame {
    private final Observer observer;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public ObserverFrame(Observer observer) {
        this.observer = observer;
        setTitle("EcoWatch - Observer Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 520);
        setLocationRelativeTo(null);

        // Header info bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel welcomeLabel = new JLabel("Welcome, " + observer.getName() + " (Observer)");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Action Toolbar
        JButton submitBtn = new JButton("Submit New Report");
        JButton viewDetailsBtn = new JButton("View Details & Actions");
        JButton changePassBtn = new JButton("Change Password");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(submitBtn);
        buttons.add(viewDetailsBtn);
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