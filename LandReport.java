public class LandReport extends EnvironmentalReport {
    private String landIssue;     // e.g. Illegal logging, Soil erosion, Habitat clearing

    public LandReport(String id, String submitterID, String desc, String severity,
                      String date, String status, Location loc, String landIssue) {
        super(id, submitterID, desc, severity, date, status, loc);
        this.landIssue = landIssue;
    }

    @Override public String getCategoryDetails() { return "Land issue: " + landIssue; }
    @Override public String getType()            { return "LAND"; }
    @Override protected String getExtraData()    { return landIssue; }
}