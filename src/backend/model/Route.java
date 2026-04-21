package model;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private int routeId;
    private Vehicle vehicle;
    private List<RouteStop> stops;
    private int currentStopIndex; // Um zu wissen, wo das Fahrzeug gerade ist

    public Route(int routeId, Vehicle vehicle) {
        this.routeId = routeId;
        this.vehicle = vehicle;
        this.stops = new ArrayList<>();
        this.currentStopIndex = 0;
    }

    public int getRouteId() { return routeId; }
    public Vehicle getVehicle() { return vehicle; }

    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) {
        this.stops = stops;
    }

    public int getCurrentStopIndex() { return currentStopIndex; }
    public void setCurrentStopIndex(int index) {
        this.currentStopIndex = index;
    }

    //Hilfsmethoden
    public RouteStop getNextStop() {
        if (currentStopIndex + 1 < stops.size()) {
            return stops.get(currentStopIndex + 1);
        }
        return null;
    }

    public RouteStop getCurrentStop() {
        if (currentStopIndex < stops.size()) {
            return stops.get(currentStopIndex);
        }
        return null;
    }
}