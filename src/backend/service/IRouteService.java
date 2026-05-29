package service;

import model.Passenger;
import model.Route;
import model.Station;
import model.Vehicle;

public interface IRouteService {
    Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger);
    Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation);
    void recalcArrivalTimesFromCurrent(Route route);
}
