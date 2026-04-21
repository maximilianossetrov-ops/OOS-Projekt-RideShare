package model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RouteStop extends Station{
    private LocalDateTime plannedArrivalTime;
    private List<Passenger> passengersToPickUp = new ArrayList<>();
    private List<Passenger> passengersToDropOff = new ArrayList<>();

    public RouteStop(String name, Boolean isDepot) {
        super(name, isDepot);
    }

    public LocalDateTime getPlannedArrivalTime() {return plannedArrivalTime;}
    public void setPlannedArrivalTime(LocalDateTime plannedArrivalTime) {this.plannedArrivalTime = plannedArrivalTime;}

    public List<Passenger> getPassengersToPickUp() { return passengersToPickUp; }
    public List<Passenger> getPassengersToDropOff() { return passengersToDropOff; }
}