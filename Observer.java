import java.util.List;

/** Community member who submits and views environmental reports. */
public class Observer extends User {

    public Observer(String userID, String name, String username, String password) {
        super(userID, name, username, password);
    }

    @Override
    public String getRole() { return "OBSERVER"; }

    public void createReport(EnvironmentalReport report) {
        report.submitReport();
    }

    public void viewReports() {
        printList(FileManager.searchReports(""));
    }

    public void searchReport(String keyword) {
        printList(FileManager.searchReports(keyword));
    }

    private void printList(List<EnvironmentalReport> list) {
        if (list.isEmpty()) { System.out.println("No reports found."); return; }
        for (EnvironmentalReport r : list) r.displaySummary();
    }
}