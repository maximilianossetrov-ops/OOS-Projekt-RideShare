package service;

import model.Passenger;
import model.RouteStop;
import model.Vehicle;
import repository.DataStore;

import java.util.ArrayList;

/**
 * Implementierung des IFleetService.
 * Verwaltet die Fahrzeugauswahl und verarbeitet Haltestellenankünfte.
 */
public class FleetService implements IFleetService {

    private final DataStore dataStore;

    public FleetService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Wählt das erste Fahrzeug aus der Flotte, das noch freie Plätze hat.
     * Eine ausgefeiltere Strategie könnte z. B. das nächstgelegene Fahrzeug bevorzugen.
     */
    @Override
    public Vehicle getVehicleForPassenger(Passenger passenger) {
        for (Vehicle vehicle : dataStore.getVehicles()) {
            if (vehicle.hasCapacity()) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * Verarbeitet die Ankunft eines Fahrzeugs an einem Haltepunkt:
     * 1. Halt als erreicht markieren
     * 2. Fahrgäste aussteigen lassen
     * 3. Fahrgäste einsteigen lassen
     * 4. Routenindex auf den nächsten Halt setzen
     */
    @Override
    public void confirmArrival(Vehicle vehicle, RouteStop stop) {
        stop.setReached(true);

        // Kopie der Liste nötig, da removePassenger den Zustand des Fahrgastes ändert
        for (Passenger passenger : new ArrayList<>(stop.getPassengersToDropOff())) {
            vehicle.removePassenger(passenger);
        }
        for (Passenger passenger : new ArrayList<>(stop.getPassengersToPickUp())) {
            vehicle.addPassenger(passenger);
        }

        // Routenzeiger auf den nächsten Halt vorrücken
        if (vehicle.getCurrentRoute() != null) {
            int nextIndex = vehicle.getCurrentRoute().getCurrentStopIndex() + 1;
            vehicle.getCurrentRoute().setCurrentStopIndex(nextIndex);
        }
    }
}
