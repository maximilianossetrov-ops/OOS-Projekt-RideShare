import java.util.List;

public class Vehicle {
    private int id;
    private int maxCapacity;
    private List<Passenger> passengerList = new List<Passenger>();

    Vehicle(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
    }

    public int getId() {
        return this.id;
    }
    public int getMaxCapacity() {
        return this.maxCapacity;
    }
    public List<Passenger> getPassengerList() {
        return passengerList;
    }

    public boolean addPassenger(Passenger passenger) {
        if(passengerList.contains(passenger.getId())) {
            return false;
        }
        passengerList.add(passenger.getId());
        return true;
    }
    public boolean removePassenger(Passenger passenger) {
        if(!passengerList.contains(passenger) {
            return false;
        }
        passengerList.remove(passenger);
        return true;
    }


}