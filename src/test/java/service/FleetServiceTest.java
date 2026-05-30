package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.DataStore;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FleetServiceTest {

    private DataStore dataStore;
    private FleetService fleetService;
    private Vehicle vehicle;
    private Passenger passenger;

    // No-op stub for IRouteService
    private static final IRouteService NO_OP_ROUTE = new IRouteService() {
        public Route calcInitialRoute(Vehicle v, Station s, Station t, Passenger p) { return null; }
        public Route calcNewRoute(Route r, Passenger p, Station s, Station t)       { return null; }
        public void recalcArrivalTimesFromCurrent(Route r) {}
    };

    @BeforeEach
    void setUp() {
        dataStore    = new DataStore();
        vehicle      = new Vehicle(1, 4);
        passenger    = new Passenger(1, "test@test.com");
        dataStore.addVehicle(vehicle);
        fleetService = new FleetService(dataStore, NO_OP_ROUTE);
    }

    // ── getVehicleForPassenger ─────────────────────────────────────────────────

    @Test
    void getVehicleForPassenger_returnsFirstVehicleWithCapacity() {
        Vehicle result = fleetService.getVehicleForPassenger(passenger);
        assertSame(vehicle, result);
    }

    @Test
    void getVehicleForPassenger_allFull_returnsNull() {
        // Fill vehicle to max capacity (4)
        for (int i = 0; i < 4; i++) {
            vehicle.addPassenger(new Passenger(i + 10, "p" + i + "@test.com"));
        }
        assertNull(fleetService.getVehicleForPassenger(passenger));
    }

    @Test
    void getVehicleForPassenger_multipleVehicles_skipsFullOnes() {
        Vehicle full  = new Vehicle(2, 1);
        Vehicle free  = new Vehicle(3, 4);
        full.addPassenger(new Passenger(99, "x@x.com"));

        DataStore ds = new DataStore();
        ds.addVehicle(full);
        ds.addVehicle(free);
        FleetService fs = new FleetService(ds, NO_OP_ROUTE);

        assertSame(free, fs.getVehicleForPassenger(passenger));
    }

    // ── confirmArrival ─────────────────────────────────────────────────────────

    @Test
    void confirmArrival_setsStopAsReached() {
        RouteStop stop = buildStop(List.of(), List.of());
        setupRouteOnVehicle(vehicle, stop);
        fleetService.confirmArrival(vehicle, stop);
        assertTrue(stop.isReached());
    }

    @Test
    void confirmArrival_dropsOffPassengers_setsStateArrived() {
        vehicle.addPassenger(passenger); // put on board (IN_TRANSIT)
        RouteStop stop = buildStop(List.of(), List.of(passenger));
        setupRouteOnVehicle(vehicle, stop);

        fleetService.confirmArrival(vehicle, stop);

        assertEquals(PassengerState.ARRIVED, passenger.getState());
        assertFalse(vehicle.getPassengers().contains(passenger));
    }

    @Test
    void confirmArrival_picksUpPassengers_setsStateInTransit() {
        RouteStop stop = buildStop(List.of(passenger), List.of());
        setupRouteOnVehicle(vehicle, stop);

        fleetService.confirmArrival(vehicle, stop);

        assertEquals(PassengerState.IN_TRANSIT, passenger.getState());
    }

    @Test
    void confirmArrival_advancesCurrentStopIndex() {
        RouteStop stop0 = buildStop(List.of(), List.of());
        RouteStop stop1 = buildStop(List.of(), List.of());
        Route route = new Route(1, vehicle);
        route.setStops(List.of(stop0, stop1));
        vehicle.setCurrentRoute(route);

        fleetService.confirmArrival(vehicle, stop0);

        assertEquals(1, vehicle.getCurrentRoute().getCurrentStopIndex());
    }

    @Test
    void confirmArrival_lastStop_clearsRouteFromVehicle() {
        RouteStop onlyStop = buildStop(List.of(), List.of());
        setupRouteOnVehicle(vehicle, onlyStop); // route has only this stop

        fleetService.confirmArrival(vehicle, onlyStop);

        // After advancing past last stop, getCurrentStop() returns null → route cleared
        assertNull(vehicle.getCurrentRoute());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RouteStop buildStop(List<Passenger> toPickUp, List<Passenger> toDropOff) {
        RouteStop stop = new RouteStop(new Station("TestStation", false));
        toPickUp.forEach(stop::addPassengerToPickUp);
        toDropOff.forEach(stop::addPassengerToDropOff);
        return stop;
    }

    private void setupRouteOnVehicle(Vehicle v, RouteStop stop) {
        Route route = new Route(1, v);
        route.setStops(List.of(stop));
        v.setCurrentRoute(route);
    }
}
