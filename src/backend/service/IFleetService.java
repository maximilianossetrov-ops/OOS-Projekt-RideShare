package service;

import model.Passenger;
import model.RouteStop;
import model.Vehicle;

public interface IFleetService {
    Vehicle getVehicleForPassenger(Passenger passenger);
    void confirmArrival(Vehicle vehicle, RouteStop stop);
}
