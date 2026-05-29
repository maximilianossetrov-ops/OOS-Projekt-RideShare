package repository;

import model.Booking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    private static final Path FILE = Path.of("data/bookings.json");

    public static List<Booking> load() {
        List<Booking> bookings = new ArrayList<>();
        if (!Files.exists(FILE)) return bookings;
        try {
            String content = Files.readString(FILE);
            int pos = 0;
            while ((pos = content.indexOf('{', pos)) != -1) {
                int end = content.indexOf('}', pos);
                if (end == -1) break;
                String entry = content.substring(pos + 1, end);
                int    bookingId   = parseIntField(entry, "bookingId");
                int    passengerId = parseIntField(entry, "passengerId");
                int    vehicleId   = parseIntField(entry, "vehicleId");
                String pickup      = parseStringField(entry, "pickupStation");
                String dropoff     = parseStringField(entry, "dropoffStation");
                String state       = parseStringField(entry, "state");
                String bookedAt    = parseStringField(entry, "bookedAt");
                if (bookingId > 0 && passengerId > 0 && state != null) {
                    bookings.add(new Booking(bookingId, passengerId, vehicleId,
                            pickup, dropoff, state, bookedAt));
                }
                pos = end + 1;
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Buchungen: " + e.getMessage());
        }
        return bookings;
    }

    public static void save(List<Booking> bookings) {
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < bookings.size(); i++) {
                Booking b = bookings.get(i);
                sb.append("  {")
                  .append("\"bookingId\": ").append(b.getBookingId()).append(", ")
                  .append("\"passengerId\": ").append(b.getPassengerId()).append(", ")
                  .append("\"vehicleId\": ").append(b.getVehicleId()).append(", ")
                  .append("\"pickupStation\": \"").append(b.getPickupStationName()).append("\", ")
                  .append("\"dropoffStation\": \"").append(b.getDropoffStationName()).append("\", ")
                  .append("\"state\": \"").append(b.getState()).append("\", ")
                  .append("\"bookedAt\": \"").append(b.getBookedAt()).append("\"")
                  .append("}");
                if (i < bookings.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            Files.writeString(FILE, sb.toString());
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Buchungen: " + e.getMessage());
        }
    }

    private static int parseIntField(String json, String key) {
        String search = "\"" + key + "\": ";
        int idx = json.indexOf(search);
        if (idx == -1) return -1;
        int start = idx + search.length();
        int end   = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return (end > start) ? Integer.parseInt(json.substring(start, end)) : -1;
    }

    private static String parseStringField(String json, String key) {
        String search = "\"" + key + "\": \"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int start = idx + search.length();
        int end   = json.indexOf('"', start);
        return (end != -1) ? json.substring(start, end) : null;
    }
}
