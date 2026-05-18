package service;

import model.Passenger;
import model.RouteStop;
import model.Vehicle;

/**
 * Schnittstelle für den Flottenservice.
 * Verwaltet die Fahrzeugauswahl und die Abarbeitung von Haltepunkten.
 */
public interface IFleetService {

    /**
     * Wählt ein geeignetes Fahrzeug für den Fahrgast aus.
     * Ein Fahrzeug ist geeignet, wenn noch freie Sitzplätze vorhanden sind.
     *
     * @return ein verfügbares Fahrzeug, oder null wenn die Flotte voll ist
     */
    Vehicle getVehicleForPassenger(Passenger passenger);

    /**
     * Bestätigt die Ankunft eines Fahrzeugs an einem Haltepunkt.
     * Fahrgäste steigen aus bzw. ein, und der Stoppindex der Route wird weitergesetzt.
     */
    void confirmArrival(Vehicle vehicle, RouteStop stop);
}
