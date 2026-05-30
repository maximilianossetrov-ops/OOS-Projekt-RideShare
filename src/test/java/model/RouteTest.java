package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    private Route route;
    private RouteStop stop0, stop1, stop2;

    @BeforeEach
    void setUp() {
        Vehicle v = new Vehicle(1, 4);
        route = new Route(1, v);

        stop0 = new RouteStop(new Station("A", true));
        stop1 = new RouteStop(new Station("B", false));
        stop2 = new RouteStop(new Station("C", false));
        route.setStops(List.of(stop0, stop1, stop2));
    }

    @Test
    void getCurrentStop_atIndex0_returnsFirstStop() {
        assertSame(stop0, route.getCurrentStop());
    }

    @Test
    void getNextStop_atIndex0_returnsSecondStop() {
        assertSame(stop1, route.getNextStop());
    }

    @Test
    void getNextStop_atLastStop_returnsNull() {
        route.setCurrentStopIndex(2);
        assertNull(route.getNextStop());
    }

    @Test
    void getCurrentStop_null_whenIndexBeyondStops() {
        route.setCurrentStopIndex(99);
        assertNull(route.getCurrentStop());
    }

    @Test
    void setCurrentStopIndex_advancesCurrentStop() {
        route.setCurrentStopIndex(1);
        assertSame(stop1, route.getCurrentStop());
        assertSame(stop2, route.getNextStop());
    }

    @Test
    void setStops_replacesExistingStops() {
        RouteStop newStop = new RouteStop(new Station("X", false));
        route.setStops(List.of(newStop));
        assertEquals(1, route.getStops().size());
        assertSame(newStop, route.getCurrentStop());
    }

    @Test
    void addStop_appendsToList() {
        int before = route.getStops().size();
        route.addStop(new RouteStop(new Station("D", false)));
        assertEquals(before + 1, route.getStops().size());
    }

    @Test
    void getVehicle_returnsAssignedVehicle() {
        Vehicle v = new Vehicle(5, 4);
        Route r = new Route(2, v);
        assertSame(v, r.getVehicle());
    }
}
