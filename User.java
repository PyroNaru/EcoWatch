/** Abstract superclass for everyone who logs in. Cannot be instantiated directly. */
public abstract class User {
    private String userID;
    private String name;
    private String username;
    private String password;

    public User(String userID, String name, String username, String password) {
        this.userID = userID;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getUserID()   { return userID; }
    public String getName()     { return name; }
    public String getUsername() { return username; }

    public boolean login(String inputUsername, String inputPassword) {
        return username.equals(inputUsername) && password.equals(inputPassword);
    }

    public void logout() {
        System.out.println(name + " logged out.");
    }

    public boolean changePassword(String currentPassword, String newPassword) {
        if (currentPassword == null || !this.password.equals(currentPassword)) {
            return false;
        }
        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }
        this.password = newPassword;
        return true;
    }

    /** Overridden by each subclass (polymorphism). */
    public abstract String getRole();

    /** One line for users.txt: userID|role|name|username|password */
    public String toRecord() {
        return String.join(FileManager.DELIM, userID, getRole(), name, username, password);
    }
}