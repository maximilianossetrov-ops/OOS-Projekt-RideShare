package service;

import model.Passenger;
import model.PassengerState;
import model.Route;
import model.Station;
import model.Vehicle;

public class BookingService implements IBookingService {

    private final IRouteService routeService;
    private final IFleetService fleetService;

    public BookingService(IRouteService routeService, IFleetService fleetService) {
        this.routeService = routeService;
        this.fleetService = fleetService;
    }

    @Override
    public boolean bookRide(Station start, Station target, Passenger passenger) {
        Vehicle vehicle = fleetService.getVehicleForPassenger(passenger);
        if (vehicle == null) {
            System.out.println("Buchung fehlgeschlagen: Kein freies Fahrzeug verfügbar.");
            return false;
        }

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

        vehicle.setCurrentRoute(route);
        vehicle.addPassenger(passenger);
        // Passagier ist noch nicht eingestiegen – Zustand auf WAITING zurücksetzen.
        passenger.setState(PassengerState.WAITING);
        passenger.setAssignedVehicle(vehicle);
        passenger.setPickupStation(start);
        passenger.setDropoffStation(target);

        System.out.println("Buchung erfolgreich: " + passenger.getName()
                + " → Fahrzeug #" + vehicle.getId()
                + " | " + start.getName() + " → " + target.getName());
        return true;
    }
}
