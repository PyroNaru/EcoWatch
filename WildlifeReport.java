public class WildlifeReport extends EnvironmentalReport {
    private String species;         // e.g. Philippine eagle, Sea turtle
    private String wildlifeIssue;   // e.g. Injured, Poaching, Illegal trade

    public WildlifeReport(String id, String submitterID, String desc, String severity,
                          String date, String status, Location loc, String species, String wildlifeIssue) {
        super(id, submitterID, desc, severity, date, status, loc);
        this.species = species;
        this.wildlifeIssue = wildlifeIssue;
    }

    @Override public String getCategoryDetails() { return "Species: " + species + ", Issue: " + wildlifeIssue; }
    @Override public String getType()            { return "WILDLIFE"; }
    @Override protected String getExtraData()    { return species + FileManager.DELIM + wildlifeIssue; }
}