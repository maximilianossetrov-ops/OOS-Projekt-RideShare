package model;

/**
 * Mögliche Zustände eines Fahrgastes während einer Fahrt.
 * Wird vom Vehicle gesetzt, sobald ein Fahrgast ein- oder aussteigt.
 */
public enum PassengerState {
    WAITING,     // Fahrgast hat gebucht und wartet an der Abholhaltestelle
    IN_TRANSIT,  // Fahrgast ist eingestiegen und befindet sich im Fahrzeug
    ARRIVED      // Fahrgast wurde an der Zielhaltestelle abgesetzt
}
