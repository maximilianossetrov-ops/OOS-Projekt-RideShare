package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

    @Test
    void konstruktorSetztAlleFelder() {
        Booking b = new Booking(1, 10, 5, "Alexanderplatz", "Hauptbahnhof", "WAITING", "2024-01-01T10:00");
        assertEquals(1, b.getBookingId());
        assertEquals(10, b.getPassengerId());
        assertEquals(5, b.getVehicleId());
        assertEquals("Alexanderplatz", b.getPickupStationName());
        assertEquals("Hauptbahnhof", b.getDropoffStationName());
        assertEquals("WAITING", b.getState());
        assertEquals("2024-01-01T10:00", b.getBookedAt());
    }

    @Test
    void isActiveWennWaiting() {
        Booking b = new Booking(1, 1, 1, "A", "B", "WAITING", "2024-01-01");
        assertTrue(b.isActive());
    }

    @Test
    void isActiveWennInTransit() {
        Booking b = new Booking(1, 1, 1, "A", "B", "IN_TRANSIT", "2024-01-01");
        assertTrue(b.isActive());
    }

    @Test
    void nichtActiveWennArrived() {
        Booking b = new Booking(1, 1, 1, "A", "B", "ARRIVED", "2024-01-01");
        assertFalse(b.isActive());
    }

    @Test
    void setStateAendertActiveStatus() {
        Booking b = new Booking(1, 1, 1, "A", "B", "WAITING", "2024-01-01");
        assertTrue(b.isActive());
        b.setState("ARRIVED");
        assertFalse(b.isActive());
    }
}
