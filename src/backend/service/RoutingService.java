package service;

import model.*;
import repository.DataStore;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementierung des IRouteService mit Dijkstra-Algorithmus.
 *
 * Für eine Erstbuchung wird eine vollständige Route berechnet:
 *   Depot → Abholhaltestelle → Zielhaltestelle → Depot
 *
 * Bei einer Folgebuchung werden die neuen Halte so in die bestehende Route
 * eingefügt, dass der zusätzliche Umweg minimal bleibt (Greedy-Heuristik).
 */
public class RoutingService implements IRouteService {

    private final DataStore dataStore;

    // Statischer Zähler für eindeutige Routen-IDs
    private static int routeIdCounter = 0;

    public RoutingService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // -------------------------------------------------------------------------
    // Öffentliche Methoden (Interface-Implementierung)
    // -------------------------------------------------------------------------

    /**
     * Erstellt eine komplett neue Route für ein Fahrzeug ohne aktive Route.
     * Pfad: Depot → ... → Abholpunkt → ... → Zielpunkt → ... → Depot
     */
    @Override
    public Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger) {
        List<Connection> connections = dataStore.getConnections();
        Station depot = findDepot();
        if (depot == null) return null;

        // Drei Teilstrecken mit Dijkstra berechnen
        List<Station> depotToStart  = findShortestPath(depot,  start,  connections);
        List<Station> startToTarget = findShortestPath(start,  target, connections);
        List<Station> targetToDepot = findShortestPath(target, depot,  connections);

        if (depotToStart == null || startToTarget == null || targetToDepot == null) return null;

        // Teilpfade zusammenführen – doppelte Grenzstationen überspringen
        List<Station> fullPath = new ArrayList<>(depotToStart);
        fullPath.addAll(startToTarget.subList(1, startToTarget.size()));
        fullPath.addAll(targetToDepot.subList(1, targetToDepot.size()));

        // Route aus dem Pfad aufbauen und Ankunftszeiten berechnen
        Route route = new Route(++routeIdCounter, vehicle);
        LocalDateTime time = LocalDateTime.now();

        for (int i = 0; i < fullPath.size(); i++) {
            Station station = fullPath.get(i);
            RouteStop stop = new RouteStop(station);

            if (station.equals(start))  stop.addPassengerToPickUp(passenger);
            if (station.equals(target)) stop.addPassengerToDropOff(passenger);

            if (i > 0) {
                // Fahrzeit von der vorherigen zur aktuellen Station addieren
                time = time.plusMinutes(shortestTravelTime(fullPath.get(i - 1), station, connections));
            }
            stop.setPlannedArrivalTime(time);
            route.addStop(stop);
        }
        return route;
    }

    /**
     * Fügt einen neuen Fahrgast in eine bestehende Route ein.
     * Einstieg- und Ausstiegshalt werden an der Position eingefügt,
     * die den Gesamtumweg am geringsten erhöht.
     * Gibt null zurück, wenn die Fahrzeugkapazität überschritten würde.
     */
    @Override
    public Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation) {
        List<Connection> connections = dataStore.getConnections();

        // Arbeitskopie der Halteliste erstellen, damit die ursprüngliche Route unverändert bleibt
        List<RouteStop> stops = new ArrayList<>(currentRoute.getStops());
        int fromIndex = currentRoute.getCurrentStopIndex();

        // Depothalt am Ende nicht verschieben – neue Halte kommen immer davor
        boolean endsAtDepot = !stops.isEmpty() && stops.get(stops.size() - 1).getStation().isDepot();
        int insertLimit = endsAtDepot ? stops.size() - 1 : stops.size();

        // Optimale Einfügeposition für den Abholhalt suchen
        int pickupPos = findBestInsertPosition(stops, fromIndex + 1, insertLimit, pickupStation, connections);
        RouteStop pickupStop = new RouteStop(pickupStation);
        pickupStop.addPassengerToPickUp(passenger);
        stops.add(pickupPos, pickupStop);

        // Jetzt Abholhalt eingefügt → neues Limit für den Abgabehalt berechnen
        int dropoffLimit = endsAtDepot ? stops.size() - 1 : stops.size();
        int dropoffPos = findBestInsertPosition(stops, pickupPos + 1, dropoffLimit, dropoffStation, connections);
        RouteStop dropoffStop = new RouteStop(dropoffStation);
        dropoffStop.addPassengerToDropOff(passenger);
        stops.add(dropoffPos, dropoffStop);

        // Kapazitätsprüfung: Besetzung durch alle zukünftigen Halte simulieren
        Vehicle vehicle = currentRoute.getVehicle();
        int occupancy = vehicle.getPassengers().size();
        for (int i = fromIndex; i < stops.size(); i++) {
            occupancy += stops.get(i).getPassengersToPickUp().size();
            if (occupancy > vehicle.getMaxCapacity()) return null;
            occupancy -= stops.get(i).getPassengersToDropOff().size();
        }

        // Ankunftszeiten neu berechnen und Route aktualisieren
        recalcArrivalTimes(stops, fromIndex, connections);
        currentRoute.setStops(stops);
        return currentRoute;
    }

    // -------------------------------------------------------------------------
    // Private Hilfsmethoden
    // -------------------------------------------------------------------------

    /** Sucht das Depot im Streckennetz. */
    private Station findDepot() {
        return dataStore.getStations().stream()
                .filter(Station::isDepot)
                .findFirst()
                .orElse(null);
    }

    /**
     * Sucht die optimale Einfügeposition für eine neue Station zwischen
     * startIdx und maxPos. Bewertet jede mögliche Position anhand des Umwegs.
     */
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
                // Einfügen am Ende: nur Anfahrtskosten berücksichtigen
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

    /**
     * Berechnet die geplanten Ankunftszeiten aller Halte ab fromIdx neu.
     * Nötig nach jeder Änderung an der Halteliste.
     */
    private void recalcArrivalTimes(List<RouteStop> stops, int fromIdx, List<Connection> connections) {
        if (stops.isEmpty() || fromIdx >= stops.size()) return;

        LocalDateTime time = LocalDateTime.now();
        stops.get(fromIdx).setPlannedArrivalTime(time);

        for (int i = fromIdx + 1; i < stops.size(); i++) {
            int travelTime = shortestTravelTime(stops.get(i - 1).getStation(), stops.get(i).getStation(), connections);
            // Fallback: 60 Minuten, wenn keine Verbindung gefunden (sollte bei korrektem Netz nicht vorkommen)
            time = time.plusMinutes(travelTime == Integer.MAX_VALUE ? 60 : travelTime);
            stops.get(i).setPlannedArrivalTime(time);
        }
    }

    /**
     * Führt den Dijkstra-Algorithmus aus und gibt den kürzesten Pfad
     * als geordnete Stationsliste zurück. Gibt null zurück, wenn kein Pfad existiert.
     */
    private List<Station> findShortestPath(Station start, Station end, List<Connection> connections) {
        DijkstraResult result = runDijkstra(start, connections);

        if (result.distances().getOrDefault(end, Integer.MAX_VALUE) == Integer.MAX_VALUE) return null;

        // Pfad rückwärts rekonstruieren
        List<Station> path = new ArrayList<>();
        for (Station s = end; s != null; s = result.predecessors().get(s)) {
            path.add(0, s);
        }
        return path;
    }

    /**
     * Gibt die kürzeste Fahrzeit in Minuten zwischen zwei Stationen zurück.
     * Nutzt den gleichen Dijkstra-Kern wie findShortestPath, um Code-Duplizierung zu vermeiden.
     */
    private int shortestTravelTime(Station from, Station to, List<Connection> connections) {
        if (from.equals(to)) return 0;
        return runDijkstra(from, connections).distances().getOrDefault(to, Integer.MAX_VALUE);
    }

    /**
     * Kernimplementierung des Dijkstra-Algorithmus.
     * Berechnet von der Startstation aus die kürzesten Wege zu allen erreichbaren Stationen.
     *
     * @return Distanz- und Vorgänger-Maps, aus denen Pfade rekonstruiert werden können
     */
    private DijkstraResult runDijkstra(Station start, List<Connection> connections) {
        Map<Station, Integer> distances = new HashMap<>();
        Map<Station, Station> predecessors = new HashMap<>();

        // PriorityQueue sortiert Stationen nach ihrer aktuell bekannten Distanz
        PriorityQueue<Station> queue = new PriorityQueue<>(
                Comparator.comparingInt(s -> distances.getOrDefault(s, Integer.MAX_VALUE)));

        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Station current = queue.poll();
            int currentDist = distances.getOrDefault(current, Integer.MAX_VALUE);
            if (currentDist == Integer.MAX_VALUE) break; // Restliche Knoten nicht erreichbar

            for (Connection connection : connections) {
                if (!connection.connects(current)) continue;

                Station neighbor = connection.getDestinationFrom(current);
                int newDist = currentDist + connection.getTravelTimeMinutes();

                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, current);
                    // Priorität neu setzen: alten Eintrag entfernen, aktualisierten einfügen
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return new DijkstraResult(distances, predecessors);
    }

    /**
     * Hilfsobjekt, das das Ergebnis einer Dijkstra-Ausführung kapselt.
     * Enthält die Distanzen von der Startstation sowie die Vorgänger-Map
     * zur Pfadrekonstruktion.
     */
    private record DijkstraResult(Map<Station, Integer> distances, Map<Station, Station> predecessors) {}
}
