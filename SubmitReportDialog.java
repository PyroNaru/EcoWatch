import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Pop-up form where an Observer fills in a new report. */
public class SubmitReportDialog extends JDialog {
    private final Observer observer;
    private boolean submitted = false;

    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Climate", "Marine", "Wildlife", "Land"});
    private final JTextArea descArea = new JTextArea(4, 22);
    private final JComboBox<String> severityBox = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
    private final JTextField barangayField = new JTextField(22);
    private final JTextField municipalityField = new JTextField(22);
    private final JLabel extra1Label = new JLabel();
    private final JLabel extra2Label = new JLabel("Wildlife issue:");
    private final JTextField extra1Field = new JTextField(22);
    private final JTextField extra2Field = new JTextField(22);

    public SubmitReportDialog(Frame owner, Observer observer) {
        super(owner, "Submit Environmental Report", true);   // true = modal
        this.observer = observer;

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        addRow(form, 0, new JLabel("Category:"), categoryBox);
        addRow(form, 1, new JLabel("Description:"), new JScrollPane(descArea));
        addRow(form, 2, new JLabel("Severity:"), severityBox);
        addRow(form, 3, new JLabel("Barangay:"), barangayField);
        addRow(form, 4, new JLabel("Municipality/City:"), municipalityField);
        addRow(form, 5, extra1Label, extra1Field);
        addRow(form, 6, extra2Label, extra2Field);

        JButton submitBtn = new JButton("Submit");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(submitBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        categoryBox.addActionListener(e -> updateExtraFields());
        submitBtn.addActionListener(e -> doSubmit());
        cancelBtn.addActionListener(e -> dispose());

        updateExtraFields();
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, int row, JComponent label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        p.add(label, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, c);
    }

    /** Changes the category-specific labels depending on the chosen category. */
    private void updateExtraFields() {
        String cat = (String) categoryBox.getSelectedItem();
        boolean wildlife = cat.equals("Wildlife");
        switch (cat) {
            case "Climate":  extra1Label.setText("Weather type:"); break;
            case "Marine":   extra1Label.setText("Marine issue:"); break;
            case "Wildlife": extra1Label.setText("Species:");      break;
            default:         extra1Label.setText("Land issue:");
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
            JOptionPane.showMessageDialog(this, "Please fill in every field.", "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        observer.createReport(report);
        submitted = true;
        dispose();
    }

    public boolean wasSubmitted() { return submitted; }
}