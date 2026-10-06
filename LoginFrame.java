import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    public LoginFrame() {
        setTitle("EcoWatch - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("EcoWatch", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel subtitle = new JLabel("Environmental Monitoring and Reporting System", SwingConstants.CENTER);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.add(title);
        header.add(subtitle);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        JPanel root = new JPanel(new BorderLayout(10, 15));
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());   // Enter key
        registerBtn.addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(loginBtn);

        pack();
        setLocationRelativeTo(null);   // center on screen
    }

    private void doLogin() {
        String username = FileManager.clean(usernameField.getText());
        String password = new String(passwordField.getPassword());
        User user = FileManager.findUser(username);

        if (user == null || !user.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Wrong username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
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
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Full name:"));  p.add(name);
        p.add(new JLabel("Username:"));   p.add(username);
        p.add(new JLabel("Password:"));   p.add(password);

        int choice = JOptionPane.showConfirmDialog(this, p, "Register as Observer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String n = FileManager.clean(name.getText());
        String u = FileManager.clean(username.getText());
        String pw = FileManager.clean(new String(password.getPassword()));
        if (n.isEmpty() || u.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (FileManager.findUser(u) != null) {
            JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FileManager.saveUser(new Observer(FileManager.nextId(FileManager.USERS, "U"), n, u, pw));
        JOptionPane.showMessageDialog(this, "Registered! You can now log in.");
    }
}