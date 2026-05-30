package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StationTest {

    @Test
    void equals_sameName_areEqual() {
        Station a = new Station("Alexanderplatz", false);
        Station b = new Station("Alexanderplatz", true);
        assertEquals(a, b);
    }

    @Test
    void equals_differentName_notEqual() {
        Station a = new Station("Alex", false);
        Station b = new Station("Zoo",  false);
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        Station s = new Station("Alex", false);
        assertNotEquals(s, null);
    }

    @Test
    void equals_differentType_returnsFalse() {
        Station s = new Station("Alex", false);
        assertNotEquals(s, "Alex");
    }

    @Test
    void hashCode_sameName_equal() {
        Station a = new Station("Museum", false);
        Station b = new Station("Museum", true);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void isDepot_depot_returnsTrue() {
        assertTrue(new Station("Depot", true).isDepot());
    }

    @Test
    void isDepot_regular_returnsFalse() {
        assertFalse(new Station("Alex", false).isDepot());
    }

    @Test
    void toString_returnsName() {
        assertEquals("Alexanderplatz", new Station("Alexanderplatz", false).toString());
    }
}
