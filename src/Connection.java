public class Connection {
    private Station station1;
    private Station station2;
    private int travelTimeMinutes;

    public Connection(Station station1, Station station2, int travelTimeMinutes) {
        this.station1 = station1;
        this.station2 = station2;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Station getStation1() { return station1; }
    public Station getStation2() { return station2; }
    public int getTravelTimeMinutes() { return travelTimeMinutes; }


    //Hilfsmethoden
    public boolean connects(Station station) {
        return station1.equals(station) || station2.equals(station);
    }

    public Station getDestinationFrom(Station currentStation) {
        if (currentStation.equals(station1)) {
            return station2;
        } else if (currentStation.equals(station2)) {
            return station1;
        }
        return null; // Wenn die Station gar nicht zu dieser Verbindung gehört
    }

    @Override
    public String toString() {
        // Nutzt die getName() Methode der Stationen (falls du diese schon in Station definiert hast)
        return "Verbindung: " + station1.getName() + " <--> " + station2.getName() +
                " (" + travelTimeMinutes + " Min.)";
    }
}