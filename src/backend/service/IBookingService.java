package service;

import model.Passenger;
import model.Station;

public interface IBookingService {
    boolean bookRide(Station start, Station target, Passenger passenger);
}
