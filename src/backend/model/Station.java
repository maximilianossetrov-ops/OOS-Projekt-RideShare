package model;

import java.util.Objects;

/**
 * Repräsentiert eine Haltestelle im Streckennetz.
 * Haltestellen dienen als Knoten im Graphen, über den der RoutingService
 * die kürzesten Wege berechnet. Das Depot ist eine besondere Haltestelle,
 * von der aus alle Fahrzeuge starten und zu der sie zurückkehren.
 */
public class Station {

    private final String name;
    private final boolean depot;

    /**
     * Erstellt eine neue Haltestelle.
     *
     * @param name  Anzeigename der Haltestelle
     * @param depot true, wenn es sich um das Depot (Zentrale) handelt
     */
    public Station(String name, boolean depot) {
        this.name = name;
        this.depot = depot;
    }

    public String getName() { return name; }

    /** Gibt zurück, ob diese Haltestelle das Depot ist. */
    public boolean isDepot() { return depot; }

    /**
     * Zwei Haltestellen sind identisch, wenn ihr Name übereinstimmt.
     * Das ermöglicht den Vergleich im Graphen-Algorithmus.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        return Objects.equals(name, ((Station) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
