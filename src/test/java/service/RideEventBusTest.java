package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RideEventBusTest {

    private RideEventBus bus;

    @BeforeEach
    void setUp() {
        bus = new RideEventBus();
    }

    @Test
    void subscribe_andPublish_callsListener() {
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(RideEventBus.Event.BOOKING_CHANGED, count::incrementAndGet);
        bus.publish(RideEventBus.Event.BOOKING_CHANGED);
        assertEquals(1, count.get());
    }

    @Test
    void publish_noListeners_noException() {
        assertDoesNotThrow(() -> bus.publish(RideEventBus.Event.BOOKING_CHANGED));
    }

    @Test
    void unsubscribe_listenerNotCalled() {
        AtomicInteger count = new AtomicInteger(0);
        Runnable listener = count::incrementAndGet;
        bus.subscribe(RideEventBus.Event.BOOKING_CHANGED, listener);
        bus.unsubscribe(RideEventBus.Event.BOOKING_CHANGED, listener);
        bus.publish(RideEventBus.Event.BOOKING_CHANGED);
        assertEquals(0, count.get());
    }

    @Test
    void multipleListeners_allCalled() {
        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);
        bus.subscribe(RideEventBus.Event.STOP_CONFIRMED, a::incrementAndGet);
        bus.subscribe(RideEventBus.Event.STOP_CONFIRMED, b::incrementAndGet);
        bus.publish(RideEventBus.Event.STOP_CONFIRMED);
        assertEquals(1, a.get());
        assertEquals(1, b.get());
    }

    @Test
    void differentEvents_areIndependent() {
        AtomicInteger booking = new AtomicInteger(0);
        AtomicInteger stop    = new AtomicInteger(0);
        bus.subscribe(RideEventBus.Event.BOOKING_CHANGED, booking::incrementAndGet);
        bus.subscribe(RideEventBus.Event.STOP_CONFIRMED,  stop::incrementAndGet);

        bus.publish(RideEventBus.Event.BOOKING_CHANGED);

        assertEquals(1, booking.get());
        assertEquals(0, stop.get());
    }

    @Test
    void publishTwice_listenerCalledTwice() {
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(RideEventBus.Event.BOOKING_CHANGED, count::incrementAndGet);
        bus.publish(RideEventBus.Event.BOOKING_CHANGED);
        bus.publish(RideEventBus.Event.BOOKING_CHANGED);
        assertEquals(2, count.get());
    }
}
