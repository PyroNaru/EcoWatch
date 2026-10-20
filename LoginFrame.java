import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    private JLabel createLogoLabel() {
        ImageIcon originalIcon = new ImageIcon("assets\\FramelessLogo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(80, 49, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage),SwingConstants.CENTER);

        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        return logoLabel;
    }

    public LoginFrame() {
        setTitle("EcoWatch - Authentication Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UITheme.setupWindow(this, 440, 560);
        getContentPane().setBackground(UITheme.BG_APP);

        // Main card
        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(UITheme.CARD_BG);
        mainCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(28, 36, 24, 36)
        ));

        // Header
        JLabel iconLabel = createLogoLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("EcoWatch", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Environmental Monitoring & Reporting",
                SwingConstants.CENTER
        );
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        UITheme.styleInput(usernameField);
        UITheme.styleInput(passwordField);

        addRow(form, 0, "Username", usernameField);
        addRow(form, 1, "Password", passwordField);

        // Buttons
        JButton loginBtn = UITheme.createPrimaryButton("Sign In");
        JButton registerBtn =
                UITheme.createSecondaryButton("Register as Observer");

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setBackground(UITheme.CARD_BG);
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        // Assemble login card
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

    private void addRow(
            JPanel panel, int row,
            String labelText, JComponent comp) {

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row * 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(row == 0 ? 0 : 8, 0, 4, 0);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);
        panel.add(lbl, c);

        c.gridy = row * 2 + 1;
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(comp, c);
    }

    private void doLogin() {
        String username = FileManager.clean(usernameField.getText());
        String password = new String(passwordField.getPassword());

        User user = FileManager.findUser(username);

        if (user == null || !user.login(username, password)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dispose();

        if (user instanceof Admin) {
            new AdminFrame((Admin) user).setVisible(true);
        } else {
            new ObserverFrame((Observer) user).setVisible(true);
        }
    }

    /**
     * Opens the registration dialog.
     */
    private void doRegister() {
        JDialog dialog = new JDialog(
                this, "Register as Observer", true
        );

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Use the same application background and window dimensions.
        UITheme.setupWindow(dialog, 440, 560);
        dialog.getContentPane().setBackground(UITheme.BG_APP);

        // Registration fields
        JTextField name = new JTextField(16);
        JTextField username = new JTextField(16);
        JPasswordField password = new JPasswordField(16);
        JPasswordField confirmPassword = new JPasswordField(16);

        UITheme.styleInput(name);
        UITheme.styleInput(username);
        UITheme.styleInput(password);
        UITheme.styleInput(confirmPassword);

        // Registration card: same styling as the login card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(24, 36, 24, 36)
        ));

        // Header
        JLabel iconLabel = createLogoLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(
                "Create Observer Account",
                SwingConstants.CENTER
        );
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Join us in protecting the environment",
                SwingConstants.CENTER
        );
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(12));

        // Registration form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(8, 0, 12, 0));

        addRow(form, 0, "Full Name", name);
        addRow(form, 1, "Username", username);
        addRow(form, 2, "Password", password);
        addRow(form, 3, "Confirm Password", confirmPassword);

        card.add(form);

        // Buttons
        JButton createButton =
                UITheme.createPrimaryButton("Create Account");

        JButton cancelButton =
                UITheme.createSecondaryButton("Cancel");

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setBackground(UITheme.CARD_BG);
        buttons.add(createButton);
        buttons.add(cancelButton);

        card.add(buttons);

        // Outer container, matching the login frame
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_APP);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.add(card);

        dialog.setContentPane(root);

        // Registration validation and account creation
        createButton.addActionListener(e -> {
            String n = FileManager.clean(name.getText());
            String u = FileManager.clean(username.getText());
            String pw = new String(password.getPassword());
            String cpw = new String(confirmPassword.getPassword());

            if (n.isEmpty() || u.isEmpty()
                    || pw.isEmpty() || cpw.isEmpty()) {
                showRegistrationMessage(
                        dialog,
                        "All fields are required.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!pw.equals(cpw)) {
                showRegistrationMessage(
                        dialog,
                        "Passwords do not match.",
                        "Check Password",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (pw.length() < 4) {
                showRegistrationMessage(
                        dialog,
                        "Password must be at least 4 characters.",
                        "Password Too Short",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (FileManager.findUser(u) != null) {
                showRegistrationMessage(
                        dialog,
                        "Username already taken.",
                        "Username Unavailable",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            FileManager.saveUser(
                    new Observer(
                            FileManager.nextId(FileManager.USERS, "U"),
                            n, u, pw
                    )
            );

            dialog.dispose();

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully! You can now log in.",
                    "Registration Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.getRootPane().setDefaultButton(createButton);

        // Fixed size, matching the login frame's configuration.
        dialog.setSize(440, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showRegistrationMessage(
            Component parent,
            String message,
            String title,
            int type) {

        JOptionPane.showMessageDialog(
                parent, message, title, type
        );
    }
}