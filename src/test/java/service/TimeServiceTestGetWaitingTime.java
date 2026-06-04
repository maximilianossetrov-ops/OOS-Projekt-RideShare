package service;

import model.*;
import org.junit.jupiter.api.Test;
import service.TimeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeServiceTestGetWaitingTime {

    private final TimeService timeService = new TimeService();

    @Test
    void getWaitingTime_returnsWaitingTime_whenPassengerWillBePickedUp() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "pw");

        Vehicle vehicle = new Vehicle(1, 4);
        Route route = new Route(1, vehicle);
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        Station station = new Station("Museumsinsel", false);
        RouteStop stop = new RouteStop(station);
        stop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));
        stop.addPassengerToPickUp(passenger);

        route.setStops(List.of(stop));
        route.setCurrentStopIndex(0);

        int waitingTime = timeService.getWaitingTime(passenger);

        assertTrue(waitingTime >= 9 && waitingTime <= 10);
    }

    @Test
    void getWaitingTime_returnsMinusOne_whenPassengerHasNoAssignedVehicle() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "pw");

        int waitingTime = timeService.getWaitingTime(passenger);

        assertEquals(-1, waitingTime);
    }

    @Test
    void getWaitingTime_returnsMinusOne_whenPassengerIsNotScheduledForPickup() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "pw");

        Passenger otherPassenger =
                new Passenger(2, "Anna", "anna@test.de", "pw");

        Vehicle vehicle = new Vehicle(1, 4);
        Route route = new Route(1, vehicle);
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        Station station = new Station("Museumsinsel", false);
        RouteStop stop = new RouteStop(station);
        stop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));

        // Nur anderer Passagier wird abgeholt
        stop.addPassengerToPickUp(otherPassenger);

        route.setStops(List.of(stop));
        route.setCurrentStopIndex(0);

        int waitingTime = timeService.getWaitingTime(passenger);

        assertEquals(-1, waitingTime);
    }
}