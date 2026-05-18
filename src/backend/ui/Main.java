package ui;

import repository.DataSetup;
import repository.DataStore;

/**
 * Einstiegspunkt der EasyRide-Anwendung.
 * Initialisiert den DataStore, befüllt ihn mit Testdaten
 * und startet anschließend die JavaFX-Oberfläche.
 */
public class Main {

    // Zentraler Datenspeicher, der von allen Services genutzt wird
    private static DataStore database;

    /** Gibt den gemeinsamen Datenbankstore zurück. */
    public static DataStore getDatabase() {
        return database;
    }

    public static void main(String[] args) {
        database = new DataStore();
        DataSetup.fillDatabase(database);
        EasyRideApp.main(args);
    }
}
