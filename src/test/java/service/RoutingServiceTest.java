package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.DataStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoutingServiceTest {

    private DataStore dataStore;
    private RoutingService routingService;

    private Station depot;
    private Station stationA;
    private Station stationB;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        routingService = new RoutingService(dataStore);

        depot    = new Station("Depot", true);
        stationA = new Station("Alexanderplatz", false);
        stationB = new Station("Hauptbahnhof", false);

        dataStore.addStation(depot);
        dataStore.addStation(stationA);
        dataStore.addStation(stationB);

        dataStore.addConnection(new Connection(depot,    stationA, 5));
        dataStore.addConnection(new Connection(stationA, stationB, 10));
        dataStore.addConnection(new Connection(stationB, depot,    8));
    }

    @Test
    void routeWirdBerechnet() {
        Vehicle vehicle   = new Vehicle(1, 4);
        Passenger passenger = new Passenger(1, "Test", "test@test.com", "pw");

        Route route = routingService.calcInitialRoute(vehicle, stationA, stationB, passenger);
        assertNotNull(route);
    }

    @Test
    void routeEnthaeltAbholhalt() {
        Vehicle vehicle   = new Vehicle(1, 4);
        Passenger passenger = new Passenger(1, "Test", "test@test.com", "pw");

        Route route = routingService.calcInitialRoute(vehicle, stationA, stationB, passenger);
        assertNotNull(route);

        boolean hatAbholhalt = route.getStops().stream()
                .anyMatch(s -> s.getPassengersToPickUp().contains(passenger));
        assertTrue(hatAbholhalt, "Route muss einen Abholhalt enthalten");
    }

    @Test
    void routeEnthaeltAbsatzhalt() {
        Vehicle vehicle   = new Vehicle(1, 4);
        Passenger passenger = new Passenger(1, "Test", "test@test.com", "pw");

        Route route = routingService.calcInitialRoute(vehicle, stationA, stationB, passenger);
        assertNotNull(route);

        boolean hatAbsatzhalt = route.getStops().stream()
                .anyMatch(s -> s.getPassengersToDropOff().contains(passenger));
        assertTrue(hatAbsatzhalt, "Route muss einen Absatzhalt enthalten");
    }

    @Test
    void abholungVorAbsatz() {
        Vehicle vehicle   = new Vehicle(1, 4);
        Passenger passenger = new Passenger(1, "Test", "test@test.com", "pw");

        Route route = routingService.calcInitialRoute(vehicle, stationA, stationB, passenger);
        assertNotNull(route);

        List<RouteStop> stops = route.getStops();
        int pickupIdx  = -1;
        int dropoffIdx = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getPassengersToPickUp().contains(passenger))  pickupIdx  = i;
            if (stops.get(i).getPassengersToDropOff().contains(passenger)) dropoffIdx = i;
        }
        assertTrue(pickupIdx >= 0 && dropoffIdx > pickupIdx,
                "Abholung muss vor der Abgabe liegen");
    }

    @Test
    void ohneDepotWirdNullZurueckgegeben() {
        DataStore ohneDepot = new DataStore();
        RoutingService service = new RoutingService(ohneDepot);

        ohneDepot.addStation(stationA);
        ohneDepot.addStation(stationB);
        ohneDepot.addConnection(new Connection(stationA, stationB, 10));

        Route route = service.calcInitialRoute(new Vehicle(1, 4),
                stationA, stationB, new Passenger(1, "Test", "test@test.com", "pw"));
        assertNull(route, "Ohne Depot muss null zurückgegeben werden");
    }

    @Test
    void routeHatAnkunftszeitenGesetzt() {
        Vehicle vehicle   = new Vehicle(1, 4);
        Passenger passenger = new Passenger(1, "Test", "test@test.com", "pw");

        Route route = routingService.calcInitialRoute(vehicle, stationA, stationB, passenger);
        assertNotNull(route);

        route.getStops().forEach(stop ->
                assertNotNull(stop.getPlannedArrivalTime(), "Jeder Halt braucht eine geplante Ankunftszeit"));
    }
}
