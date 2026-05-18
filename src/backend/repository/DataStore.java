package repository;

import model.Connection;
import model.Passenger;
import model.Station;
import model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zentraler In-Memory-Datenspeicher, der alle persistenten Objekte des Systems hält.
 * Da wir keine echte Datenbank nutzen, übernimmt diese Klasse die Rolle
 * eines einfachen Repositories. Die Getter liefern unveränderliche Listen zurück,
 * damit externe Klassen die Daten nicht direkt manipulieren können.
 */
public class DataStore {

    private final List<Passenger> registeredPassengers;
    private final List<Vehicle> vehicles;
    private final List<Station> stations;
    private final List<Connection> connections;

    public DataStore() {
        this.registeredPassengers = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    // --- Lesezugriff (unveränderliche Sichten) ---

    public List<Passenger> getRegisteredPassengers() {
        return Collections.unmodifiableList(registeredPassengers);
    }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<Station> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    // --- Schreibzugriff über dedizierte Methoden ---

    public void addPassenger(Passenger passenger) {
        registeredPassengers.add(passenger);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void addStation(Station station) {
        stations.add(station);
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }
}
