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

    @Override
    public Vehicle getVehicleForPassenger(Passenger p) {
        for (Vehicle v : dataStore.getVehicles()) {
            if (v.getPassengers().size() < v.getMaxCapacity()) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void confirmArrival(Vehicle vehicle, RouteStop stop) {
        stop.setReached(true);
        for (Passenger p : new ArrayList<>(stop.getPassengersToDropOff())) {
            vehicle.removePassenger(p);
        }
        for (Passenger p : new ArrayList<>(stop.getPassengersToPickUp())) {
            vehicle.addPassenger(p);
        }
        if (vehicle.getCurrentRoute() != null) {
            int next = vehicle.getCurrentRoute().getCurrentStopIndex() + 1;
            vehicle.getCurrentRoute().setCurrentStopIndex(next);
        }
    }
}
