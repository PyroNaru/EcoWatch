import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog enabling authenticated users (Observer or Admin)
 * to change their account password with validation and modern UI styling.
 */
public class ChangePasswordDialog extends JDialog {
    private final User user;
    private final JPasswordField currentPasswordField = new JPasswordField(16);
    private final JPasswordField newPasswordField = new JPasswordField(16);
    private final JPasswordField confirmPasswordField = new JPasswordField(16);
    private boolean changed = false;

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Account Security - Change Password", true);
        this.user = user;
        UITheme.setupWindow(this, 440, 320);
        getContentPane().setBackground(UITheme.BG_APP);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel("🔒 Change Password");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("User: " + user.getName());
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

        styleField(currentPasswordField);
        styleField(newPasswordField);
        styleField(confirmPasswordField);

        addRow(formCard, 0, "Current Password:", currentPasswordField);
        addRow(formCard, 1, "New Password:", newPasswordField);
        addRow(formCard, 2, "Confirm Password:", confirmPasswordField);

        // Buttons
        JButton saveBtn = UITheme.createPrimaryButton("Update Password");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(UITheme.BG_APP);
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(UITheme.BG_APP);
        content.setBorder(new EmptyBorder(12, 16, 12, 16));
        content.add(formCard, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        saveBtn.addActionListener(e -> doChangePassword());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void styleField(JPasswordField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);
        panel.add(lbl, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(field, c);
    }

    private void doChangePassword() {
        String currentPass = FileManager.clean(new String(currentPasswordField.getPassword()));
        String newPass = FileManager.clean(new String(newPasswordField.getPassword()));
        String confirmPass = FileManager.clean(new String(confirmPasswordField.getPassword()));

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "New password must be at least 4 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!user.changePassword(currentPass, newPass)) {
            JOptionPane.showMessageDialog(this, "Incorrect current password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileManager.updateUser(user);
        changed = true;
        JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isChanged() {
        return changed;
    }
}
