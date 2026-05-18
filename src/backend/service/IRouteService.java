package service;

import model.Passenger;
import model.Route;
import model.Station;
import model.Vehicle;

/**
 * Schnittstelle für den Routenberechnungsservice.
 * Abstrahiert den Dijkstra-Algorithmus hinter einem klaren Vertrag,
 * damit der BookingService nicht von der konkreten Implementierung abhängt.
 */
public interface IRouteService {

    /**
     * Berechnet eine neue Route für ein Fahrzeug ohne bestehende Route.
     * Die Route führt: Depot → Abholhaltestelle → Zielhaltestelle → Depot.
     */
    Route calcInitialRoute(Vehicle vehicle, Station start, Station target, Passenger passenger);

    /**
     * Fügt einen neuen Fahrgast optimal in eine bestehende Route ein.
     * Die neuen Halte werden so platziert, dass der Umweg möglichst gering ist.
     *
     * @return die aktualisierte Route, oder null wenn die Kapazität überschritten würde
     */
    Route calcNewRoute(Route currentRoute, Passenger passenger, Station pickupStation, Station dropoffStation);
}
