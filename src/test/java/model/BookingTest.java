package model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import service.*;



class BookingTest {

    @Test
    void isActive_returnsFalse_whenStateIsArrived() {
        Booking booking = new Booking(
                1,
                100,
                10,
                "Hauptbahnhof",
                "Universität",
                PassengerState.ARRIVED.toString(),
                "2025-01-01T10:00:00"
        );

        assertFalse(booking.isActive());
    }

    @Test
    void isActive_returnsTrue_whenStateIsWaiting() {
        Booking booking = new Booking(
                1,
                100,
                10,
                "Hauptbahnhof",
                "Universität",
                PassengerState.WAITING.toString(),
                "2025-01-01T10:00:00"
        );

        assertTrue(booking.isActive());
    }

    @Test
    void isActive_returnsTrue_whenStateIsInTransit() {
        Booking booking = new Booking(
                1,
                100,
                10,
                "Hauptbahnhof",
                "Universität",
                PassengerState.IN_TRANSIT.toString(),
                "2025-01-01T10:00:00"
        );

        assertTrue(booking.isActive());
    }
}