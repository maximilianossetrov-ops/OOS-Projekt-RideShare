package service;

import model.*;
import repository.DataStore;

import java.time.LocalDateTime;
import java.util.*;

public class RoutingService implements IRouteService {

    private final DataStore dataStore;
    private static int routeIdCounter = 0;

    public RoutingService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger) {
        List<Connection> connections = dataStore.getConnections();
        Station depot = findDepot();
        if (depot == null) return null;

        List<Station> depotToStart  = findShortestPath(depot,  start,  connections);
        List<Station> startToTarget = findShortestPath(start,  target, connections);
        List<Station> targetToDepot = findShortestPath(target, depot,  connections);

        if (depotToStart == null || startToTarget == null || targetToDepot == null) return null;

        // Grenzstationen beim Zusammenführen der Teilpfade überspringen
        List<Station> fullPath = new ArrayList<>(depotToStart);
        fullPath.addAll(startToTarget.subList(1, startToTarget.size()));
        fullPath.addAll(targetToDepot.subList(1, targetToDepot.size()));

        Route route = new Route(++routeIdCounter, vehicle);
        LocalDateTime time = LocalDateTime.now();

        for (int i = 0; i < fullPath.size(); i++) {
            Station station = fullPath.get(i);
            RouteStop stop = new RouteStop(station);

            if (station.equals(start))  stop.addPassengerToPickUp(passenger);
            if (station.equals(target)) stop.addPassengerToDropOff(passenger);

            if (i > 0) {
                time = time.plusMinutes(shortestTravelTime(fullPath.get(i - 1), station, connections));
            }
            stop.setPlannedArrivalTime(time);
            route.addStop(stop);
        }
        return route;
    }

    @Override
    public void recalcArrivalTimesFromCurrent(Route route) {
        recalcArrivalTimes(route.getStops(), route.getCurrentStopIndex(), dataStore.getConnections());
    }

    @Override
    public Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation) {
        List<Connection> connections = dataStore.getConnections();

        // Arbeitskopie, damit wir die laufende Route nicht direkt anfassen
        List<RouteStop> stops = new ArrayList<>(currentRoute.getStops());
        int fromIndex = currentRoute.getCurrentStopIndex();

        // Depot bleibt immer am Ende – neue Halte kommen davor
        boolean endsAtDepot = !stops.isEmpty() && stops.get(stops.size() - 1).getStation().isDepot();
        int insertLimit = endsAtDepot ? stops.size() - 1 : stops.size();

        int pickupPos = findBestInsertPosition(stops, fromIndex + 1, insertLimit, pickupStation, connections);
        RouteStop pickupStop = new RouteStop(pickupStation);
        pickupStop.addPassengerToPickUp(passenger);
        stops.add(pickupPos, pickupStop);

        int dropoffLimit = endsAtDepot ? stops.size() - 1 : stops.size();
        int dropoffPos = findBestInsertPosition(stops, pickupPos + 1, dropoffLimit, dropoffStation, connections);
        RouteStop dropoffStop = new RouteStop(dropoffStation);
        dropoffStop.addPassengerToDropOff(passenger);
        stops.add(dropoffPos, dropoffStop);

        // Kapazität prüfen: Belegung durch alle künftigen Halte simulieren
        Vehicle vehicle = currentRoute.getVehicle();
        int occupancy = vehicle.getPassengers().size();
        for (int i = fromIndex; i < stops.size(); i++) {
            occupancy += stops.get(i).getPassengersToPickUp().size();
            if (occupancy > vehicle.getMaxCapacity()) return null;
            occupancy -= stops.get(i).getPassengersToDropOff().size();
        }

        recalcArrivalTimes(stops, fromIndex, connections);
        currentRoute.setStops(stops);
        return currentRoute;
    }

    private Station findDepot() {
        return dataStore.getStations().stream()
                .filter(Station::isDepot)
                .findFirst()
                .orElse(null);
    }

    private int findBestInsertPosition(List<RouteStop> stops, int startIdx, int maxPos,
                                       Station station, List<Connection> connections) {
        int safeStart = Math.max(1, startIdx);
        if (stops.isEmpty() || safeStart > maxPos) return maxPos;

        int bestPos = maxPos;
        int minDetour = Integer.MAX_VALUE;

        for (int i = safeStart; i <= maxPos; i++) {
            Station from = stops.get(i - 1).getStation();
            int detour;

            if (i == stops.size()) {
                // Einfügen am Ende: kein "to"-Knoten vorhanden, nur Anfahrtskosten
                detour = shortestTravelTime(from, station, connections);
            } else {
                // Umweg = (from→new) + (new→to) − (from→to)
                Station to = stops.get(i).getStation();
                detour = shortestTravelTime(from, station, connections)
                       + shortestTravelTime(station, to, connections)
                       - shortestTravelTime(from, to, connections);
            }

            if (detour < minDetour) {
                minDetour = detour;
                bestPos = i;
            }
        }
        return bestPos;
    }

    private void recalcArrivalTimes(List<RouteStop> stops, int fromIdx, List<Connection> connections) {
        if (stops.isEmpty() || fromIdx >= stops.size()) return;

        LocalDateTime time = LocalDateTime.now();
        stops.get(fromIdx).setPlannedArrivalTime(time);

        for (int i = fromIdx + 1; i < stops.size(); i++) {
            int travelTime = shortestTravelTime(stops.get(i - 1).getStation(), stops.get(i).getStation(), connections);
            // Fallback – sollte bei einem zusammenhängenden Netz nie auftreten
            time = time.plusMinutes(travelTime == Integer.MAX_VALUE ? 60 : travelTime);
            stops.get(i).setPlannedArrivalTime(time);
        }
    }

    private List<Station> findShortestPath(Station start, Station end, List<Connection> connections) {
        DijkstraResult result = runDijkstra(start, connections);
        if (result.distances().getOrDefault(end, Integer.MAX_VALUE) == Integer.MAX_VALUE) return null;

        // Pfad rückwärts über Vorgänger rekonstruieren
        List<Station> path = new ArrayList<>();
        for (Station s = end; s != null; s = result.predecessors().get(s)) {
            path.add(0, s);
        }
        return path;
    }

    private int shortestTravelTime(Station from, Station to, List<Connection> connections) {
        if (from.equals(to)) return 0;
        return runDijkstra(from, connections).distances().getOrDefault(to, Integer.MAX_VALUE);
    }

    private DijkstraResult runDijkstra(Station start, List<Connection> connections) {
        Map<Station, Integer> distances = new HashMap<>();
        Map<Station, Station> predecessors = new HashMap<>();

        PriorityQueue<Station> queue = new PriorityQueue<>(
                Comparator.comparingInt(s -> distances.getOrDefault(s, Integer.MAX_VALUE)));

        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Station current = queue.poll();
            int currentDist = distances.getOrDefault(current, Integer.MAX_VALUE);
            if (currentDist == Integer.MAX_VALUE) break; // restliche Knoten nicht erreichbar

            for (Connection connection : connections) {
                if (!connection.connects(current)) continue;

                Station neighbor = connection.getDestinationFrom(current);
                int newDist = currentDist + connection.getTravelTimeMinutes();

                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, current);
                    // PriorityQueue kennt kein decrease-key – alten Knoten rauswerfen und neu einsetzen
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return new DijkstraResult(distances, predecessors);
    }

    private record DijkstraResult(Map<Station, Integer> distances, Map<Station, Station> predecessors) {}
}
