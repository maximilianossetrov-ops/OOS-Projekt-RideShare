package repository;

import model.Station;
import model.Connection;

public class DataSetup {
    public static void fillDatabase(DataStore db) {
        System.out.println("Lade Karte und Stationen in die Datenbank...");

        // --- 1. STATIONEN ERSTELLEN ---
        Station depot = new Station("Zentrale (Depot)", true);

        Station alex = new Station("Alexanderplatz", false);
        Station zoo = new Station("Zoologischer Garten", false);
        Station friedrich = new Station("Friedrichstraße", false);
        Station kotti = new Station("Kottbusser Tor", false);

        Station rosi = new Station("Rosenthaler Platz", false);
        Station hermann = new Station("Hermannstraße", false);
        Station museum = new Station("Museumsinsel", false);
        Station warschauer = new Station("Warschauer Straße", false);
        Station potsdamer = new Station("Potsdamer Platz", false);
        Station brandenburger = new Station("Brandenburger Tor", false);
        Station mehring = new Station("Mehringdamm", false);
        Station gleisdreieck = new Station("Gleisdreieck", false);
        Station wittenberg = new Station("Wittenbergplatz", false);
        Station frankfurter = new Station("Frankfurter Tor", false);

        // Haltestellen in die Datenbank schreiben
        db.addStation(depot);
        db.addStation(alex);
        db.addStation(zoo);
        db.addStation(friedrich);
        db.addStation(kotti);
        db.addStation(rosi);
        db.addStation(hermann);
        db.addStation(museum);
        db.addStation(warschauer);
        db.addStation(potsdamer);
        db.addStation(brandenburger);
        db.addStation(mehring);
        db.addStation(gleisdreieck);
        db.addStation(wittenberg);
        db.addStation(frankfurter);

        // 2. VERBINDUNGEN (Kanten im Graphen)
        db.addConnection(new Connection(depot, alex, 10));

        db.addConnection(new Connection(alex, museum, 3));
        db.addConnection(new Connection(museum, brandenburger, 2));
        db.addConnection(new Connection(brandenburger, friedrich, 2));

        db.addConnection(new Connection(rosi, alex, 4));
        db.addConnection(new Connection(alex, kotti, 6));
        db.addConnection(new Connection(kotti, hermann, 8));

        db.addConnection(new Connection(warschauer, kotti, 5));
        db.addConnection(new Connection(kotti, mehring, 4));
        db.addConnection(new Connection(mehring, gleisdreieck, 3));
        db.addConnection(new Connection(gleisdreieck, wittenberg, 5));
        db.addConnection(new Connection(wittenberg, zoo, 2));

        db.addConnection(new Connection(friedrich, potsdamer, 4));
        db.addConnection(new Connection(potsdamer, gleisdreieck, 2));
        db.addConnection(new Connection(alex, frankfurter, 5));

        System.out.println("Karte erfolgreich geladen! " + db.getStations().size() + " Stationen aktiv.");
    }
}