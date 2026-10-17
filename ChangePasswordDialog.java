import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog enabling authenticated users (Observer or Admin)
 * to change their account password with validation.
 */
public class ChangePasswordDialog extends JDialog {
    private final User user;
    private final JPasswordField currentPasswordField = new JPasswordField(16);
    private final JPasswordField newPasswordField = new JPasswordField(16);
    private final JPasswordField confirmPasswordField = new JPasswordField(16);
    private boolean changed = false;

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password - " + user.getName(), true);
        this.user = user;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 10, 20));

        addRow(form, 0, new JLabel("Current Password:"), currentPasswordField);
        addRow(form, 1, new JLabel("New Password:"), newPasswordField);
        addRow(form, 2, new JLabel("Confirm Password:"), confirmPasswordField);

        JButton saveBtn = new JButton("Update Password");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> doChangePassword());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, int row, JComponent label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(label, c);
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
