import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/** Reusable panel: search bar + table of reports. Used by both ObserverFrame and AdminFrame. */
public class ReportTablePanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Category", "Severity", "Status", "Date", "Location", "Description"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(20);
    private Consumer<String> doubleClickAction;

    public ReportTablePanel() {
        setLayout(new BorderLayout(8, 8));

        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(new JLabel("Search Reports:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(showAllBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Column sizing
        table.getColumnModel().getColumn(0).setPreferredWidth(65);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(85);   // Category
        table.getColumnModel().getColumn(2).setPreferredWidth(75);   // Severity
        table.getColumnModel().getColumn(3).setPreferredWidth(105);  // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(90);   // Date
        table.getColumnModel().getColumn(5).setPreferredWidth(170);  // Location
        table.getColumnModel().getColumn(6).setPreferredWidth(280);  // Description

        // Center align ID, Category, Severity, Status, Date
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Double-click row handler
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && doubleClickAction != null) {
                    String selectedID = getSelectedReportID();
                    if (selectedID != null) {
                        doubleClickAction.accept(selectedID);
                    }
                }
            }
        });

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        showAllBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        refresh();
    }

    public void setOnRowDoubleClicked(Consumer<String> action) {
        this.doubleClickAction = action;
    }

    /** Re-reads reports.txt and fills the table (uses the search text as a filter). */
    public void refresh() {
        model.setRowCount(0);
        for (EnvironmentalReport r : FileManager.searchReports(searchField.getText().trim())) {
            model.addRow(new Object[]{ r.getReportID(), r.getType(), r.getSeverity(), r.getStatus(),
                    r.getDateReported(), r.getLocation().getFullAddress(), r.getDescription() });
        }
    }

    /** @return the selected report's ID, or null if no row is selected */
    public String getSelectedReportID() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) model.getValueAt(row, 0);
    }
}