import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Reusable dialog displaying comprehensive details and chronological
 * action history for a report. Used by both Observer and Admin.
 */
public class ReportDetailsDialog extends JDialog {
    private final EnvironmentalReport report;
    private final boolean isAdmin;
    private final Admin admin;
    private final DefaultTableModel actionsModel;
    private final JTable actionsTable;
    private final JLabel statusLabel;
    private boolean changed = false;

    public ReportDetailsDialog(Frame owner, EnvironmentalReport report, User currentUser) {
        super(owner, "Report Details - " + report.getReportID(), true);
        this.report = report;
        this.isAdmin = currentUser instanceof Admin;
        this.admin = isAdmin ? (Admin) currentUser : null;

        setSize(700, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel: Metadata & Geolocation ---
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 8, 4));
        topPanel.setBorder(BorderFactory.createTitledBorder("Report Overview"));

        statusLabel = new JLabel(report.getStatus());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        topPanel.add(new JLabel("Report ID: " + report.getReportID()));
        topPanel.add(new JLabel("Category: " + report.getType()));
        topPanel.add(new JLabel("Submitted By: " + report.getSubmitterID()));
        topPanel.add(new JLabel("Date Reported: " + report.getDateReported()));
        topPanel.add(new JLabel("Severity: " + report.getSeverity()));

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrapper.add(new JLabel("Status: "));
        statusWrapper.add(statusLabel);
        topPanel.add(statusWrapper);

        topPanel.add(new JLabel("Location: " + report.getLocation().getFullAddress()));
        topPanel.add(new JLabel("Details: " + report.getCategoryDetails()));

        // --- Center Panel: Description & Actions ---
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Description Box
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Report Description"));
        JTextArea descArea = new JTextArea(report.getDescription(), 4, 30);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        // Actions Table Box
        JPanel actionsPanel = new JPanel(new BorderLayout(5, 5));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Follow-up Action History"));

        actionsModel = new DefaultTableModel(new String[]{"Action ID", "Date Taken", "Action Taken", "Remarks"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        actionsTable = new JTable(actionsModel);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionsTable.setRowHeight(22);
        loadActions();

        actionsPanel.add(new JScrollPane(actionsTable), BorderLayout.CENTER);

        // Admin action management toolbar inside actions panel
        if (isAdmin) {
            JPanel actionTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
            JButton addActionBtn = new JButton("Add Action");
            JButton editActionBtn = new JButton("Edit Selected Action");
            JButton updateStatusBtn = new JButton("Change Status");

            actionTools.add(updateStatusBtn);
            actionTools.add(addActionBtn);
            actionTools.add(editActionBtn);
            actionsPanel.add(actionTools, BorderLayout.SOUTH);

            addActionBtn.addActionListener(e -> doAddAction());
            editActionBtn.addActionListener(e -> doEditAction());
            updateStatusBtn.addActionListener(e -> doUpdateStatus());
        }

        centerPanel.add(descPanel, BorderLayout.NORTH);
        centerPanel.add(actionsPanel, BorderLayout.CENTER);

        // --- Bottom Panel: Close Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadActions() {
        actionsModel.setRowCount(0);
        for (ReportAction a : report.getActions()) {
            actionsModel.addRow(new Object[]{
                    a.getActionID(),
                    a.getDateTaken(),
                    a.getActionTaken(),
                    a.getRemarks()
            });
        }
    }

    private void doAddAction() {
        if (!isAdmin || admin == null) return;
        JTextField actionField = new JTextField(20);
        JTextField remarksField = new JTextField(20);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Action Taken:")); p.add(actionField);
        p.add(new JLabel("Remarks:"));      p.add(remarksField);

        int choice = JOptionPane.showConfirmDialog(this, p, "Record Action for " + report.getReportID(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String a = FileManager.clean(actionField.getText());
        String rem = FileManager.clean(remarksField.getText());
        if (a.isEmpty() || rem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        admin.addAction(report.getReportID(), a, rem);
        // Refresh local actions from disk
        EnvironmentalReport updated = FileManager.findReport(report.getReportID());
        if (updated != null) {
            report.getActions().clear();
            report.getActions().addAll(updated.getActions());
            loadActions();
        }
        changed = true;
    }

    private void doEditAction() {
        if (!isAdmin || admin == null) return;
        int row = actionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an action from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String actionID = (String) actionsModel.getValueAt(row, 0);
        ReportAction selectedAction = null;
        for (ReportAction a : report.getActions()) {
            if (a.getActionID().equals(actionID)) {
                selectedAction = a;
                break;
            }
        }
        if (selectedAction == null) return;

        JTextField actionField = new JTextField(selectedAction.getActionTaken(), 20);
        JTextField remarksField = new JTextField(selectedAction.getRemarks(), 20);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Action Taken:")); p.add(actionField);
        p.add(new JLabel("Remarks:"));      p.add(remarksField);

        int choice = JOptionPane.showConfirmDialog(this, p, "Edit Action " + actionID,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String newAction = FileManager.clean(actionField.getText());
        String newRemarks = FileManager.clean(remarksField.getText());
        if (newAction.isEmpty() || newRemarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        admin.updateAction(report.getReportID(), actionID, newAction, newRemarks);
        selectedAction.update(newAction, newRemarks);
        loadActions();
        changed = true;
        JOptionPane.showMessageDialog(this, "Action updated successfully.");
    }

    private void doUpdateStatus() {
        if (!isAdmin || admin == null) return;
        String[] options = {"PENDING", "UNDER REVIEW", "RESOLVED"};
        String choice = (String) JOptionPane.showInputDialog(this, "Select status for " + report.getReportID() + ":",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, options, report.getStatus());
        if (choice == null || choice.equals(report.getStatus())) return;

        admin.updateStatus(report.getReportID(), choice);
        report.updateStatus(choice);
        statusLabel.setText(choice);
        changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }
}
