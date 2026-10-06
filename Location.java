/** Where a problem was reported. Aggregation: a Location can exist without any report. */
public class Location {
    private String locationID;
    private String barangay;
    private String municipality;

    public Location(String locationID, String barangay, String municipality) {
        this.locationID = locationID;
        this.barangay = barangay;
        this.municipality = municipality;
    }

    public String getLocationID()   { return locationID; }
    public String getFullAddress()  { return barangay + ", " + municipality; }
    public String getBarangay()     { return barangay; }
    public String getMunicipality() { return municipality; }

    /** locations.txt: locationID|barangay|municipality */
    public String toRecord() {
        return String.join(FileManager.DELIM, locationID, barangay, municipality);
    }
}