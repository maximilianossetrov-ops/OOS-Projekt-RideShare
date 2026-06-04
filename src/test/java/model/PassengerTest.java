package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PassengerTest {

    @Test
    void vollstaendigerKonstruktorSetztFelder() {
        Passenger p = new Passenger(1, "Max", "max@example.com", "geheim");
        assertEquals(1, p.getId());
        assertEquals("Max", p.getName());
        assertEquals("max@example.com", p.getEmail());
        assertEquals("geheim", p.getPassword());
    }

    @Test
    void standardzustandIstWaiting() {
        Passenger p = new Passenger(1, "Anna", "anna@example.com", "pw");
        assertEquals(PassengerState.WAITING, p.getState());
    }

    @Test
    void emailKonstruktorLeitenameAb() {
        Passenger p = new Passenger(2, "hans@example.com");
        assertEquals("hans", p.getName());
        assertEquals("hans@example.com", p.getEmail());
    }

    @Test
    void nullPasswortWirdLeerString() {
        Passenger p = new Passenger(1, "Max", "max@example.com", null);
        assertEquals("", p.getPassword());
    }

    @Test
    void setStateSetzt() {
        Passenger p = new Passenger(1, "Test", "test@test.com", "pw");
        p.setState(PassengerState.IN_TRANSIT);
        assertEquals(PassengerState.IN_TRANSIT, p.getState());
        p.setState(PassengerState.ARRIVED);
        assertEquals(PassengerState.ARRIVED, p.getState());
    }

    @Test
    void fahrzeugUndStationenZuweisbar() {
        Passenger p = new Passenger(1, "Test", "test@test.com", "pw");
        Vehicle v = new Vehicle(1, 4);
        Station pickup  = new Station("A", false);
        Station dropoff = new Station("B", false);

        p.setAssignedVehicle(v);
        p.setPickupStation(pickup);
        p.setDropoffStation(dropoff);

        assertSame(v, p.getAssignedVehicle());
        assertEquals(pickup, p.getPickupStation());
        assertEquals(dropoff, p.getDropoffStation());
    }
}
