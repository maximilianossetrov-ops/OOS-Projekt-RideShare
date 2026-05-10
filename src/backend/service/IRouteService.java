package service;

import model.Passenger;
import model.Route;
import model.RouteStop;
import model.Station;
import model.Vehicle;

public interface IRouteService {
    Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation);
    Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger);
}
