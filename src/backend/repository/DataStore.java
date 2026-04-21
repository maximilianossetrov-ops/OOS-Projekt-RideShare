package repository;

import model.Connection;
import model.Passenger;
import model.Station;
import model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class DataStore {

    // --- UNSERE TABELLEN (Die Listen) ---
    private List<Passenger> registeredPassengers;
    private List<Vehicle> vehicles; // Oder 'Driver', je nachdem wie ihr das nennt
    private List<Station> stations;
    private List<Connection> connections;


    public DataStore() {
        this.registeredPassengers = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    //GETTER
    public List<Passenger> getRegisteredPassengers() {
        return registeredPassengers;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<Station> getStations() {
        return stations;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    //HILFSMETHODEN
    public void addPassenger(Passenger passenger) {
        this.registeredPassengers.add(passenger);
    }
    public void addStation(Station station) {
        // Hier könnte man später sogar prüfen, ob die Station schon existiert!
        this.stations.add(station);
    }
    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    public void printPassenger() {
        System.out.println("Registered Passengers: ");
        for(Passenger p: registeredPassengers) {
            System.out.println(p.getEmail());
        }
    }
}