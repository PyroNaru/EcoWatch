import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Production-ready report table panel featuring real-time live search,
 * multi-criteria category & status filtering, interactive KPI cards,
 * column sorting, empty state feedback, and CSV data export.
 */
public class ReportTablePanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Category", "Severity", "Status", "Date", "Location", "Description"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(14);

    // Filter dropdowns
    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"All Categories", "CLIMATE", "MARINE", "LAND", "WILDLIFE"});
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"All Statuses", "PENDING", "UNDER REVIEW", "RESOLVED"});
    private final JComboBox<String> severityCombo = new JComboBox<>(new String[]{"All Severities", "LOW", "MEDIUM", "HIGH"});

    // Clickable KPI counters
    private final JLabel statTotal = new JLabel("Total: 0");
    private final JLabel statPending = new JLabel("Pending: 0");
    private final JLabel statReview = new JLabel("Under Review: 0");
    private final JLabel statResolved = new JLabel("Resolved: 0");
    private final JLabel countFeedback = new JLabel("Showing 0 reports");

    private Consumer<String> doubleClickAction;

    public ReportTablePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BG_APP);

        // --- Top Container: Filters & Metrics ---
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UITheme.BG_APP);

        // Filter Bar (Top row)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBackground(UITheme.BG_APP);

        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(UITheme.FONT_BODY_BOLD);
        searchIcon.setForeground(UITheme.TEXT_MAIN);

        searchField.setFont(UITheme.FONT_BODY);
        searchField.setToolTipText("Filter by keyword, location, or report ID");
        searchField.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));

        styleFilterCombo(categoryCombo);
        styleFilterCombo(statusCombo);
        styleFilterCombo(severityCombo);

        JButton resetBtn = UITheme.createSecondaryButton("Reset Filters");
        JButton exportBtn = UITheme.createSecondaryButton("📥 Export CSV");
        exportBtn.setToolTipText("Export currently filtered reports to a CSV file");

        filterBar.add(searchIcon);
        filterBar.add(searchField);
        filterBar.add(categoryCombo);
        filterBar.add(statusCombo);
        filterBar.add(severityCombo);
        filterBar.add(resetBtn);
        filterBar.add(exportBtn);

        // Interactive Stats Strip (Bottom row)
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statsBar.setBackground(UITheme.BG_APP);

        formatStatPill(statTotal, UITheme.TEXT_MAIN, new Color(241, 245, 249), "Click to show all reports");
        formatStatPill(statPending, UITheme.STATUS_PENDING_TXT, UITheme.STATUS_PENDING_BG, "Click to filter by Pending");
        formatStatPill(statReview, UITheme.STATUS_REVIEW_TXT, UITheme.STATUS_REVIEW_BG, "Click to filter by Under Review");
        formatStatPill(statResolved, UITheme.STATUS_RESOLVED_TXT, UITheme.STATUS_RESOLVED_BG, "Click to filter by Resolved");

        countFeedback.setFont(UITheme.FONT_BODY);
        countFeedback.setForeground(UITheme.TEXT_MUTED);

        statsBar.add(new JLabel("Quick Filters:"));
        statsBar.add(statTotal);
        statsBar.add(statPending);
        statsBar.add(statReview);
        statsBar.add(statResolved);
        statsBar.add(Box.createHorizontalStrut(12));
        statsBar.add(countFeedback);

        topContainer.add(filterBar, BorderLayout.NORTH);
        topContainer.add(statsBar, BorderLayout.SOUTH);

        // --- Table Styling ---
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setFont(UITheme.FONT_BODY);
        table.setGridColor(UITheme.BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(220, 252, 231));
        table.setSelectionForeground(UITheme.TEXT_MAIN);
        table.setAutoCreateRowSorter(true); // Column sorting

        // Header
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(UITheme.TEXT_MAIN);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(65);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(95);   // Category
        table.getColumnModel().getColumn(2).setPreferredWidth(85);   // Severity
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(95);   // Date
        table.getColumnModel().getColumn(5).setPreferredWidth(180);  // Location
        table.getColumnModel().getColumn(6).setPreferredWidth(300);  // Description

        // Renderers
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(new Color(220, 252, 231));
                    c.setForeground(UITheme.TEXT_MAIN);
                } else {
                    c.setBackground(row % 2 == 1 ? new Color(248, 250, 252) : Color.WHITE);
                    c.setForeground(UITheme.TEXT_MAIN);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        DefaultTableCellRenderer centerZebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    c.setBackground(new Color(220, 252, 231));
                    c.setForeground(UITheme.TEXT_MAIN);
                } else {
                    c.setBackground(row % 2 == 1 ? new Color(248, 250, 252) : Color.WHITE);
                    c.setForeground(UITheme.TEXT_MAIN);
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerZebraRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerZebraRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new UITheme.BadgeCellRenderer(false));
        table.getColumnModel().getColumn(3).setCellRenderer(new UITheme.BadgeCellRenderer(true));
        table.getColumnModel().getColumn(4).setCellRenderer(centerZebraRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(zebraRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(zebraRenderer);

        // Double-click row listener
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Event Wiring ---
        // Real-time live search on typing
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        categoryCombo.addActionListener(e -> refresh());
        statusCombo.addActionListener(e -> refresh());
        severityCombo.addActionListener(e -> refresh());

        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
            severityCombo.setSelectedIndex(0);
            refresh();
        });

        exportBtn.addActionListener(e -> exportToCSV());

        // KPI card click actions
        statTotal.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                statusCombo.setSelectedIndex(0);
            }
        });
        statPending.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                statusCombo.setSelectedItem("PENDING");
            }
        });
        statReview.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                statusCombo.setSelectedItem("UNDER REVIEW");
            }
        });
        statResolved.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                statusCombo.setSelectedItem("RESOLVED");
            }
        });

        refresh();
    }

    private void styleFilterCombo(JComboBox<?> box) {
        box.setFont(UITheme.FONT_BODY);
        box.setBackground(Color.WHITE);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void formatStatPill(JLabel label, Color fg, Color bg, String tooltip) {
        label.setFont(UITheme.FONT_STAT_LBL);
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setBorder(new CompoundBorder(
                new LineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText(tooltip);
    }

    public void setOnRowDoubleClicked(Consumer<String> action) {
        this.doubleClickAction = action;
    }

    /** Re-reads reports.txt, filters records based on active criteria, and updates metrics. */
    public void refresh() {
        model.setRowCount(0);
        List<EnvironmentalReport> allReports = FileManager.loadReports();

        // Calculate overarching metrics
        int total = allReports.size();
        int pending = 0, review = 0, resolved = 0;
        for (EnvironmentalReport r : allReports) {
            String s = r.getStatus().toUpperCase();
            if (s.equals("PENDING")) pending++;
            else if (s.equals("UNDER REVIEW")) review++;
            else if (s.equals("RESOLVED")) resolved++;
        }

        statTotal.setText("Total: " + total);
        statPending.setText("Pending: " + pending);
        statReview.setText("In Review: " + review);
        statResolved.setText("Resolved: " + resolved);

        // Filter criteria
        String kw = searchField.getText().trim().toLowerCase();
        String catFilter = (String) categoryCombo.getSelectedItem();
        String statFilter = (String) statusCombo.getSelectedItem();
        String sevFilter = (String) severityCombo.getSelectedItem();

        int displayedCount = 0;
        for (EnvironmentalReport r : allReports) {
            // Category filter
            if (!catFilter.equals("All Categories") && !r.getType().equalsIgnoreCase(catFilter)) {
                continue;
            }
            // Status filter
            if (!statFilter.equals("All Statuses") && !r.getStatus().equalsIgnoreCase(statFilter)) {
                continue;
            }
            // Severity filter
            if (!sevFilter.equals("All Severities") && !r.getSeverity().equalsIgnoreCase(sevFilter)) {
                continue;
            }
            // Keyword search (ID, Type, Severity, Status, Description, Location, Category Details)
            if (!kw.isEmpty()) {
                String haystack = (r.getReportID() + " " + r.getType() + " " + r.getSeverity() + " "
                        + r.getStatus() + " " + r.getDescription() + " " + r.getLocation().getFullAddress()
                        + " " + r.getCategoryDetails()).toLowerCase();
                if (!haystack.contains(kw)) continue;
            }

            model.addRow(new Object[]{
                    r.getReportID(),
                    r.getType(),
                    r.getSeverity(),
                    r.getStatus(),
                    r.getDateReported(),
                    r.getLocation().getFullAddress(),
                    r.getDescription()
            });
            displayedCount++;
        }

        countFeedback.setText("Showing " + displayedCount + " of " + total + " reports");
    }

    public String getSelectedReportID() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (String) model.getValueAt(modelRow, 0);
    }

    /** Exports current table view to a standard CSV file with headers. */
    private void exportToCSV() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No reports to export in the current view.", "Export CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Reports to CSV");
        chooser.setSelectedFile(new File("EcoWatch_Reports_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheet (*.csv)", "csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".csv")) {
            target = new File(target.getAbsolutePath() + ".csv");
        }

        try (FileWriter out = new FileWriter(target)) {
            // Write CSV Headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                out.write("\"" + model.getColumnName(col) + "\"");
                if (col < model.getColumnCount() - 1) out.write(",");
            }
            out.write("\n");

            // Write Rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    String text = val == null ? "" : val.toString().replace("\"", "\"\"");
                    out.write("\"" + text + "\"");
                    if (col < model.getColumnCount() - 1) out.write(",");
                }
                out.write("\n");
            }
            JOptionPane.showMessageDialog(this,
                    "Successfully exported " + model.getRowCount() + " records to:\n" + target.getName(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}