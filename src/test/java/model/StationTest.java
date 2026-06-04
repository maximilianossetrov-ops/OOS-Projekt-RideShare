package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StationTest {

    @Test
    void equalsNachName() {
        Station a = new Station("Alexanderplatz", false);
        Station b = new Station("Alexanderplatz", true);
        assertEquals(a, b, "Zwei Stationen mit gleichem Namen müssen gleich sein");
    }

    @Test
    void nichtGleichBeiVerschiedenemNamen() {
        Station a = new Station("Alexanderplatz", false);
        Station b = new Station("Hauptbahnhof", false);
        assertNotEquals(a, b);
    }

    @Test
    void hashCodeKonsistentMitEquals() {
        Station a = new Station("Mitte", false);
        Station b = new Station("Mitte", true);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void depotFlagKorrekt() {
        assertTrue(new Station("Depot", true).isDepot());
        assertFalse(new Station("Bahnhof", false).isDepot());
    }

    @Test
    void toStringGibtNamenZurueck() {
        assertEquals("Potsdamer Platz", new Station("Potsdamer Platz", false).toString());
    }
}
