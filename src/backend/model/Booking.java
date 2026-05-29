package model;

public class Booking {

    private final int bookingId;
    private final int passengerId;
    private final int vehicleId;
    private final String pickupStationName;
    private final String dropoffStationName;
    private String state;
    private final String bookedAt;

    public Booking(int bookingId, int passengerId, int vehicleId,
                   String pickupStationName, String dropoffStationName,
                   String state, String bookedAt) {
        this.bookingId         = bookingId;
        this.passengerId       = passengerId;
        this.vehicleId         = vehicleId;
        this.pickupStationName  = pickupStationName;
        this.dropoffStationName = dropoffStationName;
        this.state             = state;
        this.bookedAt          = bookedAt;
    }

    public int    getBookingId()           { return bookingId; }
    public int    getPassengerId()         { return passengerId; }
    public int    getVehicleId()           { return vehicleId; }
    public String getPickupStationName()   { return pickupStationName; }
    public String getDropoffStationName()  { return dropoffStationName; }
    public String getState()              { return state; }
    public void   setState(String state)  { this.state = state; }
    public String getBookedAt()           { return bookedAt; }

    public boolean isActive() {
        return !PassengerState.ARRIVED.toString().equals(state);
    }
}
