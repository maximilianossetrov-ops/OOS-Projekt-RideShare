package repository;

import model.Passenger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Liest und schreibt registrierte Nutzer in data/users.json.


public class PassengerRepository {

    private static final Path FILE = Path.of("data/users.json");

    /** Lädt alle gespeicherten Nutzer aus der JSON-Datei. */
    public static List<Passenger> load() {
        List<Passenger> passengers = new ArrayList<>();
        if (!Files.exists(FILE)) return passengers;

        try {
            String content = Files.readString(FILE);
            int pos = 0;
            while ((pos = content.indexOf('{', pos)) != -1) {
                int end = content.indexOf('}', pos);
                if (end == -1) break;
                String entry = content.substring(pos + 1, end);
                int id = parseIntField(entry, "id");
                String email = parseStringField(entry, "email");
                if (id > 0 && email != null) {
                    passengers.add(new Passenger(id, email));
                }
                pos = end + 1;
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Nutzer: " + e.getMessage());
        }
        return passengers;
    }

    /** Speichert die komplette Nutzerliste in die JSON-Datei. */
    public static void save(List<Passenger> passengers) {
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                sb.append("  {\"id\": ").append(p.getId())
                  .append(", \"email\": \"").append(p.getEmail()).append("\"}");
                if (i < passengers.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(FILE, sb.toString());
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Nutzer: " + e.getMessage());
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
