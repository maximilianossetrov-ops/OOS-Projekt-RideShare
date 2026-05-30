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
                String name = parseStringField(entry, "name");
                String email = parseStringField(entry, "email");
                String password = parseStringField(entry, "password");
                if (id > 0 && email != null) {
                    if (name == null)
                        name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                    passengers.add(new Passenger(id, name, email, password != null ? password : ""));
                }
                pos = end + 1;
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Nutzer: " + e.getMessage());
        }
        return passengers;
    }

    public static void save(List<Passenger> passengers) {
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                sb.append("  {\"id\": ").append(p.getId())
                  .append(", \"name\": \"").append(p.getName()).append("\"")
                  .append(", \"email\": \"").append(p.getEmail()).append("\"")
                  .append(", \"password\": \"").append(p.getPassword()).append("\"}");
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
