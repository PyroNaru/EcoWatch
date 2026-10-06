/** A follow-up action on a report. Composition: it belongs to exactly one report. */
public class ReportAction {
    private String actionID;
    private String reportID;
    private String actionTaken;
    private String dateTaken;
    private String remarks;

    public ReportAction(String actionID, String reportID, String actionTaken, String dateTaken, String remarks) {
        this.actionID = actionID;
        this.reportID = reportID;
        this.actionTaken = actionTaken;
        this.dateTaken = dateTaken;
        this.remarks = remarks;
    }

    public String getActionID() { return actionID; }
    public String getReportID() { return reportID; }

    public String getDetails() {
        return "[" + dateTaken + "] " + actionTaken + " (" + remarks + ")";
    }

    /** actions.txt: actionID|reportID|actionTaken|dateTaken|remarks */
    public String toRecord() {
        return String.join(FileManager.DELIM, actionID, reportID, actionTaken, dateTaken, remarks);
    }
}