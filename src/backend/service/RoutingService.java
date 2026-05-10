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

    // Builds: depot → ... → start (pickup) → ... → target (dropoff) → ... → depot
    @Override
    public Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger) {
        List<Connection> connections = dataStore.getConnections();
        Station depot = findDepot();
        if (depot == null) return null;

        List<Station> toStart  = dijkstra(depot,  start,  connections);
        List<Station> toTarget = dijkstra(start,  target, connections);
        List<Station> toDepot  = dijkstra(target, depot,  connections);

        if (toStart == null || toTarget == null || toDepot == null) return null;

        // Merge paths, skipping duplicate boundary stations
        List<Station> fullPath = new ArrayList<>(toStart);
        fullPath.addAll(toTarget.subList(1, toTarget.size()));
        fullPath.addAll(toDepot.subList(1, toDepot.size()));

        Route route = new Route(++routeIdCounter, vehicle);
        LocalDateTime time = LocalDateTime.now();

        for (int i = 0; i < fullPath.size(); i++) {
            Station station = fullPath.get(i);
            RouteStop stop = new RouteStop(station);

            if (station.equals(start))  stop.getPassengersToPickUp().add(passenger);
            if (station.equals(target)) stop.getPassengersToDropOff().add(passenger);

            if (i > 0) {
                time = time.plusMinutes(directConnectionTime(fullPath.get(i - 1), station, connections));
            }
            stop.setPlannedArrivalTime(time);
            route.getStops().add(stop);
        }
        return route;
    }

    // Inserts pickup and dropoff stops at optimal positions, always before the final depot stop.
    @Override
    public Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation) {
        List<Connection> connections = dataStore.getConnections();
        List<RouteStop> stops = new ArrayList<>(currentRoute.getStops());
        int fromIdx = currentRoute.getCurrentStopIndex();

        // Keep the depot stop at the end — insert all new stops before it
        boolean endsAtDepot = !stops.isEmpty()
                && Boolean.TRUE.equals(stops.get(stops.size() - 1).getStation().getIsDepot());

        int pickupMax  = endsAtDepot ? stops.size() - 1 : stops.size();
        int pickupPos  = findBestInsertPosition(stops, fromIdx + 1, pickupMax, pickupStation, connections);
        RouteStop pickupStop = new RouteStop(pickupStation);
        pickupStop.getPassengersToPickUp().add(passenger);
        stops.add(pickupPos, pickupStop);

        // After the pickup insert the depot (if any) is now at stops.size()-1
        int dropoffMax = endsAtDepot ? stops.size() - 1 : stops.size();
        int dropoffPos = findBestInsertPosition(stops, pickupPos + 1, dropoffMax, dropoffStation, connections);
        RouteStop dropoffStop = new RouteStop(dropoffStation);
        dropoffStop.getPassengersToDropOff().add(passenger);
        stops.add(dropoffPos, dropoffStop);

        // Capacity check: simulate occupancy through all future stops
        Vehicle vehicle = currentRoute.getVehicle();
        int occupancy = vehicle.getPassengers().size();
        for (int i = fromIdx; i < stops.size(); i++) {
            occupancy += stops.get(i).getPassengersToPickUp().size();
            if (occupancy > vehicle.getMaxCapacity()) return null; // would overflow
            occupancy -= stops.get(i).getPassengersToDropOff().size();
        }

        recalcArrivalTimes(stops, fromIdx, connections);
        currentRoute.setStops(stops);
        return currentRoute;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Station findDepot() {
        return dataStore.getStations().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsDepot()))
                .findFirst().orElse(null);
    }

    // Finds the insertion index (between stops[i-1] and stops[i]) that adds the
    // least extra travel time. startIdx: first position to consider; maxPos: last.
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
                // Appending at the very end (no next stop)
                detour = shortestTime(from, station, connections);
            } else {
                Station to = stops.get(i).getStation();
                int direct = shortestTime(from, to, connections);
                detour = shortestTime(from, station, connections)
                       + shortestTime(station, to, connections)
                       - direct;
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
        LocalDateTime time = LocalDateTime.now(); // recalculate relative to now
        stops.get(fromIdx).setPlannedArrivalTime(time);

        for (int i = fromIdx + 1; i < stops.size(); i++) {
            int t = shortestTime(stops.get(i - 1).getStation(), stops.get(i).getStation(), connections);
            time = time.plusMinutes(t == Integer.MAX_VALUE ? 60 : t);
            stops.get(i).setPlannedArrivalTime(time);
        }
    }

    // Returns the full station list for the shortest path from start to end.
    private List<Station> dijkstra(Station start, Station end, List<Connection> connections) {
        Map<Station, Integer> dist = new HashMap<>();
        Map<Station, Station> prev = new HashMap<>();
        PriorityQueue<Station> pq = new PriorityQueue<>(
                Comparator.comparingInt(s -> dist.getOrDefault(s, Integer.MAX_VALUE)));

        dist.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Station curr = pq.poll();
            if (curr.equals(end)) break;
            int currDist = dist.getOrDefault(curr, Integer.MAX_VALUE);
            if (currDist == Integer.MAX_VALUE) break;

            for (Connection c : connections) {
                if (c.connects(curr)) {
                    Station neighbor = c.getDestinationFrom(curr);
                    int newDist = currDist + c.getTravelTimeMinutes();
                    if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        dist.put(neighbor, newDist);
                        prev.put(neighbor, curr);
                        pq.remove(neighbor);
                        pq.add(neighbor);
                    }
                }
            }
        }

        if (!dist.containsKey(end) || dist.get(end) == Integer.MAX_VALUE) return null;

        List<Station> path = new ArrayList<>();
        for (Station s = end; s != null; s = prev.get(s)) {
            path.add(0, s);
        }
        return path;
    }

    // Returns the shortest travel time in minutes between any two stations.
    private int shortestTime(Station from, Station to, List<Connection> connections) {
        if (from.equals(to)) return 0;
        Map<Station, Integer> dist = new HashMap<>();
        PriorityQueue<Station> pq = new PriorityQueue<>(
                Comparator.comparingInt(s -> dist.getOrDefault(s, Integer.MAX_VALUE)));
        dist.put(from, 0);
        pq.add(from);

        while (!pq.isEmpty()) {
            Station curr = pq.poll();
            if (curr.equals(to)) return dist.get(to);
            int currDist = dist.getOrDefault(curr, Integer.MAX_VALUE);
            if (currDist == Integer.MAX_VALUE) break;

            for (Connection c : connections) {
                if (c.connects(curr)) {
                    Station neighbor = c.getDestinationFrom(curr);
                    int newDist = currDist + c.getTravelTimeMinutes();
                    if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        dist.put(neighbor, newDist);
                        pq.remove(neighbor);
                        pq.add(neighbor);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    // Returns the direct edge time between two adjacent stops in a Dijkstra path.
    private int directConnectionTime(Station from, Station to, List<Connection> connections) {
        for (Connection c : connections) {
            if (c.connects(from)) {
                Station dest = c.getDestinationFrom(from);
                if (dest != null && dest.equals(to)) return c.getTravelTimeMinutes();
            }
        }
        return 0;
    }
}
