public class Passenger {
    private int id;
    private String name;
    private PassengerState state;

    public Passenger(int id, String name) {
        this.id = id;
        this.name = name;
        this.state = PassengerState.WAITING;
    }

    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public PassengerState getState() {
        return this.state;
    }
    public void setState(PassengerState state) {this.state = state;}

    @Override
    public String toString() {
        return "Passenger[ID=" + id + ", Name=" + name + ", Status=" + state + "]";
    }
}