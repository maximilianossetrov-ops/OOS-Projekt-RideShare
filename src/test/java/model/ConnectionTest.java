package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {

    private Station alex, zoo, museum;
    private Connection alexToZoo;

    @BeforeEach
    void setUp() {
        alex    = new Station("Alexanderplatz",      false);
        zoo     = new Station("Zoologischer Garten", false);
        museum  = new Station("Museumsinsel",         false);
        alexToZoo = new Connection(alex, zoo, 12);
    }

    @Test
    void connects_station1_returnsTrue() {
        assertTrue(alexToZoo.connects(alex));
    }

    @Test
    void connects_station2_returnsTrue() {
        assertTrue(alexToZoo.connects(zoo));
    }

    @Test
    void connects_unrelatedStation_returnsFalse() {
        assertFalse(alexToZoo.connects(museum));
    }

    @Test
    void getDestinationFrom_station1_returnsStation2() {
        assertSame(zoo, alexToZoo.getDestinationFrom(alex));
    }

    @Test
    void getDestinationFrom_station2_returnsStation1() {
        assertSame(alex, alexToZoo.getDestinationFrom(zoo));
    }

    @Test
    void getDestinationFrom_unrelated_returnsNull() {
        assertNull(alexToZoo.getDestinationFrom(museum));
    }

    @Test
    void getTravelTimeMinutes_returnsCorrectValue() {
        assertEquals(12, alexToZoo.getTravelTimeMinutes());
    }

    @Test
    void getters_returnBothStations() {
        assertSame(alex, alexToZoo.getStation1());
        assertSame(zoo,  alexToZoo.getStation2());
    }
}
