import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Abstract superclass of all report categories. Cannot be instantiated directly. */
public abstract class EnvironmentalReport {
    private String reportID;
    private String submitterID;
    private String description;
    private String severity;       // LOW, MEDIUM, HIGH
    private String dateReported;
    private String status;         // PENDING, UNDER REVIEW, RESOLVED
    private Location location;                                   // aggregation
    private List<ReportAction> actions = new ArrayList<>();      // composition

    public EnvironmentalReport(String reportID, String submitterID, String description,
                               String severity, String dateReported, String status, Location location) {
        this.reportID = reportID;
        this.submitterID = submitterID;
        this.description = description;
        this.severity = severity;
        this.dateReported = dateReported;
        this.status = status;
        this.location = location;
    }

    public String getReportID()     { return reportID; }
    public String getSubmitterID()  { return submitterID; }
    public String getDescription()  { return description; }
    public String getSeverity()     { return severity; }
    public String getDateReported() { return dateReported; }
    public String getStatus()       { return status; }
    public Location getLocation()  { return location; }
    public List<ReportAction> getActions() { return actions; }

    /** Each subclass overrides these (polymorphism / abstraction). */
    public abstract String getCategoryDetails();
    public abstract String getType();
    protected abstract String getExtraData();   // category-specific columns for the .txt file

    public boolean validateReport() {
        List<String> fields = new ArrayList<>(Arrays.asList(
                description, severity, location.getBarangay(), location.getMunicipality()));
        fields.addAll(Arrays.asList(getExtraData().split("\\|", -1)));
        for (String f : fields) {
            if (f == null || f.isBlank()) {
                System.out.println("Error: no field may be empty.");
                return false;
            }
        }
        if (!(severity.equals("LOW") || severity.equals("MEDIUM") || severity.equals("HIGH"))) {
            System.out.println("Error: severity must be LOW, MEDIUM, or HIGH.");
            return false;
        }
        return true;
    }

    public void submitReport() {
        if (!validateReport()) return;
        FileManager.saveRecord(FileManager.LOCATIONS, location.toRecord());
        FileManager.saveRecord(FileManager.REPORTS, toRecord());
        System.out.println("Report " + reportID + " submitted.");
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
        FileManager.updateRecord(FileManager.REPORTS, reportID, toRecord());
    }

    public void displaySummary() {
        System.out.println(reportID + " | " + getType() + " | " + severity + " | " + status
                + " | " + dateReported + " | " + description);
    }

    /** reports.txt: reportID|type|submitterID|description|severity|dateReported|status|locationID|extra... */
    public String toRecord() {
        return String.join(FileManager.DELIM, reportID, getType(), submitterID, description,
                severity, dateReported, status, location.getLocationID(), getExtraData());
    }
}