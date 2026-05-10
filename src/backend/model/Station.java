package model;

import java.util.Objects;

public class Station {
    private String name;
    private Boolean isDepot;

    public Station(String name, Boolean isDepot) {
        this.name = name;
        this.isDepot = isDepot;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsDepot() {
        return isDepot;
    }

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
}