package model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RouteStop {
    Station station;
    private LocalDateTime plannedArrivalTime;
    private List<Passenger> passengersToPickUp = new ArrayList<>();
    private List<Passenger> passengersToDropOff = new ArrayList<>();
    private Boolean reached;

    public RouteStop(Station station) {
        this.station = station;
        this.reached = false;
    }

    public LocalDateTime getPlannedArrivalTime() {return plannedArrivalTime;}
    public void setPlannedArrivalTime(LocalDateTime plannedArrivalTime) {this.plannedArrivalTime = plannedArrivalTime;}

    public List<Passenger> getPassengersToPickUp() { return passengersToPickUp; }
    public List<Passenger> getPassengersToDropOff() { return passengersToDropOff; }

    public void setReached(boolean reached) {
        this.reached = reached;
    }

    public boolean isReached() {
        return reached;
    }

    public Station getStation() {
        return station;
    }

}