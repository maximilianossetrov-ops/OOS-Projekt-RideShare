package model;

public class Passenger {
    private int id;
    private String name;
    private String email;
    private PassengerState state;

    // Tracking fields set by BookingService after a ride is booked
    private Vehicle assignedVehicle;
    private Station pickupStation;
    private Station dropoffStation;

    public Passenger(int id, String email) {
        this.id = id;
        this.email = email;
        // Derive a display name from the email prefix
        this.name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        this.state = PassengerState.WAITING;
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public PassengerState getState() { return this.state; }
    public void setState(PassengerState state) { this.state = state; }

    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public void setAssignedVehicle(Vehicle vehicle) { this.assignedVehicle = vehicle; }

    public Station getPickupStation() { return pickupStation; }
    public void setPickupStation(Station station) { this.pickupStation = station; }

    public Station getDropoffStation() { return dropoffStation; }
    public void setDropoffStation(Station station) { this.dropoffStation = station; }

    @Override
    public String toString() {
        return "Passenger[name=" + name + ", email=" + email + ", status=" + state + "]";
    }
}
