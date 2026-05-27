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
        Route route = getRouteOf(passenger);
        if (route == null) return -1;

        List<RouteStop> stops = route.getStops();
        for (int i = route.getCurrentStopIndex(); i < stops.size(); i++) {
            if (stops.get(i).getPassengersToPickUp().contains(passenger)) {
                return minutesUntil(stops.get(i).getPlannedArrivalTime());
            }
        }
        return -1;
    }

    @Override
    public int getRemainingTime(Passenger passenger) {
        Route route = getRouteOf(passenger);
        if (route == null) return -1;

        List<RouteStop> stops = route.getStops();
        for (int i = route.getCurrentStopIndex(); i < stops.size(); i++) {
            if (stops.get(i).getPassengersToDropOff().contains(passenger)) {
                return minutesUntil(stops.get(i).getPlannedArrivalTime());
            }
        }
        return -1;
    }

    private Route getRouteOf(Passenger passenger) {
        if (passenger.getAssignedVehicle() == null) return null;
        return passenger.getAssignedVehicle().getCurrentRoute();
    }

    private int minutesUntil(LocalDateTime targetTime) {
        if (targetTime == null) return -1;
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), targetTime);
        return (int) Math.max(0, minutes);
    }
}
