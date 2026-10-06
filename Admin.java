import java.time.LocalDate;

/** Authorized personnel who review reports, change status, delete, and log follow-up actions. */
public class Admin extends User {

    public Admin(String userID, String name, String username, String password) {
        super(userID, name, username, password);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    public void reviewReport(String reportID) {
        EnvironmentalReport r = FileManager.findReport(reportID);
        if (r == null) { System.out.println("Report not found."); return; }
        r.displaySummary();
        System.out.println("  Details: " + r.getCategoryDetails());   // polymorphic call
        System.out.println("  Location: " + r.getLocation().getFullAddress());
        if (r.getActions().isEmpty()) System.out.println("  No follow-up actions yet.");
        for (ReportAction a : r.getActions()) System.out.println("  - " + a.getDetails());
    }

    public void updateStatus(String reportID, String newStatus) {
        EnvironmentalReport r = FileManager.findReport(reportID);
        if (r == null) { System.out.println("Report not found."); return; }
        r.updateStatus(newStatus);
        System.out.println("Status updated.");
    }

    public void deleteReport(String reportID) {
        EnvironmentalReport r = FileManager.findReport(reportID);
        if (r == null) { System.out.println("Report not found."); return; }
        FileManager.deleteRecord(FileManager.REPORTS, reportID);
        FileManager.deleteRecord(FileManager.LOCATIONS, r.getLocation().getLocationID());
        FileManager.deleteRecordsWhere(FileManager.ACTIONS, 1, reportID); // composition: actions die with report
        System.out.println("Report " + reportID + " deleted.");
    }

    public void addAction(String reportID, String actionTaken, String remarks) {
        if (FileManager.findReport(reportID) == null) { System.out.println("Report not found."); return; }
        ReportAction a = new ReportAction(FileManager.nextId(FileManager.ACTIONS, "A"),
                reportID, actionTaken, LocalDate.now().toString(), remarks);
        FileManager.saveRecord(FileManager.ACTIONS, a.toRecord());
        System.out.println("Action recorded.");
    }
}