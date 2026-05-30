package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PassengerTest {

    @Test
    void constructor4Arg_setsAllFields() {
        Passenger p = new Passenger(1, "Max", "max@test.com", "hash123");
        assertEquals(1,            p.getId());
        assertEquals("Max",        p.getName());
        assertEquals("max@test.com", p.getEmail());
        assertEquals("hash123",    p.getPassword());
    }

    @Test
    void constructor2Arg_derivesNameFromEmail() {
        Passenger p = new Passenger(2, "anna@example.com");
        assertEquals("anna", p.getName());
        assertEquals("anna@example.com", p.getEmail());
        assertEquals("", p.getPassword());
    }

    @Test
    void constructor2Arg_emailWithoutAt_usesFullEmailAsName() {
        Passenger p = new Passenger(3, "noatsign");
        assertEquals("noatsign", p.getName());
        assertEquals("noatsign", p.getEmail());
    }

    @Test
    void constructor_nullPassword_storedAsEmpty() {
        Passenger p = new Passenger(4, "Lea", "lea@test.com", null);
        assertEquals("", p.getPassword());
    }

    @Test
    void initialState_isWaiting() {
        Passenger p = new Passenger(1, "test@test.com");
        assertEquals(PassengerState.WAITING, p.getState());
    }

    @Test
    void setState_updatesState() {
        Passenger p = new Passenger(1, "test@test.com");
        p.setState(PassengerState.IN_TRANSIT);
        assertEquals(PassengerState.IN_TRANSIT, p.getState());

        p.setState(PassengerState.ARRIVED);
        assertEquals(PassengerState.ARRIVED, p.getState());
    }

    @Test
    void setAssignedVehicle_andGet() {
        Passenger p = new Passenger(1, "test@test.com");
        Vehicle v = new Vehicle(1, 4);
        p.setAssignedVehicle(v);
        assertSame(v, p.getAssignedVehicle());
    }

    @Test
    void setPickupAndDropoffStation_andGet() {
        Passenger p = new Passenger(1, "test@test.com");
        Station pickup  = new Station("Alex",  false);
        Station dropoff = new Station("Depot", true);
        p.setPickupStation(pickup);
        p.setDropoffStation(dropoff);
        assertSame(pickup,  p.getPickupStation());
        assertSame(dropoff, p.getDropoffStation());
    }
}
