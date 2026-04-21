package ui;

import repository.DataSetup;
import repository.DataStore;

public class Main {
    public static DataStore database = new DataStore();

    public static void main(String[] args) {
        DataSetup.fillDatabase(database);
        EasyRideApp.main(args);
    }
}