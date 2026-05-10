package service;

import model.*;
import repository.DataStore;

public class BookingService implements IBookingService {
    private final RoutingService routingService;
    private final FleetService fleetService;

    public BookingService(DataStore dataStore) {
        this.routingService = new RoutingService(dataStore);
        this.fleetService = new FleetService(dataStore);
    }

    @Override
    public boolean bookRide(Station start, Station target, Passenger p) {
        Vehicle v = fleetService.getVehicleForPassenger(p);
        if (v == null) {
            System.out.println("Fehler: Kein freies Fahrzeug verfügbar!");
            return false;
        }

        Route newRoute;
        if (v.getCurrentRoute() == null) {
            newRoute = routingService.calcInitialRoute(v, start, target, p);
        } else {
            newRoute = routingService.calcNewRoute(v.getCurrentRoute(), p, start, target);
        }

        if (newRoute == null) {
            System.out.println("Fehler: Route konnte nicht berechnet werden.");
            return false;
        }

        v.setCurrentRoute(newRoute);
        v.addPassenger(p); // adds to vehicle list; sets state IN_TRANSIT

        // Link the passenger back to their vehicle and journey endpoints
        // so that TimeService can answer waiting-time / remaining-time queries.
        p.setAssignedVehicle(v);
        p.setPickupStation(start);
        p.setDropoffStation(target);

        System.out.println("Erfolg! " + p.getName() + " (" + p.getEmail() + ")"
                + " → Fahrzeug " + v.getId()
                + " | Start: " + start.getName()
                + " | Ziel: " + target.getName());
        return true;
    }

    public void confirmArrival(Vehicle v, RouteStop stop) {
        fleetService.confirmArrival(v, stop);
    }
}
