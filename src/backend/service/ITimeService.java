package service;

import model.Passenger;

public interface ITimeService {
    int getWaitingTime(Passenger passenger);
    int getRemainingTime(Passenger passenger);
}
