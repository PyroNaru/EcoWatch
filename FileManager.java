import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Handles ALL reading and writing of the .txt "database" files (Java File I/O). */
public class FileManager {
    public static final String DELIM = "|";
    public static final String USERS = "users.txt";
    public static final String REPORTS = "reports.txt";
    public static final String LOCATIONS = "locations.txt";
    public static final String ACTIONS = "actions.txt";
    private static final String DIR = "data";

    /** Cleans user input so a '|' can never break the .txt format. */
    public static String clean(String s) { return s.trim().replace("|", "/"); }

    private static Path path(String file) { return Paths.get(DIR, file); }

    // ---------- generic file operations (the CRUD core) ----------

    /** CREATE: append one line to a file. */
    public static void saveRecord(String file, String line) {
        try {
            Files.createDirectories(Paths.get(DIR));
            Files.write(path(file), Collections.singletonList(line),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    /** READ: every line, split into columns. */
    public static List<String[]> loadRecords(String file) {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(path(file))) return rows;
        try {
            for (String line : Files.readAllLines(path(file))) {
                if (!line.isBlank()) rows.add(line.split("\\|", -1));
            }
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
        return rows;
    }

    private static void rewrite(String file, List<String> lines) {
        try {
            Files.write(path(file), lines);
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    /** UPDATE: replace the line whose first column equals id. */
    public static void updateRecord(String file, String id, String newLine) {
        List<String> out = new ArrayList<>();
        for (String[] row : loadRecords(file)) {
            out.add(row[0].equals(id) ? newLine : String.join(DELIM, row));
        }
        rewrite(file, out);
    }

    /** DELETE: remove the line whose first column equals id. */
    public static void deleteRecord(String file, String id) {
        deleteRecordsWhere(file, 0, id);
    }

    /** DELETE: remove every line whose given column equals value. */
    public static void deleteRecordsWhere(String file, int column, String value) {
        List<String> out = new ArrayList<>();
        for (String[] row : loadRecords(file)) {
            if (!row[column].equals(value)) out.add(String.join(DELIM, row));
        }
        rewrite(file, out);
    }

    /** Next ID like R001, U002 (highest existing number + 1, so deletes never cause duplicates). */
    public static String nextId(String file, String prefix) {
        int max = 0;
        for (String[] row : loadRecords(file)) {
            try { max = Math.max(max, Integer.parseInt(row[0].substring(prefix.length()))); }
            catch (NumberFormatException ignored) { }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    // ---------- users ----------

    public static void saveUser(User u) { saveRecord(USERS, u.toRecord()); }

    public static void updateUser(User u) { updateRecord(USERS, u.getUserID(), u.toRecord()); }

    /** Rebuilds a User object (Observer or Admin) from users.txt. Returns null if not found. */
    public static User findUser(String username) {
        for (String[] r : loadRecords(USERS)) {
            if (r[3].equals(username)) return userFromRecord(r);
        }
        return null;
    }

    private static User userFromRecord(String[] r) {   // userID|role|name|username|password
        return r[1].equals("ADMIN") ? new Admin(r[0], r[2], r[3], r[4])
                                    : new Observer(r[0], r[2], r[3], r[4]);
    }

    // ---------- reports ----------

    public static EnvironmentalReport findReport(String reportID) {
        for (EnvironmentalReport r : loadReports()) {
            if (r.getReportID().equalsIgnoreCase(reportID)) return r;
        }
        return null;
    }

    /** Case-insensitive search across id, type, severity, status, description. Empty keyword = all. */
    public static List<EnvironmentalReport> searchReports(String keyword) {
        List<EnvironmentalReport> found = new ArrayList<>();
        String k = keyword.toLowerCase();
        for (EnvironmentalReport r : loadReports()) {
            String haystack = (r.getReportID() + " " + r.getType() + " " + r.getSeverity() + " "
                    + r.getStatus() + " " + r.getDescription()).toLowerCase();
            if (haystack.contains(k)) found.add(r);
        }
        return found;
    }

    public static List<EnvironmentalReport> loadReports() {
        List<EnvironmentalReport> list = new ArrayList<>();
        for (String[] r : loadRecords(REPORTS)) {
            EnvironmentalReport rep = reportFromRecord(r);
            if (rep != null) list.add(rep);
        }
        return list;
    }

    // reportID|type|submitterID|description|severity|date|status|locationID|extra...
    private static EnvironmentalReport reportFromRecord(String[] r) {
        Location loc = findLocation(r[7]);
        EnvironmentalReport rep;
        switch (r[1]) {   // the type column decides which subclass to build
            case "CLIMATE":  rep = new ClimateReport(r[0], r[2], r[3], r[4], r[5], r[6], loc, r[8]); break;
            case "MARINE":   rep = new MarineReport(r[0], r[2], r[3], r[4], r[5], r[6], loc, r[8]); break;
            case "LAND":     rep = new LandReport(r[0], r[2], r[3], r[4], r[5], r[6], loc, r[8]); break;
            case "WILDLIFE": rep = new WildlifeReport(r[0], r[2], r[3], r[4], r[5], r[6], loc, r[8], r[9]); break;
            default: return null;
        }
        for (String[] a : loadRecords(ACTIONS)) {   // actionID|reportID|actionTaken|date|remarks
            if (a[1].equals(r[0])) rep.getActions().add(new ReportAction(a[0], a[1], a[2], a[3], a[4]));
        }
        return rep;
    }

    private static Location findLocation(String locationID) {
        for (String[] r : loadRecords(LOCATIONS)) {
            if (r[0].equals(locationID)) return new Location(r[0], r[1], r[2]);
        }
        return new Location(locationID, "Unknown", "Unknown");
    }

    public static void updateAction(ReportAction a) {
        updateRecord(ACTIONS, a.getActionID(), a.toRecord());
    }

    /** Pre-populates realistic initial environmental reports if reports.txt is empty. */
    public static void seedSampleDataIfEmpty() {
        if (!loadRecords(REPORTS).isEmpty()) return;

        // Locations
        Location loc1 = new Location("L001", "Tuguegarao East", "Tuguegarao City");
        Location loc2 = new Location("L002", "Sabang", "Puerto Galera");
        Location loc3 = new Location("L003", "San Mateo", "Norzagaray");
        Location loc4 = new Location("L004", "Malagos", "Davao City");

        saveRecord(LOCATIONS, loc1.toRecord());
        saveRecord(LOCATIONS, loc2.toRecord());
        saveRecord(LOCATIONS, loc3.toRecord());
        saveRecord(LOCATIONS, loc4.toRecord());

        // Reports
        ClimateReport rep1 = new ClimateReport("R001", "U002",
                "Rising river floodwaters recorded following persistent heavy rainfall. Embankment overtopping threatening low-lying households.",
                "HIGH", "2026-10-14", "UNDER REVIEW", loc1, "Flash Flood");

        MarineReport rep2 = new MarineReport("R002", "U003",
                "Dense accumulation of non-biodegradable plastics and micro-waste washing ashore, choking nearshore coral reef zones.",
                "MEDIUM", "2026-10-10", "RESOLVED", loc2, "Coastal Plastic Pollution");

        LandReport rep3 = new LandReport("R003", "U002",
                "Illegal quarrying activities and timber cutting causing noticeable soil slope instability near local watershed boundary.",
                "HIGH", "2026-10-16", "PENDING", loc3, "Illegal Logging & Soil Erosion");

        WildlifeReport rep4 = new WildlifeReport("R004", "U003",
                "Juvenile Philippine eagle found grounded and entangled in discarded fishing nylon near forest border.",
                "LOW", "2026-10-08", "RESOLVED", loc4, "Philippine Eagle (Pithecophaga jefferyi)", "Entangled Raptor Rescue");

        saveRecord(REPORTS, rep1.toRecord());
        saveRecord(REPORTS, rep2.toRecord());
        saveRecord(REPORTS, rep3.toRecord());
        saveRecord(REPORTS, rep4.toRecord());

        // Actions
        ReportAction act1 = new ReportAction("A001", "R001",
                "Emergency rescue boats deployed; sandbag reinforcements initiated.", "2026-10-15",
                "Coordinated with local CDRRMO emergency crew.");
        ReportAction act2 = new ReportAction("A002", "R002",
                "Organized coastal cleanup drive with 45 community volunteers.", "2026-10-12",
                "Collected 380 kg of waste for eco-brick recycling.");
        ReportAction act3 = new ReportAction("A003", "R004",
                "Rescued by DENR-certified handlers; treated for minor wing abrasions and safely released.", "2026-10-09",
                "Health clearance confirmed at raptor center.");

        saveRecord(ACTIONS, act1.toRecord());
        saveRecord(ACTIONS, act2.toRecord());
        saveRecord(ACTIONS, act3.toRecord());
    }
}