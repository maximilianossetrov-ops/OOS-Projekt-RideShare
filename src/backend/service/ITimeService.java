package service;

import model.Passenger;

/**
 * Schnittstelle für Zeitberechnungen während einer Fahrt.
 * Ermöglicht es der UI, Echtzeit-Informationen zur Wartezeit anzuzeigen.
 */
public interface ITimeService {

    /**
     * Berechnet die verbleibenden Minuten bis zur Abholung des Fahrgastes.
     *
     * @return Minuten bis zur Abholung, oder -1 wenn der Halt bereits passiert wurde
     */
    int getWaitingTime(Passenger passenger);

    /**
     * Berechnet die verbleibende Fahrzeit bis zum Ziel des Fahrgastes.
     *
     * @return Minuten bis zur Ankunft, oder -1 wenn der Halt bereits passiert wurde
     */
    int getRemainingTime(Passenger passenger);
}
