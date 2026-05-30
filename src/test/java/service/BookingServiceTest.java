package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private Station start, target;
    private Passenger passenger;
    private Vehicle vehicle;
    private Route dummyRoute;

    // Simple stubs — no mocking framework needed
    private IFleetService fleetReturnsVehicle;
    private IFleetService fleetReturnsNull;
    private IRouteService routeReturnsRoute;
    private IRouteService routeReturnsNull;

    @BeforeEach
    void setUp() {
        start    = new Station("Alex", false);
        target   = new Station("Zoo",  false);
        passenger = new Passenger(1, "test@test.com");
        vehicle   = new Vehicle(1, 4);
        dummyRoute = new Route(1, vehicle);
        dummyRoute.setStops(List.of(new RouteStop(start), new RouteStop(target)));

        fleetReturnsVehicle = new IFleetService() {
            public Vehicle getVehicleForPassenger(Passenger p) { return vehicle; }
            public void confirmArrival(Vehicle v, RouteStop s) {}
        };
        fleetReturnsNull = new IFleetService() {
            public Vehicle getVehicleForPassenger(Passenger p) { return null; }
            public void confirmArrival(Vehicle v, RouteStop s) {}
        };
        routeReturnsRoute = new IRouteService() {
            public Route calcInitialRoute(Vehicle v, Station s, Station t, Passenger p) { return dummyRoute; }
            public Route calcNewRoute(Route r, Passenger p, Station s, Station t)       { return dummyRoute; }
            public void recalcArrivalTimesFromCurrent(Route r) {}
        };
        routeReturnsNull = new IRouteService() {
            public Route calcInitialRoute(Vehicle v, Station s, Station t, Passenger p) { return null; }
            public Route calcNewRoute(Route r, Passenger p, Station s, Station t)       { return null; }
            public void recalcArrivalTimesFromCurrent(Route r) {}
        };
    }

    @Test
    void bookRide_noVehicleAvailable_returnsFalse() {
        BookingService bs = new BookingService(routeReturnsRoute, fleetReturnsNull);
        assertFalse(bs.bookRide(start, target, passenger));
    }

    @Test
    void bookRide_routeCalculationFails_returnsFalse() {
        BookingService bs = new BookingService(routeReturnsNull, fleetReturnsVehicle);
        assertFalse(bs.bookRide(start, target, passenger));
    }

    @Test
    void bookRide_success_returnsTrue() {
        BookingService bs = new BookingService(routeReturnsRoute, fleetReturnsVehicle);
        assertTrue(bs.bookRide(start, target, passenger));
    }

    @Test
    void bookRide_success_passengerStateIsWaiting() {
        BookingService bs = new BookingService(routeReturnsRoute, fleetReturnsVehicle);
        bs.bookRide(start, target, passenger);
        // BookingService sets WAITING after vehicle.addPassenger() sets IN_TRANSIT
        assertEquals(PassengerState.WAITING, passenger.getState());
    }

    @Test
    void bookRide_success_vehicleAssignedToPassenger() {
        BookingService bs = new BookingService(routeReturnsRoute, fleetReturnsVehicle);
        bs.bookRide(start, target, passenger);
        assertSame(vehicle, passenger.getAssignedVehicle());
    }

    @Test
    void bookRide_success_pickupAndDropoffStationsSet() {
        BookingService bs = new BookingService(routeReturnsRoute, fleetReturnsVehicle);
        bs.bookRide(start, target, passenger);
        assertSame(start,  passenger.getPickupStation());
        assertSame(target, passenger.getDropoffStation());
    }

    @Test
    void bookRide_vehicleAlreadyHasRoute_usesCalcNewRoute() {
        vehicle.setCurrentRoute(dummyRoute); // vehicle already has a route
        AtomicBoolean newRouteUsed = new AtomicBoolean(false);

        IRouteService spy = new IRouteService() {
            public Route calcInitialRoute(Vehicle v, Station s, Station t, Passenger p) {
                return dummyRoute;
            }
            public Route calcNewRoute(Route r, Passenger p, Station s, Station t) {
                newRouteUsed.set(true);
                return dummyRoute;
            }
            public void recalcArrivalTimesFromCurrent(Route r) {}
        };

        BookingService bs = new BookingService(spy, fleetReturnsVehicle);
        assertTrue(bs.bookRide(start, target, passenger));
        assertTrue(newRouteUsed.get(), "calcNewRoute should have been called");
    }

    @Test
    void bookRide_vehicleNoRoute_usesCalcInitialRoute() {
        // vehicle.getCurrentRoute() is null by default
        AtomicBoolean initialRouteUsed = new AtomicBoolean(false);

        IRouteService spy = new IRouteService() {
            public Route calcInitialRoute(Vehicle v, Station s, Station t, Passenger p) {
                initialRouteUsed.set(true);
                return dummyRoute;
            }
            public Route calcNewRoute(Route r, Passenger p, Station s, Station t) { return dummyRoute; }
            public void recalcArrivalTimesFromCurrent(Route r) {}
        };

        BookingService bs = new BookingService(spy, fleetReturnsVehicle);
        bs.bookRide(start, target, passenger);
        assertTrue(initialRouteUsed.get(), "calcInitialRoute should have been called");
    }
}
