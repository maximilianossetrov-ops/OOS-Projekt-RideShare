package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteStop {

    private final Station station;
    private LocalDateTime plannedArrivalTime;

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

    public void addPassengerToPickUp(Passenger passenger) { passengersToPickUp.add(passenger); }
    public void addPassengerToDropOff(Passenger passenger) { passengersToDropOff.add(passenger); }

    public List<Passenger> getPassengersToPickUp() {
        return Collections.unmodifiableList(passengersToPickUp);
    }

    public List<Passenger> getPassengersToDropOff() {
        return Collections.unmodifiableList(passengersToDropOff);
    }
}
