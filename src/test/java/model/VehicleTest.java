package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private Vehicle vehicle;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        vehicle   = new Vehicle(1, 2);
        passenger = new Passenger(1, "test@test.com");
    }

    @Test
    void newVehicle_hasCapacity() {
        assertTrue(vehicle.hasCapacity());
    }

    @Test
    void addPassenger_returnsTrue_andSetsStateInTransit() {
        assertTrue(vehicle.addPassenger(passenger));
        assertEquals(PassengerState.IN_TRANSIT, passenger.getState());
        assertEquals(1, vehicle.getPassengers().size());
    }

    @Test
    void addPassenger_duplicate_returnsFalse() {
        vehicle.addPassenger(passenger);
        assertFalse(vehicle.addPassenger(passenger));
        assertEquals(1, vehicle.getPassengers().size());
    }

    @Test
    void addPassenger_beyondCapacity_returnsFalse() {
        Passenger p2 = new Passenger(2, "p2@test.com");
        Passenger p3 = new Passenger(3, "p3@test.com");
        vehicle.addPassenger(passenger);
        vehicle.addPassenger(p2);
        assertFalse(vehicle.addPassenger(p3));
    }

    @Test
    void hasCapacity_false_whenFull() {
        vehicle.addPassenger(passenger);
        vehicle.addPassenger(new Passenger(2, "p2@test.com"));
        assertFalse(vehicle.hasCapacity());
    }

    @Test
    void removePassenger_returnsTrue_andSetsStateArrived() {
        vehicle.addPassenger(passenger);
        assertTrue(vehicle.removePassenger(passenger));
        assertEquals(PassengerState.ARRIVED, passenger.getState());
        assertTrue(vehicle.getPassengers().isEmpty());
    }

    @Test
    void removePassenger_notOnBoard_returnsFalse() {
        assertFalse(vehicle.removePassenger(passenger));
    }

    @Test
    void setCurrentRoute_andGet() {
        Route route = new Route(1, vehicle);
        vehicle.setCurrentRoute(route);
        assertSame(route, vehicle.getCurrentRoute());
    }

    @Test
    void getPassengers_returnsUnmodifiableView() {
        vehicle.addPassenger(passenger);
        assertThrows(UnsupportedOperationException.class,
                () -> vehicle.getPassengers().add(new Passenger(99, "x@x.com")));
    }
}
