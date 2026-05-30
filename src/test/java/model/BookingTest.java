package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private Booking booking(String state) {
        return new Booking(1, 1, 1, "Alex", "Zoo", state, "2026-01-01T10:00");
    }

    @Test
    void isActive_waiting_returnsTrue() {
        assertTrue(booking("WAITING").isActive());
    }

    @Test
    void isActive_inTransit_returnsTrue() {
        assertTrue(booking("IN_TRANSIT").isActive());
    }

    @Test
    void isActive_arrived_returnsFalse() {
        assertFalse(booking("ARRIVED").isActive());
    }

    @Test
    void setState_changesState() {
        Booking b = booking("WAITING");
        b.setState("ARRIVED");
        assertEquals("ARRIVED", b.getState());
        assertFalse(b.isActive());
    }

    @Test
    void getters_returnAllConstructorValues() {
        Booking b = new Booking(42, 7, 3, "Depot", "Museum", "IN_TRANSIT", "2026-05-30T09:00");
        assertEquals(42,           b.getBookingId());
        assertEquals(7,            b.getPassengerId());
        assertEquals(3,            b.getVehicleId());
        assertEquals("Depot",      b.getPickupStationName());
        assertEquals("Museum",     b.getDropoffStationName());
        assertEquals("IN_TRANSIT", b.getState());
        assertEquals("2026-05-30T09:00", b.getBookedAt());
    }
}
