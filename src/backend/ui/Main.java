package ui;

import repository.DataSetup;
import repository.DataStore;

public class Main {

    private static DataStore database;

    public static DataStore getDatabase() {
        return database;
    }

    public static void main(String[] args) {
        database = new DataStore();
        DataSetup.fillDatabase(database);
        EasyRideApp.main(args);
    }
}
