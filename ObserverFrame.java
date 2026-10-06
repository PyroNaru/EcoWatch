import javax.swing.*;
import java.awt.*;

public class ObserverFrame extends JFrame {
    private final Observer observer;
    private final ReportTablePanel tablePanel = new ReportTablePanel();

    public ObserverFrame(Observer observer) {
        this.observer = observer;
        setTitle("EcoWatch - Observer: " + observer.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 480);
        setLocationRelativeTo(null);

        JButton submitBtn = new JButton("Submit New Report");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(submitBtn);
        buttons.add(refreshBtn);
        buttons.add(logoutBtn);

        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(tablePanel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        submitBtn.addActionListener(e -> {
            SubmitReportDialog d = new SubmitReportDialog(this, observer);
            d.setVisible(true);
            if (d.wasSubmitted()) tablePanel.refresh();
        });
        refreshBtn.addActionListener(e -> tablePanel.refresh());
        logoutBtn.addActionListener(e -> {
            observer.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}