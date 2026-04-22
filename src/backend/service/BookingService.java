package service;

import model.*;
import repository.DataStore;
import service.*;
import model.*;
import ui.Main;

import static model.PassengerState.IN_TRANSIT;

public class BookingService {
    // Der BookingService braucht Zugriff auf die anderen Gehirne
    private RoutingService routingService;
    private FleetService fleetService;

    public BookingService() {
        this.routingService = new RoutingService();
        this.fleetService = new FleetService();
    }


    public boolean bookRide(Station start, Station target, Passenger p) {
        System.out.println("Buchungsanfrage für: " + p.getName());

        // 1. Ein passendes Fahrzeug finden
        Vehicle v = fleetService.getVehicleForPassenger(p);

        if (v == null) {
            System.out.println("Fehler: Kein freies Fahrzeug verfügbar!");
            return false;
        }

        // 2. Die beste Route berechnen lassen
        // Übergeben die aktuelle Route des Autos und den neuen Gast
        Route neueRoute = routingService.calcNewRoute(v.getCurrentRoute(), p, start, target);

        if (neueRoute == null) {
            System.out.println("Fehler: Route konnte nicht berechnet werden.");
            return false;
        }

        // 3. Dem Fahrzeug die neue Route zuweisen
        v.setCurrentRoute(neueRoute);
        v.getPassengers().add(p);
        p.setState(IN_TRANSIT); // Passagier ist jetzt unterwegs

        System.out.println("Erfolg! " + p.getName() + " wurde Fahrzeug " + v.getId() + " zugewiesen.");
        return true;
    }

    public void confirmArrival(Vehicle v, RouteStop stop) {
        // Logik wenn ein Fahrzeug ankommt (Passagiere aussteigen lassen etc.)
        System.out.println("Fahrzeug " + v.getId() + " ist an Station " + stop.getStation().getName() + " angekommen.");
        stop.setReached(true);
    }
}