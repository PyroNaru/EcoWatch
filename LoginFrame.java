import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    public LoginFrame() {
        setTitle("EcoWatch - Authentication Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UITheme.setupWindow(this, 440, 560);
        getContentPane().setBackground(UITheme.BG_APP);

        // --- Outer Container ---
        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(UITheme.CARD_BG);
        mainCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(28, 36, 24, 36)
        ));

        // --- Brand Header ---
        JLabel iconLabel = new JLabel("🌱", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("EcoWatch", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Environmental Monitoring & Reporting", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Input Fields ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        UITheme.styleInput(usernameField);
        UITheme.styleInput(passwordField);

        addRow(form, 0, "Username", usernameField);
        addRow(form, 1, "Password", passwordField);

        // --- Action Buttons ---
        JButton loginBtn = UITheme.createPrimaryButton("Sign In");
        JButton registerBtn = UITheme.createSecondaryButton("Register as Observer");

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setBackground(UITheme.CARD_BG);
        buttons.add(loginBtn);
        buttons.add(registerBtn);


        // Assembly
        mainCard.add(iconLabel);
        mainCard.add(Box.createVerticalStrut(4));
        mainCard.add(title);
        mainCard.add(Box.createVerticalStrut(2));
        mainCard.add(subtitle);
        mainCard.add(Box.createVerticalStrut(8));
        mainCard.add(Box.createVerticalStrut(14));
        mainCard.add(form);
        mainCard.add(buttons);
        mainCard.add(Box.createVerticalStrut(12));

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_APP);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.add(mainCard);
        setContentPane(root);

        // Events
        loginBtn.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(loginBtn);

        pack();
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row * 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(row == 0 ? 0 : 8, 0, 4, 0);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);
        panel.add(lbl, c);

        c.gridy = row * 2 + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(comp, c);
    }

    private void doLogin() {
        String username = FileManager.clean(usernameField.getText());
        String password = new String(passwordField.getPassword());
        User user = FileManager.findUser(username);

        if (user == null || !user.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
        if (user instanceof Admin) new AdminFrame((Admin) user).setVisible(true);
        else new ObserverFrame((Observer) user).setVisible(true);
    }

    private void doRegister() {
        JTextField name = new JTextField(15);
        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        JPasswordField confirmPassword = new JPasswordField(15);

        UITheme.styleInput(name);
        UITheme.styleInput(username);
        UITheme.styleInput(password);
        UITheme.styleInput(confirmPassword);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Full Name:"));        p.add(name);
        p.add(new JLabel("Username:"));         p.add(username);
        p.add(new JLabel("Password:"));         p.add(password);
        p.add(new JLabel("Confirm Password:")); p.add(confirmPassword);

        int choice = JOptionPane.showConfirmDialog(this, p, "Register as Observer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String n = FileManager.clean(name.getText());
        String u = FileManager.clean(username.getText());
        String pw = FileManager.clean(new String(password.getPassword()));
        String cpw = FileManager.clean(new String(confirmPassword.getPassword()));

        if (n.isEmpty() || u.isEmpty() || pw.isEmpty() || cpw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pw.equals(cpw)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pw.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (FileManager.findUser(u) != null) {
            JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileManager.saveUser(new Observer(FileManager.nextId(FileManager.USERS, "U"), n, u, pw));
        JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
    }
}