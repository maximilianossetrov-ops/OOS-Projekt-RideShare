package service;

import model.Passenger;
import model.Station;

/**
 * Schnittstelle für den Buchungsservice.
 * Definiert den Vertrag für das Buchen einer Fahrt.
 * Durch die Verwendung des Interfaces statt der konkreten Klasse
 * bleibt die UI unabhängig von der Implementierung (Dependency Inversion).
 */
public interface IBookingService {

    /**
     * Bucht eine Fahrt für den angegebenen Fahrgast.
     *
     * @param start     gewünschte Abholhaltestelle
     * @param target    gewünschte Zielhaltestelle
     * @param passenger der buchende Fahrgast
     * @return true bei erfolgreicher Buchung, false wenn kein Fahrzeug oder keine Route verfügbar
     */
    boolean bookRide(Station start, Station target, Passenger passenger);
}
