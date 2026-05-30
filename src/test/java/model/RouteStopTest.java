package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RouteStopTest {

    private RouteStop stop;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        stop      = new RouteStop(new Station("Alexanderplatz", false));
        passenger = new Passenger(1, "test@test.com");
    }

    @Test
    void newStop_notReached() {
        assertFalse(stop.isReached());
    }

    @Test
    void setReached_true_changesFlag() {
        stop.setReached(true);
        assertTrue(stop.isReached());
    }

    @Test
    void setReached_false_changesFlag() {
        stop.setReached(true);
        stop.setReached(false);
        assertFalse(stop.isReached());
    }

    @Test
    void addPassengerToPickUp_appearsInList() {
        stop.addPassengerToPickUp(passenger);
        assertTrue(stop.getPassengersToPickUp().contains(passenger));
    }

    @Test
    void addPassengerToDropOff_appearsInList() {
        stop.addPassengerToDropOff(passenger);
        assertTrue(stop.getPassengersToDropOff().contains(passenger));
    }

    @Test
    void pickupAndDropoffListsAreIndependent() {
        stop.addPassengerToPickUp(passenger);
        assertTrue(stop.getPassengersToDropOff().isEmpty());

        Passenger p2 = new Passenger(2, "p2@test.com");
        stop.addPassengerToDropOff(p2);
        assertEquals(1, stop.getPassengersToPickUp().size());
        assertEquals(1, stop.getPassengersToDropOff().size());
    }

    @Test
    void setPlannedArrivalTime_andGet() {
        LocalDateTime time = LocalDateTime.now().plusMinutes(15);
        stop.setPlannedArrivalTime(time);
        assertEquals(time, stop.getPlannedArrivalTime());
    }

    @Test
    void getStation_returnsCorrectStation() {
        assertEquals("Alexanderplatz", stop.getStation().getName());
    }

    @Test
    void passengersToPickUp_returnsUnmodifiableView() {
        assertThrows(UnsupportedOperationException.class,
                () -> stop.getPassengersToPickUp().add(passenger));
    }
}
