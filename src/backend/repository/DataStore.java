package repository;

import model.Booking;
import model.Connection;
import model.Driver;
import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataStore {

    private final List<Passenger>  registeredPassengers;
    private final List<Driver>     registeredDrivers;
    private final List<Vehicle>    vehicles;
    private final List<Station>    stations;
    private final List<Connection> connections;
    private final List<Booking>    bookings;

    // Fahrzeuge, die gerade von einem eingeloggten Fahrer belegt sind (nur im RAM).
    private final Set<Integer> claimedVehicleIds = new HashSet<>();

    public DataStore() {
        this.registeredPassengers = new ArrayList<>(PassengerRepository.load());
        this.registeredDrivers    = new ArrayList<>(DriverRepository.load());
        this.vehicles    = new ArrayList<>();
        this.stations    = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.bookings    = new ArrayList<>(BookingRepository.load());
    }

    // ── Passengers ──────────────────────────────────────────────────────────────

    public List<Passenger> getRegisteredPassengers() {
        return Collections.unmodifiableList(registeredPassengers);
    }

    public void addPassenger(Passenger passenger) {
        registeredPassengers.add(passenger);
        PassengerRepository.save(registeredPassengers);
    }

    // ── Drivers ─────────────────────────────────────────────────────────────────

    public List<Driver> getRegisteredDrivers() {
        return Collections.unmodifiableList(registeredDrivers);
    }

    public void addDriver(Driver driver) {
        registeredDrivers.add(driver);
        DriverRepository.save(registeredDrivers);
    }

    // ── Vehicle claiming (session-only, nicht persistiert) ──────────────────────

    /**
     * Reserviert ein Fahrzeug für die aktuelle Fahrer-Session.
     * Gibt true zurück, wenn die Reservierung erfolgreich war,
     * false wenn das Fahrzeug bereits belegt ist.
     */
    public boolean claimVehicle(int vehicleId) {
        return claimedVehicleIds.add(vehicleId);
    }

    /** Gibt ein Fahrzeug nach Ende der Schicht wieder frei. */
    public void releaseVehicle(int vehicleId) {
        claimedVehicleIds.remove(vehicleId);
    }

    public boolean isVehicleClaimed(int vehicleId) {
        return claimedVehicleIds.contains(vehicleId);
    }

    // ── Vehicles / Stations / Connections ────────────────────────────────────────

    public List<Vehicle>    getVehicles()    { return Collections.unmodifiableList(vehicles); }
    public List<Station>    getStations()    { return Collections.unmodifiableList(stations); }
    public List<Connection> getConnections() { return Collections.unmodifiableList(connections); }

    public void addVehicle(Vehicle vehicle)          { vehicles.add(vehicle); }
    public void addStation(Station station)          { stations.add(station); }
    public void addConnection(Connection connection) { connections.add(connection); }

    // ── Bookings ─────────────────────────────────────────────────────────────────

    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public List<Booking> getBookingsForPassenger(int passengerId) {
        return bookings.stream()
                .filter(b -> b.getPassengerId() == passengerId)
                .collect(Collectors.toList());
    }

    public Booking getActiveBookingForPassenger(int passengerId) {
        return bookings.stream()
                .filter(b -> b.getPassengerId() == passengerId && b.isActive())
                .findFirst()
                .orElse(null);
    }

    public int nextBookingId() {
        return bookings.stream().mapToInt(Booking::getBookingId).max().orElse(0) + 1;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        BookingRepository.save(bookings);
    }

    public void updateBookingState(int bookingId, String newState) {
        for (Booking b : bookings) {
            if (b.getBookingId() == bookingId) {
                b.setState(newState);
                BookingRepository.save(bookings);
                return;
            }
        }
    }

    public void setActiveBookingState(int passengerId, String newState) {
        Booking active = getActiveBookingForPassenger(passengerId);
        if (active != null) {
            updateBookingState(active.getBookingId(), newState);
        }
    }

    public void restoreActiveBookings() {
        for (Booking booking : bookings) {
            if (!booking.isActive()) continue;

            Passenger passenger = registeredPassengers.stream()
                    .filter(p -> p.getId() == booking.getPassengerId())
                    .findFirst().orElse(null);
            Station pickup = stations.stream()
                    .filter(s -> s.getName().equals(booking.getPickupStationName()))
                    .findFirst().orElse(null);
            Station dropoff = stations.stream()
                    .filter(s -> s.getName().equals(booking.getDropoffStationName()))
                    .findFirst().orElse(null);
            Vehicle vehicle = vehicles.stream()
                    .filter(v -> v.getId() == booking.getVehicleId())
                    .findFirst().orElse(null);

            if (passenger == null || pickup == null || dropoff == null || vehicle == null) continue;

            passenger.setPickupStation(pickup);
            passenger.setDropoffStation(dropoff);
            passenger.setAssignedVehicle(vehicle);
            try {
                passenger.setState(PassengerState.valueOf(booking.getState()));
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
