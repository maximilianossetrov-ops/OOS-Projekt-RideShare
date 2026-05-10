package service;

import model.Passenger;
import model.Route;
import model.RouteStop;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TimeService implements ITimeService {

    @Override
    public int getWaitingTime(Passenger passenger) {
        Route route = getRoute(passenger);
        if (route == null) return -1;

        List<RouteStop> stops = route.getStops();
        for (int i = route.getCurrentStopIndex(); i < stops.size(); i++) {
            if (stops.get(i).getPassengersToPickUp().contains(passenger)) {
                return minutesUntil(stops.get(i).getPlannedArrivalTime());
            }
        }
        return -1; // pickup stop already passed or not found
    }

    @Override
    public int getRemainingTime(Passenger passenger) {
        Route route = getRoute(passenger);
        if (route == null) return -1;

        List<RouteStop> stops = route.getStops();
        for (int i = route.getCurrentStopIndex(); i < stops.size(); i++) {
            if (stops.get(i).getPassengersToDropOff().contains(passenger)) {
                return minutesUntil(stops.get(i).getPlannedArrivalTime());
            }
        }
        return -1; // dropoff stop already passed or not found
    }

    private Route getRoute(Passenger passenger) {
        if (passenger.getAssignedVehicle() == null) return null;
        return passenger.getAssignedVehicle().getCurrentRoute();
    }

    private int minutesUntil(LocalDateTime target) {
        if (target == null) return -1;
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), target);
        return (int) Math.max(0, minutes);
    }
}
