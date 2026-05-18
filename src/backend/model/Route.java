package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repräsentiert die geplante Route eines Fahrzeugs als geordnete Folge von Halten.
 * Der currentStopIndex zeigt, welcher Halt als nächstes angefahren wird.
 * Die Route beginnt immer am Depot und endet dort wieder.
 */
public class Route {

    private final int routeId;
    private final Vehicle vehicle;
    private List<RouteStop> stops;
    private int currentStopIndex;

    public Route(int routeId, Vehicle vehicle) {
        this.routeId = routeId;
        this.vehicle = vehicle;
        this.stops = new ArrayList<>();
        this.currentStopIndex = 0;
    }

    public int getRouteId() { return routeId; }
    public Vehicle getVehicle() { return vehicle; }

    public int getCurrentStopIndex() { return currentStopIndex; }
    public void setCurrentStopIndex(int index) { this.currentStopIndex = index; }

    /** Gibt eine unveränderliche Sicht auf alle Halte der Route zurück. */
    public List<RouteStop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    /**
     * Ersetzt die komplette Halteliste – wird vom RoutingService beim Einplanen
     * neuer Fahrgäste genutzt, nachdem eine Kopie angepasst wurde.
     */
    public void setStops(List<RouteStop> stops) {
        this.stops = new ArrayList<>(stops);
    }

    /** Fügt einen neuen Halt ans Ende der Routenliste an. */
    public void addStop(RouteStop stop) {
        this.stops.add(stop);
    }

    /** Gibt den Halt zurück, der als nächstes angefahren wird. */
    public RouteStop getCurrentStop() {
        if (currentStopIndex < stops.size()) {
            return stops.get(currentStopIndex);
        }
        return null;
    }

    /** Gibt den übernächsten Halt zurück, oder null wenn keiner mehr folgt. */
    public RouteStop getNextStop() {
        if (currentStopIndex + 1 < stops.size()) {
            return stops.get(currentStopIndex + 1);
        }
        return null;
    }
}
