package model;

public class Passenger {

    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private PassengerState state;

    // Werden nach erfolgreicher Buchung vom BookingService gesetzt
    private Vehicle assignedVehicle;
    private Station pickupStation;
    private Station dropoffStation;

    public Passenger(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password != null ? password : "";
        this.state = PassengerState.WAITING;
    }

    // Für Simulation und interne Nutzung (Name wird aus E-Mail abgeleitet)
    public Passenger(int id, String email) {
        this(id, email.contains("@") ? email.substring(0, email.indexOf('@')) : email, email, "");
    }

    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    public PassengerState getState()         { return state; }
    public void setState(PassengerState state) { this.state = state; }

    public Vehicle getAssignedVehicle()              { return assignedVehicle; }
    public void setAssignedVehicle(Vehicle vehicle)  { this.assignedVehicle = vehicle; }

    public Station getPickupStation()                 { return pickupStation; }
    public void setPickupStation(Station station)     { this.pickupStation = station; }

    public Station getDropoffStation()                { return dropoffStation; }
    public void setDropoffStation(Station station)    { this.dropoffStation = station; }

    @Override
    public String toString() {
        return "Fahrgast[name=" + name + ", email=" + email + ", zustand=" + state + "]";
    }
}
