package service;

import model.Passenger;

public interface ITimeService {
    // Minutes until the vehicle reaches the passenger's pickup stop (before boarding)
    int getWaitingTime(Passenger passenger);

    // Minutes until the vehicle reaches the passenger's dropoff stop (after boarding)
    int getRemainingTime(Passenger passenger);
}
