package service;

import model.Passenger;
import model.Route;
import model.RouteStop;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementierung des ITimeService.
 * Berechnet anhand der geplanten Ankunftszeiten in der Route,
 * wie lange ein Fahrgast noch warten muss bzw. wie lange die Fahrt noch dauert.
 */
public class TimeService implements ITimeService {

    /**
     * Sucht den Einstiegshalt des Fahrgastes und berechnet die verbleibenden Minuten.
     * Gibt -1 zurück, wenn das Fahrzeug den Halt bereits passiert hat.
     */
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

    /**
     * Sucht den Ausstiegshalt des Fahrgastes und berechnet die verbleibenden Minuten.
     * Gibt -1 zurück, wenn der Halt bereits passiert wurde.
     */
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

    /** Holt die aktuelle Route des Fahrzeugs, dem der Fahrgast zugewiesen ist. */
    private Route getRouteOf(Passenger passenger) {
        if (passenger.getAssignedVehicle() == null) return null;
        return passenger.getAssignedVehicle().getCurrentRoute();
    }

    /** Berechnet die Differenz in Minuten zwischen jetzt und dem Zielzeitpunkt. */
    private int minutesUntil(LocalDateTime targetTime) {
        if (targetTime == null) return -1;
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), targetTime);
        return (int) Math.max(0, minutes);
    }
}
