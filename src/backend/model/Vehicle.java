package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repräsentiert ein Fahrzeug der EasyRide-Flotte.
 * Ein Fahrzeug hat eine maximale Kapazität und eine aktuelle Route.
 * Das Ein- und Aussteigen der Fahrgäste aktualisiert automatisch deren Zustand.
 */
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

    /** Gibt eine unveränderliche Sicht auf die aktuell mitfahrenden Fahrgäste zurück. */
    public List<Passenger> getPassengers() {
        return Collections.unmodifiableList(passengers);
    }

    public Route getCurrentRoute() { return currentRoute; }
    public void setCurrentRoute(Route route) { this.currentRoute = route; }

    /** Gibt zurück, ob noch Platz für weitere Fahrgäste vorhanden ist. */
    public boolean hasCapacity() {
        return passengers.size() < maxCapacity;
    }

    /**
     * Nimmt einen Fahrgast auf, sofern Platz vorhanden ist und er nicht bereits im Fahrzeug sitzt.
     * Setzt den Zustand des Fahrgastes auf IN_TRANSIT.
     *
     * @return true bei Erfolg, false wenn das Fahrzeug voll ist oder der Fahrgast schon drin ist
     */
    public boolean addPassenger(Passenger passenger) {
        if (!hasCapacity() || passengers.contains(passenger)) {
            return false;
        }
        passengers.add(passenger);
        passenger.setState(PassengerState.IN_TRANSIT);
        return true;
    }

    /**
     * Setzt einen Fahrgast an der aktuellen Haltestelle ab.
     * Setzt den Zustand des Fahrgastes auf ARRIVED.
     *
     * @return true bei Erfolg, false wenn der Fahrgast gar nicht im Fahrzeug sitzt
     */
    public boolean removePassenger(Passenger passenger) {
        if (!passengers.contains(passenger)) {
            return false;
        }
        passengers.remove(passenger);
        passenger.setState(PassengerState.ARRIVED);
        return true;
    }
}
