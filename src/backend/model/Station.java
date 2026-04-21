package model;

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

}