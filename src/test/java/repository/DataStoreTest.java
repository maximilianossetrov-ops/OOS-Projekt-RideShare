package repository;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests in-memory operations of DataStore.
 * File-writing methods (addPassenger, addDriver, addBooking) are covered
 * indirectly via the service-layer integration tests.
 */
class DataStoreTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        // Add in-memory objects so tests have something to work with
        dataStore.addVehicle(new Vehicle(1, 4));
        dataStore.addVehicle(new Vehicle(2, 4));
        dataStore.addStation(new Station("Alex",  false));
        dataStore.addStation(new Station("Depot", true));
        dataStore.addConnection(new Connection(
                new Station("Alex", false), new Station("Depot", true), 10));
    }

    // ── Vehicle claiming ───────────────────────────────────────────────────────

    @Test
    void isVehicleClaimed_initiallyFalse() {
        assertFalse(dataStore.isVehicleClaimed(1));
    }

    @Test
    void claimVehicle_unclaimed_returnsTrue() {
        assertTrue(dataStore.claimVehicle(1));
    }

    @Test
    void claimVehicle_alreadyClaimed_returnsFalse() {
        dataStore.claimVehicle(1);
        assertFalse(dataStore.claimVehicle(1));
    }

    @Test
    void releaseVehicle_makesVehicleClaimableAgain() {
        dataStore.claimVehicle(1);
        dataStore.releaseVehicle(1);
        assertFalse(dataStore.isVehicleClaimed(1));
        assertTrue(dataStore.claimVehicle(1)); // can claim again
    }

    @Test
    void claimAndRelease_differentVehiclesIndependent() {
        dataStore.claimVehicle(1);
        dataStore.releaseVehicle(2); // releasing an unclaimed vehicle is a no-op
        assertTrue(dataStore.isVehicleClaimed(1));
        assertFalse(dataStore.isVehicleClaimed(2));
    }

    // ── Collections ───────────────────────────────────────────────────────────

    @Test
    void addVehicle_increasesCount() {
        int before = dataStore.getVehicles().size();
        dataStore.addVehicle(new Vehicle(99, 2));
        assertEquals(before + 1, dataStore.getVehicles().size());
    }

    @Test
    void addStation_increasesCount() {
        int before = dataStore.getStations().size();
        dataStore.addStation(new Station("Neu", false));
        assertEquals(before + 1, dataStore.getStations().size());
    }

    @Test
    void addConnection_increasesCount() {
        int before = dataStore.getConnections().size();
        dataStore.addConnection(new Connection(
                new Station("A", false), new Station("B", false), 5));
        assertEquals(before + 1, dataStore.getConnections().size());
    }

    @Test
    void getVehicles_returnsUnmodifiableView() {
        assertThrows(UnsupportedOperationException.class,
                () -> dataStore.getVehicles().add(new Vehicle(50, 4)));
    }

    @Test
    void getStations_returnsUnmodifiableView() {
        assertThrows(UnsupportedOperationException.class,
                () -> dataStore.getStations().add(new Station("X", false)));
    }

    // ── Booking logic (in-memory, no file-write paths exercised) ─────────────

    @Test
    void nextBookingId_whenNoBookingsLoaded_returnsOne() {
        // bookings.json is [] so the list is empty → next ID = 1
        int id = dataStore.nextBookingId();
        assertTrue(id >= 1, "nextBookingId muss mindestens 1 sein");
    }

    @Test
    void getActiveBookingForPassenger_noMatch_returnsNull() {
        assertNull(dataStore.getActiveBookingForPassenger(99999));
    }

    @Test
    void getBookingsForPassenger_noMatch_returnsEmptyList() {
        assertTrue(dataStore.getBookingsForPassenger(99999).isEmpty());
    }
}
