package service;
import model.*;

public interface IRouteService {
    Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation);
    Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger);
}
