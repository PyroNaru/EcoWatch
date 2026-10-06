import javax.swing.SwingUtilities;

/** Entry point of the GUI version. */
public class Main {
    public static void main(String[] args) {
        // create the default admin the first time the program runs
        if (FileManager.findUser("admin") == null) {
            FileManager.saveUser(new Admin(FileManager.nextId(FileManager.USERS, "U"),
                    "Administrator", "admin", "admin123"));
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}