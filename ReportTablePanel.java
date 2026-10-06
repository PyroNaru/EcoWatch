import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Reusable panel: search bar + table of reports. Used by both ObserverFrame and AdminFrame. */
public class ReportTablePanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Category", "Severity", "Status", "Date", "Location", "Description"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(20);

    public ReportTablePanel() {
        setLayout(new BorderLayout(5, 5));

        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(showAllBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        showAllBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        refresh();
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