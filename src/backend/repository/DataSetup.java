package repository;

import model.Connection;
import model.Station;
import model.Vehicle;

public class DataSetup {

    public static void fillDatabase(DataStore db) {
        System.out.println("Lade Streckennetz und Fahrzeuge...");
        erstelleHaltestellen(db);
        erstelleVerbindungen(db);
        erstelleFahrzeuge(db);
        db.restoreActiveBookings();
        System.out.println("Bereit: " + db.getStations().size() + " Haltestellen, "
                + db.getVehicles().size() + " Fahrzeuge.");
    }

    private static void erstelleHaltestellen(DataStore db) {
        db.addStation(new Station("Zentrale (Depot)", true));
        addStations(db,
            "Alexanderplatz",    "Zoologischer Garten", "Friedrichstraße",
            "Kottbusser Tor",    "Rosenthaler Platz",   "Hermannstraße",
            "Museumsinsel",      "Warschauer Straße",   "Potsdamer Platz",
            "Brandenburger Tor", "Mehringdamm",         "Gleisdreieck",
            "Wittenbergplatz",   "Frankfurter Tor"
        );
    }

    private static void erstelleVerbindungen(DataStore db) {
        Object[][] kanten = {
            { "Zentrale (Depot)",   "Alexanderplatz",       10 },
            { "Alexanderplatz",     "Museumsinsel",          3 },
            { "Museumsinsel",       "Brandenburger Tor",     2 },
            { "Brandenburger Tor",  "Friedrichstraße",       2 },
            { "Rosenthaler Platz",  "Alexanderplatz",        4 },
            { "Alexanderplatz",     "Kottbusser Tor",        6 },
            { "Kottbusser Tor",     "Hermannstraße",         8 },
            { "Warschauer Straße",  "Kottbusser Tor",        5 },
            { "Kottbusser Tor",     "Mehringdamm",           4 },
            { "Mehringdamm",        "Gleisdreieck",          3 },
            { "Gleisdreieck",       "Wittenbergplatz",       5 },
            { "Wittenbergplatz",    "Zoologischer Garten",   2 },
            { "Friedrichstraße",    "Potsdamer Platz",       4 },
            { "Potsdamer Platz",    "Gleisdreieck",          2 },
            { "Alexanderplatz",     "Frankfurter Tor",       5 },
        };
        for (Object[] k : kanten) {
            connect(db, (String) k[0], (String) k[1], (int) k[2]);
        }
    }

    private static void erstelleFahrzeuge(DataStore db) {
        for (int i = 1; i <= 3; i++) {
            db.addVehicle(new Vehicle(i, 4));
        }
    }

    private static void addStations(DataStore db, String... namen) {
        for (String name : namen) {
            db.addStation(new Station(name, false));
        }
    }

    private static void connect(DataStore db, String von, String nach, int minuten) {
        db.addConnection(new Connection(station(db, von), station(db, nach), minuten));
    }

    private static Station station(DataStore db, String name) {
        return db.getStations().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Station nicht gefunden: " + name));
    }
}
