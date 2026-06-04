package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {

    private final Station berlin  = new Station("Berlin", false);
    private final Station hamburg = new Station("Hamburg", false);
    private final Connection conn = new Connection(berlin, hamburg, 90);

    @Test
    void connectsStation1() {
        assertTrue(conn.connects(berlin));
    }

    @Test
    void connectsStation2() {
        assertTrue(conn.connects(hamburg));
    }

    @Test
    void connectsNichtUnbeteiligte() {
        assertFalse(conn.connects(new Station("München", false)));
    }

    @Test
    void zielVonStation1IstStation2() {
        assertEquals(hamburg, conn.getDestinationFrom(berlin));
    }

    @Test
    void zielVonStation2IstStation1() {
        assertEquals(berlin, conn.getDestinationFrom(hamburg));
    }

    @Test
    void zielVonFremderStationIstNull() {
        assertNull(conn.getDestinationFrom(new Station("München", false)));
    }

    @Test
    void fahrtzeitKorrekt() {
        assertEquals(90, conn.getTravelTimeMinutes());
    }
}
