import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Reusable dialog displaying comprehensive details and chronological
 * action history for a report with modern card styling.
 */
public class ReportDetailsDialog extends JDialog {
    private final EnvironmentalReport report;
    private final boolean isAdmin;
    private final Admin admin;
    private final DefaultTableModel actionsModel;
    private final JTable actionsTable;
    private final JLabel statusBadge;
    private boolean changed = false;

    public ReportDetailsDialog(Frame owner, EnvironmentalReport report, User currentUser) {
        super(owner, "Report Details - " + report.getReportID(), true);
        this.report = report;
        this.isAdmin = currentUser instanceof Admin;
        this.admin = isAdmin ? (Admin) currentUser : null;

        setSize(800, 640);
        UITheme.setupWindow(this, 750, 600);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_APP);

        // --- Top Header Banner ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(isAdmin ? new Color(15, 23, 42) : UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel("📋 Report Overview: " + report.getReportID());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);

        statusBadge = new JLabel(report.getStatus());
        statusBadge.setFont(UITheme.FONT_BADGE);
        updateStatusBadgeColor();
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(4, 12, 4, 12));

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(statusBadge, BorderLayout.EAST);

        // --- Main Content Container ---
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UITheme.BG_APP);
        mainContent.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Metadata Card
        JPanel metaCard = new JPanel(new GridLayout(3, 2, 12, 8));
        metaCard.setBackground(UITheme.CARD_BG);
        metaCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        addMetaField(metaCard, "Category", report.getType());
        addMetaField(metaCard, "Severity", report.getSeverity());
        addMetaField(metaCard, "Submitted By", report.getSubmitterID());
        addMetaField(metaCard, "Date Reported", report.getDateReported());
        addMetaField(metaCard, "Location", report.getLocation().getFullAddress());
        addMetaField(metaCard, "Category Details", report.getCategoryDetails());

        // Description Card
        JPanel descCard = new JPanel(new BorderLayout(6, 6));
        descCard.setBackground(UITheme.CARD_BG);
        descCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel descTitle = new JLabel("Problem Description");
        descTitle.setFont(UITheme.FONT_BODY_BOLD);
        descTitle.setForeground(UITheme.TEXT_MAIN);

        JTextArea descArea = new JTextArea(report.getDescription(), 3, 30);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(UITheme.FONT_BODY);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        descCard.add(descTitle, BorderLayout.NORTH);
        descCard.add(new JScrollPane(descArea), BorderLayout.CENTER);

        // Actions Table Card
        JPanel actionsCard = new JPanel(new BorderLayout(8, 8));
        actionsCard.setBackground(UITheme.CARD_BG);
        actionsCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel actionsTitle = new JLabel("Follow-up Action Log");
        actionsTitle.setFont(UITheme.FONT_BODY_BOLD);
        actionsTitle.setForeground(UITheme.TEXT_MAIN);

        actionsModel = new DefaultTableModel(new String[]{"Action ID", "Date", "Action Taken", "Remarks"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        actionsTable = new JTable(actionsModel);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionsTable.setRowHeight(28);
        actionsTable.setFont(UITheme.FONT_BODY);
        actionsTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        actionsTable.getTableHeader().setBackground(new Color(241, 245, 249));

        // Column widths
        actionsTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        actionsTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        actionsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        actionsTable.getColumnModel().getColumn(3).setPreferredWidth(240);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        actionsTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        actionsTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);

        loadActions();

        JScrollPane actionScroll = new JScrollPane(actionsTable);
        actionScroll.setPreferredSize(new Dimension(680, 140));

        actionsCard.add(actionsTitle, BorderLayout.NORTH);
        actionsCard.add(actionScroll, BorderLayout.CENTER);

        // Admin actions toolbar inside the card
        if (isAdmin) {
            JPanel adminTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            adminTools.setBackground(UITheme.CARD_BG);

            JButton addActionBtn = UITheme.createPrimaryButton("+ Add Action");
            JButton editActionBtn = UITheme.createSecondaryButton("Edit Selected Action");
            JButton updateStatusBtn = UITheme.createSecondaryButton("Change Status");

            adminTools.add(updateStatusBtn);
            adminTools.add(editActionBtn);
            adminTools.add(addActionBtn);
            actionsCard.add(adminTools, BorderLayout.SOUTH);

            addActionBtn.addActionListener(e -> doAddAction());
            editActionBtn.addActionListener(e -> doEditAction());
            updateStatusBtn.addActionListener(e -> doUpdateStatus());
        }

        mainContent.add(metaCard);
        mainContent.add(Box.createVerticalStrut(10));
        mainContent.add(descCard);
        mainContent.add(Box.createVerticalStrut(10));
        mainContent.add(actionsCard);

        // --- Bottom Close Button ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        bottomBar.setBackground(UITheme.BG_APP);
        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(mainContent, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void updateStatusBadgeColor() {
        String s = report.getStatus().toUpperCase();
        if (s.equals("PENDING")) {
            statusBadge.setForeground(UITheme.STATUS_PENDING_TXT);
            statusBadge.setBackground(UITheme.STATUS_PENDING_BG);
        } else if (s.equals("UNDER REVIEW")) {
            statusBadge.setForeground(UITheme.STATUS_REVIEW_TXT);
            statusBadge.setBackground(UITheme.STATUS_REVIEW_BG);
        } else {
            statusBadge.setForeground(UITheme.STATUS_RESOLVED_TXT);
            statusBadge.setBackground(UITheme.STATUS_RESOLVED_BG);
        }
    }

    private void addMetaField(JPanel panel, String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT_MAIN);

        p.add(lbl);
        p.add(val);
        panel.add(p);
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
        JOptionPane.showMessageDialog(this, "Action updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doUpdateStatus() {
        if (!isAdmin || admin == null) return;
        String[] options = {"PENDING", "UNDER REVIEW", "RESOLVED"};
        String choice = (String) JOptionPane.showInputDialog(this, "Select status for " + report.getReportID() + ":",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, options, report.getStatus());
        if (choice == null || choice.equals(report.getStatus())) return;

        admin.updateStatus(report.getReportID(), choice);
        report.updateStatus(choice);
        statusBadge.setText(choice);
        updateStatusBadgeColor();
        changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }
}
