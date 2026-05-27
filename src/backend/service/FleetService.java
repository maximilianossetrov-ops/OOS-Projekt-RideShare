package service;

import model.Passenger;
import model.RouteStop;
import model.Vehicle;
import repository.DataStore;

import java.util.ArrayList;

public class FleetService implements IFleetService {

    private final DataStore dataStore;

    public FleetService(DataStore dataStore) {
        this.dataStore = dataStore;
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
        }
        for (Passenger passenger : new ArrayList<>(stop.getPassengersToPickUp())) {
            vehicle.addPassenger(passenger);
        }

        if (vehicle.getCurrentRoute() != null) {
            int nextIndex = vehicle.getCurrentRoute().getCurrentStopIndex() + 1;
            vehicle.getCurrentRoute().setCurrentStopIndex(nextIndex);
        }
    }
}
