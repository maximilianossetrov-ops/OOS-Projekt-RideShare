package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TimeServiceTest {

    private TimeService timeService;
    private Passenger passenger;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        timeService = new TimeService();
        passenger   = new Passenger(1, "test@test.com");
        vehicle     = new Vehicle(1, 4);
    }

    @Test
    void getWaitingTime_noVehicleAssigned_returnsMinusOne() {
        assertEquals(-1, timeService.getWaitingTime(passenger));
    }

    @Test
    void getWaitingTime_vehicleWithNoRoute_returnsMinusOne() {
        passenger.setAssignedVehicle(vehicle);
        assertEquals(-1, timeService.getWaitingTime(passenger));
    }

    @Test
    void getWaitingTime_passengerNotInAnyPickupStop_returnsMinusOne() {
        Route route = buildRouteWithoutPassenger(vehicle);
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);
        assertEquals(-1, timeService.getWaitingTime(passenger));
    }

    @Test
    void getWaitingTime_futurePickupStop_returnsPositiveMinutes() {
        RouteStop pickupStop = new RouteStop(new Station("Alex", false));
        pickupStop.addPassengerToPickUp(passenger);
        pickupStop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));

        Route route = new Route(1, vehicle);
        route.setStops(List.of(pickupStop));
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        int wait = timeService.getWaitingTime(passenger);
        assertTrue(wait >= 0 && wait <= 10,
                "Wartezeit sollte zwischen 0 und 10 min liegen, war: " + wait);
    }

    @Test
    void getRemainingTime_noVehicleAssigned_returnsMinusOne() {
        assertEquals(-1, timeService.getRemainingTime(passenger));
    }

    @Test
    void getRemainingTime_vehicleWithNoRoute_returnsMinusOne() {
        passenger.setAssignedVehicle(vehicle);
        assertEquals(-1, timeService.getRemainingTime(passenger));
    }

    @Test
    void getRemainingTime_futureDropoffStop_returnsPositiveMinutes() {
        RouteStop dropoffStop = new RouteStop(new Station("Zoo", false));
        dropoffStop.addPassengerToDropOff(passenger);
        dropoffStop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(20));

        Route route = new Route(1, vehicle);
        route.setStops(List.of(dropoffStop));
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        int remaining = timeService.getRemainingTime(passenger);
        assertTrue(remaining >= 0 && remaining <= 20,
                "Restzeit sollte zwischen 0 und 20 min liegen, war: " + remaining);
    }

    @Test
    void getWaitingTime_pastPickupStop_ignoredBecauseIndexAdvanced() {
        RouteStop pastStop   = new RouteStop(new Station("Past", false));
        RouteStop futureStop = new RouteStop(new Station("Future", false));
        pastStop.addPassengerToPickUp(passenger);
        pastStop.setPlannedArrivalTime(LocalDateTime.now().minusMinutes(5));
        futureStop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));

        Route route = new Route(1, vehicle);
        route.setStops(List.of(pastStop, futureStop));
        route.setCurrentStopIndex(1); // current stop is futureStop; pastStop is behind us
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        // Passenger's pickup is in a past stop → not found from currentStopIndex
        assertEquals(-1, timeService.getWaitingTime(passenger));
    }

    private Route buildRouteWithoutPassenger(Vehicle v) {
        Route r = new Route(1, v);
        RouteStop s = new RouteStop(new Station("X", false));
        s.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(5));
        r.setStops(List.of(s));
        return r;
    }
}
