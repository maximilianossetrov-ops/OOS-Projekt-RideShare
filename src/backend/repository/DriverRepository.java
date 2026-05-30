package repository;

import model.Driver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Liest und schreibt registrierte Fahrer in data/drivers.json.
// Das Fahrzeug wird erst zur Laufzeit (pro Schicht) gewählt – nicht gespeichert.

public class DriverRepository {

    private static final Path FILE = Path.of("data/drivers.json");

    public static List<Driver> load() {
        List<Driver> drivers = new ArrayList<>();
        if (!Files.exists(FILE)) return drivers;
        try {
            String content = Files.readString(FILE);
            int pos = 0;
            while ((pos = content.indexOf('{', pos)) != -1) {
                int end = content.indexOf('}', pos);
                if (end == -1) break;
                String entry = content.substring(pos + 1, end);
                int id = parseIntField(entry, "id");
                String name = parseStringField(entry, "name");
                String email = parseStringField(entry, "email");
                String password = parseStringField(entry, "password");
                if (id > 0 && email != null) {
                    drivers.add(new Driver(id,
                            name != null ? name : email,
                            email,
                            password != null ? password : ""));
                }
                pos = end + 1;
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Fahrer: " + e.getMessage());
        }
        return drivers;
    }

    public static void save(List<Driver> drivers) {
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < drivers.size(); i++) {
                Driver d = drivers.get(i);
                sb.append("  {\"id\": ").append(d.getId())
                  .append(", \"name\": \"").append(d.getName()).append("\"")
                  .append(", \"email\": \"").append(d.getEmail()).append("\"")
                  .append(", \"password\": \"").append(d.getPassword()).append("\"}");
                if (i < drivers.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(FILE, sb.toString());
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Fahrer: " + e.getMessage());
        }
    }

    private static int parseIntField(String json, String key) {
        String search = "\"" + key + "\": ";
        int idx = json.indexOf(search);
        if (idx == -1) return -1;
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return (end > start) ? Integer.parseInt(json.substring(start, end)) : -1;
    }

    private static String parseStringField(String json, String key) {
        String search = "\"" + key + "\": \"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int start = idx + search.length();
        int end = json.indexOf('"', start);
        return (end != -1) ? json.substring(start, end) : null;
    }
}
