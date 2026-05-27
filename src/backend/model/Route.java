package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<RouteStop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    // Ersetzt die gesamte Halteliste – wird nach jeder Routenänderung aufgerufen.
    public void setStops(List<RouteStop> stops) {
        this.stops = new ArrayList<>(stops);
    }

    public void addStop(RouteStop stop) {
        this.stops.add(stop);
    }

    public RouteStop getCurrentStop() {
        if (currentStopIndex < stops.size()) return stops.get(currentStopIndex);
        return null;
    }

    public RouteStop getNextStop() {
        if (currentStopIndex + 1 < stops.size()) return stops.get(currentStopIndex + 1);
        return null;
    }
}
