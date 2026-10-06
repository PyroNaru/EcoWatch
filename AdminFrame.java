import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {
    private final Admin admin;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("EcoWatch - Admin: " + admin.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null);

        JButton reviewBtn = new JButton("Review");
        JButton statusBtn = new JButton("Update Status");
        JButton actionBtn = new JButton("Add Action");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : new JButton[]{reviewBtn, statusBtn, actionBtn, deleteBtn, refreshBtn, logoutBtn}) buttons.add(b);

        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(tablePanel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        reviewBtn.addActionListener(e -> review());
        statusBtn.addActionListener(e -> updateStatus());
        actionBtn.addActionListener(e -> addAction());
        deleteBtn.addActionListener(e -> delete());
        refreshBtn.addActionListener(e -> tablePanel.refresh());
        logoutBtn.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    /** @return selected report ID, or null (and shows a message) if nothing is selected */
    private String selected() {
        String id = tablePanel.getSelectedReportID();
        if (id == null) JOptionPane.showMessageDialog(this, "Please select a report first.");
        return id;
    }

    private void review() {
        String id = selected();
        if (id == null) return;
        EnvironmentalReport r = FileManager.findReport(id);
        if (r == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Report ID: ").append(r.getReportID()).append("\n");
        sb.append("Category: ").append(r.getType()).append("\n");
        sb.append(r.getCategoryDetails()).append("\n");          // polymorphic call
        sb.append("Severity: ").append(r.getSeverity()).append("\n");
        sb.append("Status: ").append(r.getStatus()).append("\n");
        sb.append("Date: ").append(r.getDateReported()).append("\n");
        sb.append("Location: ").append(r.getLocation().getFullAddress()).append("\n");
        sb.append("Description: ").append(r.getDescription()).append("\n\n");
        sb.append("Follow-up actions:\n");
        if (r.getActions().isEmpty()) sb.append("  (none yet)");
        for (ReportAction a : r.getActions()) sb.append("  - ").append(a.getDetails()).append("\n");

        JTextArea area = new JTextArea(sb.toString(), 14, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Review Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateStatus() {
        String id = selected();
        if (id == null) return;
        String[] options = {"PENDING", "UNDER REVIEW", "RESOLVED"};
        String choice = (String) JOptionPane.showInputDialog(this, "New status for " + id + ":",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == null) return;
        admin.updateStatus(id, choice);
        tablePanel.refresh();
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
        JOptionPane.showMessageDialog(this, "Action recorded.");
    }

    private void delete() {
        String id = selected();
        if (id == null) return;
        int sure = JOptionPane.showConfirmDialog(this,
                "Delete " + id + " and all its follow-up actions?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (sure != JOptionPane.YES_OPTION) return;
        admin.deleteReport(id);
        tablePanel.refresh();
    }
}