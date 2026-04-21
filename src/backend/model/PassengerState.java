package model;

public enum PassengerState {
    WAITING,      // Passagier hat gebucht und wartet am Startpunkt
    IN_TRANSIT,   // Passagier wurde aufgenommen und befindet sich im Fahrzeug
    ARRIVED       // Passagier wurde am Ziel abgesetzt
}