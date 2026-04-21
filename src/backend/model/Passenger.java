package model;

public class Passenger {
    private int id;
    private String name;
    private String email;
    private PassengerState state;

    public Passenger(int id, String email) {
        this.id = id;
        this.email = email;
        this.state = PassengerState.WAITING;
    }

    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public String getEmail() {
        return this.name;
    }
    public PassengerState getState() {
        return this.state;
    }
    public void setState(PassengerState state) {this.state = state;}

    @Override
    public String toString() {
        return "Passenger[Email=" + email + ", Name=" + name + ", Status=" + state + "]";
    }
}