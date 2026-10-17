import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the GUI version. */
public class Main {
    public static void main(String[] args) {
        // Enable subpixel LCD text antialiasing for crisp modern fonts
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Create the default admin the first time the program runs
        if (FileManager.findUser("admin") == null) {
            FileManager.saveUser(new Admin(FileManager.nextId(FileManager.USERS, "U"),
                    "Administrator", "admin", "admin123"));
        }

        // Pre-populate realistic seed reports if empty
        FileManager.seedSampleDataIfEmpty();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}