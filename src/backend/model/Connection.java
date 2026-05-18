package model;

/**
 * Repräsentiert eine bidirektionale Verbindung (Kante) zwischen zwei Haltestellen.
 * Die Reisezeit in Minuten bildet das Kantengewicht im Graphen des RoutingService.
 */
public class Connection {

    private final Station station1;
    private final Station station2;
    private final int travelTimeMinutes;

    public Connection(Station station1, Station station2, int travelTimeMinutes) {
        this.station1 = station1;
        this.station2 = station2;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Station getStation1() { return station1; }
    public Station getStation2() { return station2; }
    public int getTravelTimeMinutes() { return travelTimeMinutes; }

    /** Prüft, ob diese Verbindung die angegebene Haltestelle enthält. */
    public boolean connects(Station station) {
        return station1.equals(station) || station2.equals(station);
    }

    /**
     * Gibt die andere Haltestelle dieser Verbindung zurück.
     * Wird im Dijkstra-Algorithmus genutzt, um Nachbarn zu ermitteln.
     *
     * @return die gegenüberliegende Haltestelle, oder null falls die Station nicht Teil dieser Verbindung ist
     */
    public Station getDestinationFrom(Station current) {
        if (current.equals(station1)) return station2;
        if (current.equals(station2)) return station1;
        return null;
    }

    @Override
    public String toString() {
        return station1.getName() + " <--> " + station2.getName() + " (" + travelTimeMinutes + " Min.)";
    }
}
