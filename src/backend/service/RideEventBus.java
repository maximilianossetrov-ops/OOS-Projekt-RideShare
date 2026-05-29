package service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Zentraler Event-Bus für Echtzeit-Synchronisation zwischen Kunden- und Fahrer-Ansicht.
 * Alle Listener werden auf dem JavaFX Application Thread ausgeführt (da publish() immer
 * von einem JavaFX-Handler oder -Timer aufgerufen wird).
 */
public class RideEventBus {

    public enum Event {
        /** Eine neue Buchung wurde erstellt oder ein Buchungsstatus hat sich geändert. */
        BOOKING_CHANGED,
        /** Der Fahrer hat einen Haltepunkt bestätigt. */
        STOP_CONFIRMED
    }

    private final Map<Event, List<Runnable>> listeners = new EnumMap<>(Event.class);

    public void subscribe(Event event, Runnable listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(Event event, Runnable listener) {
        List<Runnable> list = listeners.get(event);
        if (list != null) list.remove(listener);
    }

    /** Benachrichtigt alle Listener für das gegebene Event. Kopiert die Liste zuerst,
     *  damit ein Listener sich während des Aufrufs sicher abmelden kann. */
    public void publish(Event event) {
        List<Runnable> list = listeners.get(event);
        if (list == null || list.isEmpty()) return;
        new ArrayList<>(list).forEach(Runnable::run);
    }
}
