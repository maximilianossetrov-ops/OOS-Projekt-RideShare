package model;

import java.util.Objects;

public class Station {

    private final String name;
    private final boolean depot;

    public Station(String name, boolean depot) {
        this.name = name;
        this.depot = depot;
    }

    public String getName() { return name; }
    public boolean isDepot() { return depot; }

    // Gleichheit basiert auf dem Namen, damit Dijkstra Stationen sauber vergleichen kann.
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
