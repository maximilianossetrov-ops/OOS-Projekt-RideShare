package model;

public class Passenger {

    private final int id;
    private final String name;
    private final String email;
    private PassengerState state;

    // Werden nach erfolgreicher Buchung vom BookingService gesetzt
    private Vehicle assignedVehicle;
    private Station pickupStation;
    private Station dropoffStation;

    public Passenger(int id, String email) {
        this.id = id;
        this.email = email;
        this.name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        this.state = PassengerState.WAITING;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public PassengerState getState() { return state; }
    public void setState(PassengerState state) { this.state = state; }

    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public void setAssignedVehicle(Vehicle vehicle) { this.assignedVehicle = vehicle; }

    public Station getPickupStation() { return pickupStation; }
    public void setPickupStation(Station station) { this.pickupStation = station; }

    public Station getDropoffStation() { return dropoffStation; }
    public void setDropoffStation(Station station) { this.dropoffStation = station; }

    @Override
    public String toString() {
        return "Fahrgast[name=" + name + ", email=" + email + ", zustand=" + state + "]";
    }
}
