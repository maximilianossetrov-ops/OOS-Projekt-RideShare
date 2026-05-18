package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repräsentiert einen einzelnen Halt auf einer Fahrzeugroute.
 * An jedem Halt wird festgehalten, welche Fahrgäste ein- und aussteigen,
 * sowie wann das Fahrzeug planmäßig ankommen soll.
 */
public class RouteStop {

    private final Station station;
    private LocalDateTime plannedArrivalTime;

    // Fahrgastlisten werden beim Routenaufbau vom RoutingService befüllt
    private final List<Passenger> passengersToPickUp = new ArrayList<>();
    private final List<Passenger> passengersToDropOff = new ArrayList<>();

    private boolean reached;

    public RouteStop(Station station) {
        this.station = station;
        this.reached = false;
    }

    public Station getStation() { return station; }

    public LocalDateTime getPlannedArrivalTime() { return plannedArrivalTime; }
    public void setPlannedArrivalTime(LocalDateTime time) { this.plannedArrivalTime = time; }

    public boolean isReached() { return reached; }
    public void setReached(boolean reached) { this.reached = reached; }

    /** Fügt einen Fahrgast zur Einsteigeliste dieses Halts hinzu. */
    public void addPassengerToPickUp(Passenger passenger) {
        passengersToPickUp.add(passenger);
    }

    /** Fügt einen Fahrgast zur Aussteigeliste dieses Halts hinzu. */
    public void addPassengerToDropOff(Passenger passenger) {
        passengersToDropOff.add(passenger);
    }

    /** Gibt eine unveränderliche Sicht auf die Einsteigeliste zurück. */
    public List<Passenger> getPassengersToPickUp() {
        return Collections.unmodifiableList(passengersToPickUp);
    }

    /** Gibt eine unveränderliche Sicht auf die Aussteigeliste zurück. */
    public List<Passenger> getPassengersToDropOff() {
        return Collections.unmodifiableList(passengersToDropOff);
    }
}
