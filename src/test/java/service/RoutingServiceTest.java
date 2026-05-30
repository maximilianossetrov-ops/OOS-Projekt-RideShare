package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.DataSetup;
import repository.DataStore;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoutingServiceTest {

    private DataStore dataStore;
    private RoutingService routingService;

    // Frequently used stations (loaded from DataSetup)
    private Station depot, alex, zoo, museum, friedrichStr, kotti;

    @BeforeEach
    void setUp() {
        dataStore      = new DataStore();
        DataSetup.fillDatabase(dataStore);
        routingService = new RoutingService(dataStore);

        depot        = station("Zentrale (Depot)");
        alex         = station("Alexanderplatz");
        zoo          = station("Zoologischer Garten");
        museum       = station("Museumsinsel");
        friedrichStr = station("Friedrichstraße");
        kotti        = station("Kottbusser Tor");
    }

    // ── calcInitialRoute ───────────────────────────────────────────────────────

    @Test
    void calcInitialRoute_validConnection_returnsNonNullRoute() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        assertNotNull(routingService.calcInitialRoute(v, alex, zoo, p));
    }

    @Test
    void calcInitialRoute_routeStartsAtDepot() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);

        assertNotNull(route);
        assertEquals(depot, route.getStops().get(0).getStation());
    }

    @Test
    void calcInitialRoute_routeEndsAtDepot() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);

        assertNotNull(route);
        List<RouteStop> stops = route.getStops();
        assertEquals(depot, stops.get(stops.size() - 1).getStation());
    }

    @Test
    void calcInitialRoute_containsPickupStop_forPassenger() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);

        assertNotNull(route);
        boolean hasPickup = route.getStops().stream()
                .anyMatch(s -> s.getStation().equals(alex)
                        && s.getPassengersToPickUp().contains(p));
        assertTrue(hasPickup, "Pickup-Haltepunkt (Alex) fehlt in der Route");
    }

    @Test
    void calcInitialRoute_containsDropoffStop_forPassenger() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);

        assertNotNull(route);
        boolean hasDropoff = route.getStops().stream()
                .anyMatch(s -> s.getStation().equals(zoo)
                        && s.getPassengersToDropOff().contains(p));
        assertTrue(hasDropoff, "Dropoff-Haltepunkt (Zoo) fehlt in der Route");
    }

    @Test
    void calcInitialRoute_pickupBeforeDropoff() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);

        assertNotNull(route);
        List<RouteStop> stops = route.getStops();
        int pickupIdx  = -1, dropoffIdx = -1;
        for (int i = 0; i < stops.size(); i++) {
            // Find FIRST occurrence (start station may appear again on return leg)
            if (pickupIdx  == -1 && stops.get(i).getPassengersToPickUp().contains(p))  pickupIdx  = i;
            if (dropoffIdx == -1 && stops.get(i).getPassengersToDropOff().contains(p)) dropoffIdx = i;
        }
        assertTrue(pickupIdx  != -1, "Kein Pickup gefunden");
        assertTrue(dropoffIdx != -1, "Kein Dropoff gefunden");
        assertTrue(pickupIdx < dropoffIdx, "Pickup muss vor Dropoff liegen");
    }

    @Test
    void calcInitialRoute_allStopsHavePlannedArrivalTime() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, museum, friedrichStr, p);

        assertNotNull(route);
        boolean allHaveTimes = route.getStops().stream()
                .allMatch(s -> s.getPlannedArrivalTime() != null);
        assertTrue(allHaveTimes, "Alle Stops müssen eine geplante Ankunftszeit haben");
    }

    // ── calcNewRoute ───────────────────────────────────────────────────────────

    @Test
    void calcNewRoute_insertsPickupAndDropoff_forSecondPassenger() {
        Vehicle v  = new Vehicle(1, 4);
        Passenger p1 = new Passenger(1, "p1@test.com");
        Passenger p2 = new Passenger(2, "p2@test.com");

        Route route = routingService.calcInitialRoute(v, alex, zoo, p1);
        assertNotNull(route);
        v.setCurrentRoute(route);

        Route updated = routingService.calcNewRoute(route, p2, museum, friedrichStr);
        assertNotNull(updated, "calcNewRoute sollte eine Route zurückgeben");

        boolean hasPickup2  = updated.getStops().stream()
                .anyMatch(s -> s.getStation().equals(museum) && s.getPassengersToPickUp().contains(p2));
        boolean hasDropoff2 = updated.getStops().stream()
                .anyMatch(s -> s.getStation().equals(friedrichStr) && s.getPassengersToDropOff().contains(p2));

        assertTrue(hasPickup2,  "Pickup für p2 (Museum) muss in Route enthalten sein");
        assertTrue(hasDropoff2, "Dropoff für p2 (FriedrichStr) muss in Route enthalten sein");
    }

    @Test
    void calcNewRoute_capacityExceeded_returnsNull() {
        Vehicle v = new Vehicle(1, 1); // capacity of 1
        Passenger p1 = new Passenger(1, "p1@test.com");

        Route route = routingService.calcInitialRoute(v, alex, zoo, p1);
        assertNotNull(route);
        v.addPassenger(p1); // p1 is now on board → vehicle is full

        Passenger p2 = new Passenger(2, "p2@test.com");
        Route result = routingService.calcNewRoute(route, p2, kotti, museum);
        assertNull(result, "Soll null zurückgeben wenn Kapazität überschritten");
    }

    @Test
    void calcNewRoute_sameRouteObject_returned() {
        Vehicle v  = new Vehicle(1, 4);
        Passenger p1 = new Passenger(1, "p1@test.com");
        Passenger p2 = new Passenger(2, "p2@test.com");

        Route route = routingService.calcInitialRoute(v, alex, zoo, p1);
        assertNotNull(route);
        Route result = routingService.calcNewRoute(route, p2, museum, friedrichStr);

        assertSame(route, result, "calcNewRoute soll die selbe Route-Instanz modifizieren und zurückgeben");
    }

    // ── recalcArrivalTimesFromCurrent ──────────────────────────────────────────

    @Test
    void recalcArrivalTimesFromCurrent_setsTimesOnFutureStops() {
        Vehicle v   = new Vehicle(1, 4);
        Passenger p = new Passenger(1, "p@test.com");
        Route route = routingService.calcInitialRoute(v, alex, zoo, p);
        assertNotNull(route);

        // Reset all times to null to verify recalc sets them
        route.getStops().forEach(s -> s.setPlannedArrivalTime(null));
        routingService.recalcArrivalTimesFromCurrent(route);

        boolean allSet = route.getStops().stream()
                .skip(route.getCurrentStopIndex()) // only from current stop onwards
                .allMatch(s -> s.getPlannedArrivalTime() != null);
        assertTrue(allSet, "Alle Stops ab currentIndex müssen Zeiten bekommen");
    }

    // ── Dijkstra via arrival times ─────────────────────────────────────────────

    @Test
    void dijkstra_shorterPathArrivesEarlier_thanLongerPath() {
        // Museum → Brandenburger Tor: 2 min direct
        // Museum → Alex(3) → Depot(10) → Brandenburger Tor: much longer
        // So Museum→BrandenburgerTor route should be shorter than Museum→Zoo route
        Vehicle v1 = new Vehicle(1, 4);
        Vehicle v2 = new Vehicle(2, 4);
        Passenger p1 = new Passenger(1, "p1@test.com");
        Passenger p2 = new Passenger(2, "p2@test.com");

        Station brandenburgTor = station("Brandenburger Tor");
        Route shortRoute = routingService.calcInitialRoute(v1, museum, brandenburgTor, p1);
        Route longRoute  = routingService.calcInitialRoute(v2, museum, zoo, p2);

        assertNotNull(shortRoute);
        assertNotNull(longRoute);

        // Find dropoff times
        var shortDropoff = shortRoute.getStops().stream()
                .filter(s -> s.getPassengersToDropOff().contains(p1)).findFirst().orElseThrow();
        var longDropoff  = longRoute.getStops().stream()
                .filter(s -> s.getPassengersToDropOff().contains(p2)).findFirst().orElseThrow();

        assertTrue(
                shortDropoff.getPlannedArrivalTime().isBefore(longDropoff.getPlannedArrivalTime()),
                "Kürzere Route (Museum→BrandenburgerTor=2min) muss früher ankommen als längere (Museum→Zoo)"
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Station station(String name) {
        return dataStore.getStations().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Station nicht gefunden: " + name));
    }
}
