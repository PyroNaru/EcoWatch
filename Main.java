import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the GUI version. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // create the default admin the first time the program runs
        if (FileManager.findUser("admin") == null) {
            FileManager.saveUser(new Admin(FileManager.nextId(FileManager.USERS, "U"),
                    "Administrator", "admin", "admin123"));
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}