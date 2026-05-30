package service;

import model.Passenger;
import model.PassengerState;
import model.RouteStop;
import model.Vehicle;
import repository.DataStore;

import java.util.ArrayList;

public class FleetService implements IFleetService {

    private final DataStore dataStore;
    private final IRouteService routeService;

    public FleetService(DataStore dataStore, IRouteService routeService) {
        this.dataStore = dataStore;
        this.routeService = routeService;
    }

    // Nimmt einfach das erste Fahrzeug mit freiem Platz – keine Distanzoptimierung.
    @Override
    public Vehicle getVehicleForPassenger(Passenger passenger) {
        for (Vehicle vehicle : dataStore.getVehicles()) {
            if (vehicle.hasCapacity()) return vehicle;
        }
        return null;
    }

    @Override
    public void confirmArrival(Vehicle vehicle, RouteStop stop) {
        stop.setReached(true);

        // Kopie nötig – removePassenger() verändert den Zustand während wir iterieren
        for (Passenger passenger : new ArrayList<>(stop.getPassengersToDropOff())) {
            vehicle.removePassenger(passenger);
            dataStore.setActiveBookingState(passenger.getId(), "ARRIVED");
        }
        // Passagier ist bereits beim Buchen im Fahrzeug reserviert (WAITING);
        // bei Abholung nur noch Status auf IN_TRANSIT setzen.
        for (Passenger passenger : new ArrayList<>(stop.getPassengersToPickUp())) {
            passenger.setState(PassengerState.IN_TRANSIT);
            dataStore.setActiveBookingState(passenger.getId(), "IN_TRANSIT");
        }

        if (vehicle.getCurrentRoute() != null) {
            routeService.recalcArrivalTimesFromCurrent(vehicle.getCurrentRoute());
            int nextIndex = vehicle.getCurrentRoute().getCurrentStopIndex() + 1;
            vehicle.getCurrentRoute().setCurrentStopIndex(nextIndex);
            // Route freigeben, damit neue Buchungen calcInitialRoute statt
            // calcNewRoute auf der abgeschlossenen Route aufrufen.
            if (vehicle.getCurrentRoute().getCurrentStop() == null) {
                vehicle.setCurrentRoute(null);
            }
        }
    }
}
