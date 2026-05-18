package service;

import model.Passenger;
import model.Route;
import model.Station;
import model.Vehicle;

/**
 * Implementierung des IBookingService.
 * Koordiniert den Buchungsvorgang: Fahrzeug auswählen, Route berechnen
 * und dem Fahrgast alle nötigen Informationen zuweisen.
 *
 * Die Abhängigkeiten zu IFleetService und IRouteService werden von außen
 * übergeben (Dependency Injection), damit diese Klasse nicht selbst entscheidet,
 * welche Implementierung verwendet wird.
 */
public class BookingService implements IBookingService {

    private final IRouteService routeService;
    private final IFleetService fleetService;

    public BookingService(IRouteService routeService, IFleetService fleetService) {
        this.routeService = routeService;
        this.fleetService = fleetService;
    }

    /**
     * Bucht eine Fahrt für den Fahrgast:
     * 1. Freies Fahrzeug suchen
     * 2. Route berechnen (neu oder erweitert)
     * 3. Fahrgast dem Fahrzeug zuweisen und Buchungsdetails setzen
     */
    @Override
    public boolean bookRide(Station start, Station target, Passenger passenger) {
        Vehicle vehicle = fleetService.getVehicleForPassenger(passenger);
        if (vehicle == null) {
            System.out.println("Buchung fehlgeschlagen: Kein freies Fahrzeug verfügbar.");
            return false;
        }

        // Neue Route berechnen – je nachdem ob das Fahrzeug schon unterwegs ist
        Route route;
        if (vehicle.getCurrentRoute() == null) {
            route = routeService.calcInitialRoute(vehicle, start, target, passenger);
        } else {
            route = routeService.calcNewRoute(vehicle.getCurrentRoute(), passenger, start, target);
        }

        if (route == null) {
            System.out.println("Buchung fehlgeschlagen: Keine Route berechenbar.");
            return false;
        }

        // Fahrzeug und Fahrgast verknüpfen
        vehicle.setCurrentRoute(route);
        vehicle.addPassenger(passenger);
        passenger.setAssignedVehicle(vehicle);
        passenger.setPickupStation(start);
        passenger.setDropoffStation(target);

        System.out.println("Buchung erfolgreich: " + passenger.getName()
                + " → Fahrzeug #" + vehicle.getId()
                + " | " + start.getName() + " → " + target.getName());
        return true;
    }
}
