package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    private Passenger neuenFahrgast(int id) {
        return new Passenger(id, "Fahrgast" + id, "fg" + id + "@test.com", "pw");
    }

    @Test
    void hatKapazitaetWennLeer() {
        assertTrue(new Vehicle(1, 3).hasCapacity());
    }

    @Test
    void keineKapazitaetWennVoll() {
        Vehicle v = new Vehicle(1, 1);
        v.addPassenger(neuenFahrgast(1));
        assertFalse(v.hasCapacity());
    }

    @Test
    void addPassengerSetzteInTransit() {
        Vehicle v = new Vehicle(1, 4);
        Passenger p = neuenFahrgast(1);
        assertEquals(PassengerState.WAITING, p.getState());

        assertTrue(v.addPassenger(p));
        assertEquals(PassengerState.IN_TRANSIT, p.getState());
        assertEquals(1, v.getPassengers().size());
    }

    @Test
    void addPassengerFehltBeiVollemFahrzeug() {
        Vehicle v = new Vehicle(1, 1);
        assertTrue(v.addPassenger(neuenFahrgast(1)));
        assertFalse(v.addPassenger(neuenFahrgast(2)));
        assertEquals(1, v.getPassengers().size());
    }

    @Test
    void addPassengerFehltBeiDuplikat() {
        Vehicle v = new Vehicle(1, 4);
        Passenger p = neuenFahrgast(1);
        assertTrue(v.addPassenger(p));
        assertFalse(v.addPassenger(p));
    }

    @Test
    void removePassengerSetzteArrived() {
        Vehicle v = new Vehicle(1, 4);
        Passenger p = neuenFahrgast(1);
        v.addPassenger(p);

        assertTrue(v.removePassenger(p));
        assertEquals(PassengerState.ARRIVED, p.getState());
        assertTrue(v.getPassengers().isEmpty());
    }

    @Test
    void removePassengerFehltWennNichtAnBord() {
        Vehicle v = new Vehicle(1, 4);
        assertFalse(v.removePassenger(neuenFahrgast(99)));
    }

    @Test
    void passagierlIsteIstUnveraenderlich() {
        Vehicle v = new Vehicle(1, 4);
        assertThrows(UnsupportedOperationException.class,
                () -> v.getPassengers().add(neuenFahrgast(1)));
    }
}
