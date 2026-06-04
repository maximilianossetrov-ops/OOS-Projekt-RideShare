package service;

import static org.junit.jupiter.api.Assertions.*;

import model.Passenger;
import model.Route;
import model.RouteStop;
import model.Station;
import model.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class TimeServiceTestGetRemainingTime {

    private final TimeService timeService = new TimeService();

    @Test
    void getWaitingTime_returnsMinusOne_whenPassengerHasNoAssignedVehicle() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "passwort");

        int result = timeService.getWaitingTime(passenger);

        assertEquals(-1, result);
    }

    @Test
    void getWaitingTime_returnsWaitingTime_whenPassengerWillBePickedUp() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "passwort");

        Vehicle vehicle = new Vehicle(1, 4);
        Route route = new Route(1, vehicle);
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        Station station = new Station("Hauptbahnhof", true);

        RouteStop stop = new RouteStop(station);
        stop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));
        stop.addPassengerToPickUp(passenger);

        route.addStop(stop);

        int result = timeService.getWaitingTime(passenger);

        assertTrue(result >= 9 && result <= 10);
    }

    @Test
    void getWaitingTime_returnsMinusOne_whenPassengerIsNotInAnyPickupList() {
        Passenger passenger =
                new Passenger(1, "Max", "max@test.de", "passwort");

        Passenger otherPassenger =
                new Passenger(2, "Anna", "anna@test.de", "passwort");

        Vehicle vehicle = new Vehicle(1, 4);
        Route route = new Route(1, vehicle);
        vehicle.setCurrentRoute(route);
        passenger.setAssignedVehicle(vehicle);

        Station station = new Station("Hauptbahnhof", true);

        RouteStop stop = new RouteStop(station);
        stop.setPlannedArrivalTime(LocalDateTime.now().plusMinutes(10));
        stop.addPassengerToPickUp(otherPassenger);

        route.addStop(stop);

        int result = timeService.getWaitingTime(passenger);

        assertEquals(-1, result);
    }
}