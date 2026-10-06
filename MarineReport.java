public class MarineReport extends EnvironmentalReport {
    private String marineIssue;   // e.g. Oil spill, Coastal garbage, Coral damage

    public MarineReport(String id, String submitterID, String desc, String severity,
                        String date, String status, Location loc, String marineIssue) {
        super(id, submitterID, desc, severity, date, status, loc);
        this.marineIssue = marineIssue;
    }

    @Override public String getCategoryDetails() { return "Marine issue: " + marineIssue; }
    @Override public String getType()            { return "MARINE"; }
    @Override protected String getExtraData()    { return marineIssue; }
}