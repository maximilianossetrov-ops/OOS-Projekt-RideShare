import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    private int id;
    private int maxCapacity;
    private List<Passenger> passengerList = new ArrayList<>();

    public Vehicle(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
    }

    public int getId() { return this.id; }
    public int getMaxCapacity() { return this.maxCapacity; }
    public List<Passenger> getPassengerList() { return passengerList; }

    public boolean addPassenger(Passenger passenger) {
        if (passengerList.size() >= maxCapacity) {
            return false;
        }
        if (passengerList.contains(passenger)) {
            return false;
        }

        passengerList.add(passenger);
        passenger.setState(PassengerState.IN_TRANSIT);
        return true;
    }

    public boolean removePassenger(Passenger passenger) {
        if (!passengerList.contains(passenger)) {
            return false;
        }
        passengerList.remove(passenger);
        passenger.setState(PassengerState.ARRIVED);
        return true;
    }
}