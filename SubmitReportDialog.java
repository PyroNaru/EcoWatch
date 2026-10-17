import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;

/** Pop-up form where an Observer fills in a new report. */
public class SubmitReportDialog extends JDialog {
    private final Observer observer;
    private boolean submitted = false;

    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Climate", "Marine", "Wildlife", "Land"});
    private final JTextArea descArea = new JTextArea(4, 24);
    private final JComboBox<String> severityBox = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
    private final JTextField barangayField = new JTextField(24);
    private final JTextField municipalityField = new JTextField(24);
    private final JLabel extra1Label = new JLabel("Weather Type:");
    private final JLabel extra2Label = new JLabel("Wildlife Issue:");
    private final JTextField extra1Field = new JTextField(24);
    private final JTextField extra2Field = new JTextField(24);

    public SubmitReportDialog(Frame owner, Observer observer) {
        super(owner, "Submit Environmental Report", true);
        this.observer = observer;
        UITheme.setupWindow(this, 520, 560);
        getContentPane().setBackground(UITheme.BG_APP);

        // Header Title Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel("🌿 New Environmental Report");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Logged as " + observer.getName() + " (" + observer.getUserID() + ")");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(220, 252, 231));

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(subLbl, BorderLayout.EAST);

        // Form Card
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(UITheme.FONT_BODY);

        styleInput(categoryBox);
        styleInput(severityBox);
        styleInput(barangayField);
        styleInput(municipalityField);
        styleInput(extra1Field);
        styleInput(extra2Field);

        addRow(formCard, 0, new JLabel("Category:"), categoryBox);
        addRow(formCard, 1, new JLabel("Description:"), new JScrollPane(descArea));
        addRow(formCard, 2, new JLabel("Severity:"), severityBox);
        addRow(formCard, 3, new JLabel("Barangay:"), barangayField);
        addRow(formCard, 4, new JLabel("Municipality/City:"), municipalityField);
        addRow(formCard, 5, extra1Label, extra1Field);
        addRow(formCard, 6, extra2Label, extra2Field);

        // Buttons
        JButton submitBtn = UITheme.createPrimaryButton("Submit Report");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(UITheme.BG_APP);
        buttons.add(cancelBtn);
        buttons.add(submitBtn);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(UITheme.BG_APP);
        content.setBorder(new EmptyBorder(12, 16, 12, 16));
        content.add(formCard, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        categoryBox.addActionListener(e -> updateExtraFields());
        submitBtn.addActionListener(e -> doSubmit());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(submitBtn);

        updateExtraFields();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void styleInput(JComponent comp) {
        comp.setFont(UITheme.FONT_BODY);
        if (comp instanceof JTextField) {
            comp.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.BORDER, 1, true),
                    new EmptyBorder(5, 8, 5, 8)
            ));
        }
    }

    private void addRow(JPanel p, int row, JComponent label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        p.add(label, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        p.add(field, c);
    }

    /** Changes the category-specific labels depending on the chosen category. */
    private void updateExtraFields() {
        String cat = (String) categoryBox.getSelectedItem();
        boolean wildlife = "Wildlife".equals(cat);
        switch (cat) {
            case "Climate":  extra1Label.setText("Weather Type:"); break;
            case "Marine":   extra1Label.setText("Marine Issue:"); break;
            case "Wildlife": extra1Label.setText("Species:");      break;
            default:         extra1Label.setText("Land Issue:");
        }
        extra2Label.setVisible(wildlife);
        extra2Field.setVisible(wildlife);
        pack();
    }

    private void doSubmit() {
        String desc = FileManager.clean(descArea.getText());
        String sev = (String) severityBox.getSelectedItem();
        Location loc = new Location(FileManager.nextId(FileManager.LOCATIONS, "L"),
                FileManager.clean(barangayField.getText()), FileManager.clean(municipalityField.getText()));
        String id = FileManager.nextId(FileManager.REPORTS, "R");
        String date = LocalDate.now().toString();
        String e1 = FileManager.clean(extra1Field.getText());
        String e2 = FileManager.clean(extra2Field.getText());

        EnvironmentalReport report;
        switch ((String) categoryBox.getSelectedItem()) {
            case "Climate":  report = new ClimateReport(id, observer.getUserID(), desc, sev, date, "PENDING", loc, e1); break;
            case "Marine":   report = new MarineReport(id, observer.getUserID(), desc, sev, date, "PENDING", loc, e1); break;
            case "Wildlife": report = new WildlifeReport(id, observer.getUserID(), desc, sev, date, "PENDING", loc, e1, e2); break;
            default:         report = new LandReport(id, observer.getUserID(), desc, sev, date, "PENDING", loc, e1);
        }

        if (!report.validateReport()) {
            JOptionPane.showMessageDialog(this, "Please fill in every field.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        observer.createReport(report);
        submitted = true;
        JOptionPane.showMessageDialog(this, "Report " + id + " submitted successfully!", "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean wasSubmitted() { return submitted; }
}