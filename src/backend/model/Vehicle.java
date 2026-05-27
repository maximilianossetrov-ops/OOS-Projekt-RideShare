package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vehicle {

    private final int id;
    private final int maxCapacity;
    private final List<Passenger> passengers;
    private Route currentRoute;

    public Vehicle(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.passengers = new ArrayList<>();
    }

    public int getId() { return id; }
    public int getMaxCapacity() { return maxCapacity; }

    public List<Passenger> getPassengers() {
        return Collections.unmodifiableList(passengers);
    }

    public Route getCurrentRoute() { return currentRoute; }
    public void setCurrentRoute(Route route) { this.currentRoute = route; }

    public boolean hasCapacity() {
        return passengers.size() < maxCapacity;
    }

    // Setzt den Zustand des Fahrgastes auf IN_TRANSIT.
    public boolean addPassenger(Passenger passenger) {
        if (!hasCapacity() || passengers.contains(passenger)) return false;
        passengers.add(passenger);
        passenger.setState(PassengerState.IN_TRANSIT);
        return true;
    }

    // Setzt den Zustand des Fahrgastes auf ARRIVED.
    public boolean removePassenger(Passenger passenger) {
        if (!passengers.contains(passenger)) return false;
        passengers.remove(passenger);
        passenger.setState(PassengerState.ARRIVED);
        return true;
    }
}
