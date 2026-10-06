public class ClimateReport extends EnvironmentalReport {
    private String weatherType;   // e.g. Flood, Typhoon, Drought

    public ClimateReport(String id, String submitterID, String desc, String severity,
                         String date, String status, Location loc, String weatherType) {
        super(id, submitterID, desc, severity, date, status, loc);
        this.weatherType = weatherType;
    }

    @Override public String getCategoryDetails() { return "Weather type: " + weatherType; }
    @Override public String getType()            { return "CLIMATE"; }
    @Override protected String getExtraData()    { return weatherType; }
}