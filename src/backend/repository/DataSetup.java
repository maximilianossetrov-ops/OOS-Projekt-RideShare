package repository;

import model.Connection;
import model.Station;
import model.Vehicle;

/**
 * Initialisiert den DataStore mit Testdaten.
 * Diese Klasse übernimmt die Rolle eines "Database Seeder":
 * Sie legt alle Haltestellen, Verbindungen und Fahrzeuge an,
 * die das System zum Betrieb benötigt.
 */
public class DataSetup {

    /**
     * Befüllt den übergebenen DataStore mit dem Berliner Streckennetz
     * und einer kleinen Fahrzeugflotte.
     */
    public static void fillDatabase(DataStore db) {
        System.out.println("Lade Streckennetz und Fahrzeuge...");

        erstelleHaltestellen(db);
        erstelleVerbindungen(db);
        erstelleFahrzeuge(db);

        System.out.println("Bereit: " + db.getStations().size() + " Haltestellen, "
                + db.getVehicles().size() + " Fahrzeuge.");
    }

    // --- Haltestellen anlegen ---

    private static void erstelleHaltestellen(DataStore db) {
        // Das Depot ist der Ausgangspunkt aller Fahrzeuge
        db.addStation(new Station("Zentrale (Depot)", true));

        // Reguläre Haltestellen im Berliner Netz
        db.addStation(new Station("Alexanderplatz",      false));
        db.addStation(new Station("Zoologischer Garten", false));
        db.addStation(new Station("Friedrichstraße",     false));
        db.addStation(new Station("Kottbusser Tor",      false));
        db.addStation(new Station("Rosenthaler Platz",   false));
        db.addStation(new Station("Hermannstraße",       false));
        db.addStation(new Station("Museumsinsel",        false));
        db.addStation(new Station("Warschauer Straße",   false));
        db.addStation(new Station("Potsdamer Platz",     false));
        db.addStation(new Station("Brandenburger Tor",   false));
        db.addStation(new Station("Mehringdamm",         false));
        db.addStation(new Station("Gleisdreieck",        false));
        db.addStation(new Station("Wittenbergplatz",     false));
        db.addStation(new Station("Frankfurter Tor",     false));
    }

    // --- Verbindungen (Kanten im Graphen) anlegen ---

    private static void erstelleVerbindungen(DataStore db) {
        // Stationen aus der Liste holen, um sie als Endpunkte zu verwenden
        Station depot        = station(db, "Zentrale (Depot)");
        Station alex         = station(db, "Alexanderplatz");
        Station zoo          = station(db, "Zoologischer Garten");
        Station friedrich    = station(db, "Friedrichstraße");
        Station kotti        = station(db, "Kottbusser Tor");
        Station rosi         = station(db, "Rosenthaler Platz");
        Station hermann      = station(db, "Hermannstraße");
        Station museum       = station(db, "Museumsinsel");
        Station warschauer   = station(db, "Warschauer Straße");
        Station potsdamer    = station(db, "Potsdamer Platz");
        Station brandenburger = station(db, "Brandenburger Tor");
        Station mehring      = station(db, "Mehringdamm");
        Station gleisdreieck = station(db, "Gleisdreieck");
        Station wittenberg   = station(db, "Wittenbergplatz");
        Station frankfurter  = station(db, "Frankfurter Tor");

        // Depot-Anbindung
        db.addConnection(new Connection(depot,        alex,          10));

        // Zentrale Ost-West-Achse
        db.addConnection(new Connection(alex,         museum,         3));
        db.addConnection(new Connection(museum,       brandenburger,  2));
        db.addConnection(new Connection(brandenburger, friedrich,     2));

        // Nordost-Zweig
        db.addConnection(new Connection(rosi,         alex,           4));
        db.addConnection(new Connection(alex,         kotti,          6));
        db.addConnection(new Connection(kotti,        hermann,        8));

        // Südost-Zweig
        db.addConnection(new Connection(warschauer,   kotti,          5));
        db.addConnection(new Connection(kotti,        mehring,        4));
        db.addConnection(new Connection(mehring,      gleisdreieck,   3));
        db.addConnection(new Connection(gleisdreieck, wittenberg,     5));
        db.addConnection(new Connection(wittenberg,   zoo,            2));

        // Südliche Verbindungen
        db.addConnection(new Connection(friedrich,    potsdamer,      4));
        db.addConnection(new Connection(potsdamer,    gleisdreieck,   2));

        // Östliche Verbindung
        db.addConnection(new Connection(alex,         frankfurter,    5));
    }

    // --- Fahrzeuge anlegen ---

    private static void erstelleFahrzeuge(DataStore db) {
        // Drei Fahrzeuge mit je 4 Sitzplätzen
        db.addVehicle(new Vehicle(1, 4));
        db.addVehicle(new Vehicle(2, 4));
        db.addVehicle(new Vehicle(3, 4));
    }

    /** Hilfsmethode: Sucht eine Station anhand ihres Namens im DataStore. */
    private static Station station(DataStore db, String name) {
        return db.getStations().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Station nicht gefunden: " + name));
    }
}
