package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import model.*;
import service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EasyRideApp extends Application {

    private Stage window;
    private static IBookingService bookingService;
    private static IFleetService fleetService;
    private static ITimeService timeService;
    private Passenger loggedInPassenger;
    private Timeline liveTimer;
    private static RideEventBus eventBus;

    // Style-Konstanten
    private static final String BG    = "-fx-background-color: #F8F9FA;";
    private static final String CARD  = "-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 24px;";
    private static final String BLK   = "-fx-background-color: #212529; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private static final String GRY   = "-fx-background-color: #E9ECEF; -fx-text-fill: #212529; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private static final String GRN   = "-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private static final String FIELD = "-fx-background-color: #F1F3F5; -fx-background-radius: 8px; -fx-padding: 10px; -fx-font-size: 13px;";
    private static final String ERR   = "-fx-text-fill: #DC3545; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String OKCLR = "-fx-text-fill: #28A745; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String BLUE  = "-fx-text-fill: #0D6EFD; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String GREEN = "-fx-text-fill: #28A745; -fx-font-size: 14px; -fx-font-weight: bold;";

    // ── SZENE 0: Start ──────────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        window = stage;
        if (bookingService == null) {
            IRouteService routeService = new RoutingService(Main.getDatabase());
            fleetService   = new FleetService(Main.getDatabase(), routeService);
            bookingService = new BookingService(routeService, fleetService);
            timeService    = new TimeService();
            eventBus       = new RideEventBus();
            restoreRoutesFromActiveBookings(routeService);
        }
        window.setTitle("EasyRide – Smart Mobility");
        showRoleSelectionScene();
    }

    static void openNewWindow() {
        EasyRideApp app = new EasyRideApp();
        Stage stage = new Stage();
        try {
            app.start(stage);
        } catch (Exception ignored) {}
    }

    // ── SZENE 1: Rollenauswahl ──────────────────────────────────────────────────

    private void showRoleSelectionScene() {
        stopTimer();
        VBox card = card();
        card.setAlignment(Pos.CENTER);
        card.getChildren().addAll(
            title("EasyRide"), sub("Smart Mobility Platform"), spacer(),
            btn("Ich bin Kunde",      BLK, e -> showCustomerAuthScene()),
            btn("Fahrer-Tablet",      GRY, e -> showDriverScene()),
            btn("Simulation starten", GRN, e -> showSimulationScene()),
            new Separator(),
            btn("Neues Fenster öffnen", GRY, e -> openNewWindow())
        );
        show(centered(card), 460, 500);
    }

    // ── SZENE 2: Kunden-Anmeldung ───────────────────────────────────────────────

    private void showCustomerAuthScene() {
        showCustomerAuthScene(null);
    }

    private void showCustomerAuthScene(String successMsg) {
        VBox card = card();
        TextField emailField = field("E-Mail Adresse");
        Label errLabel = errLabel();

        Label successLabel = new Label(successMsg != null ? successMsg : "");
        successLabel.setStyle(OKCLR);
        successLabel.setWrapText(true);
        successLabel.setVisible(successMsg != null);

        card.getChildren().addAll(
            title("Einloggen"),
            successLabel,
            emailField, errLabel,
            btn("Einloggen", BLK, e -> {
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    errLabel.setText("Bitte eine E-Mail-Adresse eingeben!");
                    return;
                }
                Passenger found = Main.getDatabase().getRegisteredPassengers().stream()
                        .filter(p -> p.getEmail().equalsIgnoreCase(email))
                        .findFirst()
                        .orElse(null);
                if (found == null) {
                    errLabel.setText("Diese E-Mail ist nicht registriert. Bitte registriere dich zuerst.");
                    return;
                }
                loggedInPassenger = found;
                showBookingScene();
            }),
            lbl("── oder ──"),
            btn("Neu registrieren", GRY, e -> showRegistrationScene()),
            back(e -> showRoleSelectionScene())
        );
        show(centered(card), 460, 420);
    }

    // ── SZENE 2.5: Registrierung ────────────────────────────────────────────────

    private void showRegistrationScene() {
        VBox card = card();
        TextField emailField = field("E-Mail Adresse");
        Label errLabel = errLabel();

        card.getChildren().addAll(
            title("Konto erstellen"), emailField, errLabel,
            btn("Registrieren", BLK, e -> {
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    errLabel.setText("Bitte eine E-Mail-Adresse eingeben!");
                    return;
                }
                boolean alreadyExists = Main.getDatabase().getRegisteredPassengers().stream()
                        .anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
                if (alreadyExists) {
                    errLabel.setText("Diese E-Mail-Adresse ist bereits registriert!");
                    return;
                }
                int newId = Main.getDatabase().getRegisteredPassengers().size() + 1;
                Main.getDatabase().addPassenger(new Passenger(newId, email));
                showCustomerAuthScene("Erfolgreich registriert! Bitte melde dich jetzt an.");
            }),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 360);
    }

    // ── SZENE 3: Buchung ────────────────────────────────────────────────────────

    private void showBookingScene() {
        stopTimer();
        VBox card = card();

        Label welcomeLabel = new Label("Hallo, " + loggedInPassenger.getName() + " 👋");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");

        card.getChildren().addAll(title("Wohin geht's?"), welcomeLabel);

        // Banner für aktive Fahrt
        Booking activeBooking = Main.getDatabase().getActiveBookingForPassenger(loggedInPassenger.getId());
        if (activeBooking != null) {
            VBox banner = new VBox(6);
            banner.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 10px; -fx-background-radius: 8px;");
            Label bannerLabel = new Label("Aktive Fahrt: "
                    + activeBooking.getPickupStationName() + " → " + activeBooking.getDropoffStationName());
            bannerLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 13px;");
            bannerLabel.setWrapText(true);
            Button viewBtn = btn("Fahrt anzeigen", GRN, e -> showRideStatusScene());
            viewBtn.setPrefHeight(32);
            banner.getChildren().addAll(bannerLabel, viewBtn);
            card.getChildren().add(banner);
        }

        // Depot nicht in der Auswahl anzeigen
        List<String> stationNames = Main.getDatabase().getStations().stream()
                .filter(s -> !s.isDepot())
                .map(Station::getName)
                .collect(Collectors.toList());

        ComboBox<String> startBox  = combo(stationNames, "Starthaltepunkt...");
        ComboBox<String> targetBox = combo(stationNames, "Zielhaltepunkt...");
        Label errLabel = errLabel();

        card.getChildren().addAll(
            new Separator(),
            lbl("Neue Buchung"),
            lbl("Starthaltepunkt"),  startBox,
            lbl("Zielhaltepunkt"),   targetBox,
            errLabel,
            btn("Jetzt buchen", BLK, e -> {
                if (Main.getDatabase().getActiveBookingForPassenger(loggedInPassenger.getId()) != null) {
                    errLabel.setText("Du hast bereits eine aktive Fahrt. Bitte schließe diese zuerst ab.");
                    return;
                }
                String startName  = startBox.getValue();
                String targetName = targetBox.getValue();

                if (startName == null || targetName == null) {
                    errLabel.setText("Bitte Start- und Zielhaltepunkt wählen!");
                    return;
                }
                if (startName.equals(targetName)) {
                    errLabel.setText("Start und Ziel dürfen nicht identisch sein!");
                    return;
                }

                Station start  = findStation(startName);
                Station target = findStation(targetName);
                if (start == null || target == null) {
                    errLabel.setText("Haltepunkt nicht gefunden.");
                    return;
                }

                errLabel.setText("");
                if (bookingService.bookRide(start, target, loggedInPassenger)) {
                    int bookingId = Main.getDatabase().nextBookingId();
                    Main.getDatabase().addBooking(new Booking(
                            bookingId,
                            loggedInPassenger.getId(),
                            loggedInPassenger.getAssignedVehicle().getId(),
                            start.getName(),
                            target.getName(),
                            PassengerState.WAITING.toString(),
                            LocalDateTime.now().withNano(0).toString()
                    ));
                    eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
                    showRideStatusScene();
                } else {
                    errLabel.setText("Buchung fehlgeschlagen – kein Fahrzeug verfügbar oder keine Route möglich.");
                }
            }),
            btn("Meine Fahrten", GRY, e -> showMyRidesScene()),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 580);
    }

    // ── SZENE 4: Live-Fahrstatus ────────────────────────────────────────────────

    private void showRideStatusScene() {
        stopTimer();
        VBox card = card();
        Passenger passenger = loggedInPassenger;
        Vehicle vehicle = passenger.getAssignedVehicle();

        Label vehicleLabel   = bold("Fahrzeug #" + (vehicle != null ? vehicle.getId() : "—"));
        Label pickupLabel    = lbl("Abholung: " + (passenger.getPickupStation()  != null ? passenger.getPickupStation().getName()  : "—"));
        Label dropoffLabel   = lbl("Ziel:     " + (passenger.getDropoffStation() != null ? passenger.getDropoffStation().getName() : "—"));
        Label waitLabel      = new Label("⏳ wird berechnet...");
        Label remainingLabel = new Label("🚗 wird berechnet...");
        Label stateLabel     = new Label("Status: " + passenger.getState());
        waitLabel.setStyle(BLUE);
        remainingLabel.setStyle(GREEN);
        stateLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");

        Runnable refresh = () -> {
            stateLabel.setText("Status: " + passenger.getState());
            if (passenger.getState() == PassengerState.ARRIVED) {
                waitLabel.setText("✅ Angekommen!");
                remainingLabel.setText("🏁 Fahrt abgeschlossen.");
                return;
            }
            int waitMinutes      = timeService.getWaitingTime(passenger);
            int remainingMinutes = timeService.getRemainingTime(passenger);
            waitLabel.setText(waitMinutes >= 0
                ? "⏳ Wartezeit bis Abholung: " + waitMinutes + " Min."
                : "✅ Fahrzeug bereits am Startpunkt");
            remainingLabel.setText(remainingMinutes >= 0
                ? "🚗 Restfahrzeit bis Ziel: " + remainingMinutes + " Min."
                : "—");
        };
        refresh.run();
        eventBus.subscribe(RideEventBus.Event.STOP_CONFIRMED, refresh);

        liveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> refresh.run()));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();

        card.getChildren().addAll(
            title("Meine Fahrt"), vehicleLabel, pickupLabel, dropoffLabel,
            new Separator(), waitLabel, remainingLabel, stateLabel,
            back(e -> {
                eventBus.unsubscribe(RideEventBus.Event.STOP_CONFIRMED, refresh);
                stopTimer();
                showBookingScene();
            })
        );
        show(centered(card), 460, 480);
    }

    // ── SZENE 4b: Meine Fahrten ─────────────────────────────────────────────────

    private void showMyRidesScene() {
        stopTimer();
        VBox card = card();
        card.setMaxWidth(420);
        card.getChildren().add(title("Meine Fahrten"));

        List<Booking> myBookings = Main.getDatabase().getBookingsForPassenger(loggedInPassenger.getId());

        if (myBookings.isEmpty()) {
            card.getChildren().add(lbl("Noch keine Fahrten gebucht."));
        } else {
            List<Booking> sorted = new ArrayList<>(myBookings);
            Collections.reverse(sorted);

            for (Booking booking : sorted) {
                String stateText = switch (booking.getState()) {
                    case "WAITING"    -> "Wartend";
                    case "IN_TRANSIT" -> "Unterwegs";
                    case "ARRIVED"    -> "Abgeschlossen";
                    default           -> booking.getState();
                };
                boolean active = booking.isActive();

                VBox entry = new VBox(4);
                entry.setStyle("-fx-background-color: " + (active ? "#E8F5E9" : "#F1F3F5")
                        + "; -fx-padding: 10px; -fx-background-radius: 8px;");

                Label routeLabel = new Label(booking.getPickupStationName()
                        + " → " + booking.getDropoffStationName());
                routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                routeLabel.setWrapText(true);

                String dateDisplay = (booking.getBookedAt() != null && booking.getBookedAt().length() >= 16)
                        ? booking.getBookedAt().substring(0, 16).replace('T', ' ')
                        : "—";
                Label infoLabel = new Label("Fahrzeug #" + booking.getVehicleId()
                        + "  |  " + stateText + "  |  " + dateDisplay);
                infoLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 12px;");
                infoLabel.setWrapText(true);

                entry.getChildren().addAll(routeLabel, infoLabel);

                if (active) {
                    Button viewBtn = btn("Fahrt anzeigen", GRN, e -> showRideStatusScene());
                    viewBtn.setPrefHeight(30);
                    entry.getChildren().add(viewBtn);
                }
                card.getChildren().add(entry);
            }
        }

        card.getChildren().add(back(e -> showBookingScene()));

        VBox scrollContent = new VBox(card);
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(BG);
        scrollContent.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        show(scroll, 460, 620);
    }

    // ── SZENE 5: Fahrer-Tablet ──────────────────────────────────────────────────

    private void showDriverScene() {
        stopTimer();
        VBox card = card();
        card.setMaxWidth(460);

        List<Vehicle> vehicles = Main.getDatabase().getVehicles();
        if (vehicles.isEmpty()) {
            card.getChildren().addAll(
                title("Fahrer-Tablet"),
                new Label("Keine Fahrzeuge vorhanden."),
                back(e -> showRoleSelectionScene())
            );
            show(centered(card), 460, 280);
            return;
        }

        ComboBox<String> vehicleBox = new ComboBox<>();
        vehicleBox.setStyle(FIELD);
        vehicleBox.setMaxWidth(Double.MAX_VALUE);
        vehicles.forEach(v ->
            vehicleBox.getItems().add("Fahrzeug #" + v.getId()
                    + "  (" + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Sitzpl.)"));
        vehicleBox.getSelectionModel().selectFirst();

        Label nextStopLabel = bold("—");
        Label pickupLabel   = new Label("—");
        Label dropoffLabel  = new Label("—");
        Label routeMapLabel = new Label("—");
        pickupLabel.setWrapText(true);
        dropoffLabel.setWrapText(true);
        routeMapLabel.setWrapText(true);
        routeMapLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #495057;");

        VBox infoPanel = new VBox(6);
        infoPanel.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 12px; -fx-background-radius: 8px;");
        infoPanel.getChildren().addAll(
            lbl("Nächster anzufahrender Halt:"), nextStopLabel,
            lbl("Einsteiger:"), pickupLabel,
            lbl("Aussteiger:"), dropoffLabel,
            new Separator(), lbl("Route:"), routeMapLabel
        );

        Label statusLabel = new Label("");

        Runnable redraw = () -> {
            int idx = vehicleBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= vehicles.size()) return;

            Vehicle vehicle = vehicles.get(idx);
            Route route = vehicle.getCurrentRoute();

            if (route == null || route.getStops().isEmpty()) {
                nextStopLabel.setText("Kein aktiver Auftrag");
                pickupLabel.setText("—");
                dropoffLabel.setText("—");
                routeMapLabel.setText("—");
                return;
            }

            RouteStop currentStop = route.getCurrentStop();
            if (currentStop == null) {
                nextStopLabel.setText("Route abgeschlossen.");
                pickupLabel.setText("—");
                dropoffLabel.setText("—");
                return;
            }

            nextStopLabel.setText(currentStop.getStation().getName());
            pickupLabel.setText(currentStop.getPassengersToPickUp().isEmpty() ? "Keine"
                : currentStop.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
            dropoffLabel.setText(currentStop.getPassengersToDropOff().isEmpty() ? "Keine"
                : currentStop.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(", ")));

            routeMapLabel.setText(buildRouteDisplay(route));

            vehicleBox.getItems().set(idx, "Fahrzeug #" + vehicle.getId()
                    + "  (" + vehicle.getPassengers().size() + "/" + vehicle.getMaxCapacity() + " Sitzpl.)");
        };

        vehicleBox.setOnAction(e -> redraw.run());
        redraw.run();

        // Aktualisiert alle Fahrzeugeinträge in der ComboBox (Fahrgastzahl).
        Runnable refreshLabels = () -> {
            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);
                vehicleBox.getItems().set(i, "Fahrzeug #" + v.getId()
                        + "  (" + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Sitzpl.)");
            }
        };

        // Sofortiges Neuzeichnen bei neuer Buchung (z. B. aus einem anderen Fenster).
        Runnable onBookingChanged = () -> { refreshLabels.run(); redraw.run(); };
        eventBus.subscribe(RideEventBus.Event.BOOKING_CHANGED, onBookingChanged);

        // Periodischer Hintergrund-Refresh als Fallback.
        liveTimer = new Timeline(new KeyFrame(Duration.seconds(10), ev -> redraw.run()));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();

        Button confirmBtn = btn("✓  Haltepunkt bestätigen", GRN, null);
        confirmBtn.setOnAction(e -> {
            int idx = vehicleBox.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Vehicle vehicle = vehicles.get(idx);

            if (vehicle.getCurrentRoute() == null) {
                statusLabel.setStyle(OKCLR);
                statusLabel.setText("Kein aktiver Auftrag – Route abgeschlossen.");
                return;
            }
            RouteStop currentStop = vehicle.getCurrentRoute().getCurrentStop();
            if (currentStop == null) {
                statusLabel.setStyle(OKCLR);
                statusLabel.setText("Alle Halte bereits bestätigt.");
                return;
            }

            String stationName = currentStop.getStation().getName();
            fleetService.confirmArrival(vehicle, currentStop);
            eventBus.publish(RideEventBus.Event.STOP_CONFIRMED);
            statusLabel.setStyle(OKCLR);
            statusLabel.setText("✓  " + stationName + " bestätigt.");
            redraw.run();
        });

        card.getChildren().addAll(
            title("Fahrer-Tablet"),
            lbl("Fahrzeug wählen:"), vehicleBox,
            infoPanel, confirmBtn, statusLabel,
            back(e -> {
                eventBus.unsubscribe(RideEventBus.Event.BOOKING_CHANGED, onBookingChanged);
                stopTimer();
                showRoleSelectionScene();
            })
        );

        VBox scrollContent = new VBox(card);
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(BG);
        scrollContent.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        show(scroll, 520, 700);
    }

    // ── SZENE 6: Simulation ─────────────────────────────────────────────────────

    private void showSimulationScene() {
        stopTimer();

        VBox root = new VBox(12);
        root.setStyle(BG);
        root.setPadding(new Insets(20));

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(220);
        logArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Label routeDisplay = new Label("—");
        routeDisplay.setWrapText(true);
        routeDisplay.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        VBox statusCard = new VBox(8);
        statusCard.setStyle("-fx-background-color: white; -fx-padding: 14px; -fx-background-radius: 10px;");
        statusCard.setEffect(createShadow());
        statusCard.getChildren().addAll(bold("Fahrzeug-Status"), routeDisplay);

        // Lambdas brauchen effectively-final-Werte – Arrays funktionieren als Wrapper
        Vehicle[] simVehicle = {null};
        boolean[] autoRunning = {false};

        Runnable drawRoute = () -> {
            if (simVehicle[0] == null || simVehicle[0].getCurrentRoute() == null) {
                routeDisplay.setText("—");
                return;
            }
            Vehicle v = simVehicle[0];
            StringBuilder sb = new StringBuilder();
            sb.append("Fahrzeug #").append(v.getId()).append("  |  Im Fahrzeug: ");
            if (v.getPassengers().isEmpty()) {
                sb.append("niemand");
            } else {
                sb.append(v.getPassengers().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
            }
            sb.append("\n\n");
            sb.append(buildRouteDisplay(v.getCurrentRoute()));
            routeDisplay.setText(sb.toString());
        };

        Button setupBtn = btn("1.  Demo-Buchungen erstellen",    BLK, null);
        Button stepBtn  = btn("2.  ▶  Einen Halt simulieren",   GRY, null);
        Button autoBtn  = btn("3.  ⚡  Auto-Simulation starten", GRN, null);
        stepBtn.setDisable(true);
        autoBtn.setDisable(true);

        setupBtn.setOnAction(e -> {
            stopTimer();
            autoRunning[0] = false;
            autoBtn.setText("3.  ⚡  Auto-Simulation starten");

            Vehicle vehicle = Main.getDatabase().getVehicles().get(0);
            simVehicle[0] = vehicle;

            new ArrayList<>(vehicle.getPassengers()).forEach(vehicle::removePassenger);
            vehicle.setCurrentRoute(null);

            Station alex      = findStation("Alexanderplatz");
            Station zoo       = findStation("Zoologischer Garten");
            Station kotti     = findStation("Kottbusser Tor");
            Station potsdamer = findStation("Potsdamer Platz");

            logArea.clear();
            logArea.appendText("=== Simulation gestartet ===\n\n");

            if (alex == null || zoo == null || kotti == null || potsdamer == null) {
                logArea.appendText("FEHLER: Stationen nicht gefunden!\n");
                return;
            }

            Passenger anna = new Passenger(91, "anna@sim.de");
            Passenger bob  = new Passenger(92, "bob@sim.de");

            logArea.appendText("Buchung 1: " + anna.getName() + "  "
                    + alex.getName() + " → " + zoo.getName() + "\n");
            boolean ok1 = bookingService.bookRide(alex, zoo, anna);
            logArea.appendText(ok1
                    ? "  OK: Fahrzeug #" + anna.getAssignedVehicle().getId() + "\n"
                    : "  FEHLER: Buchung fehlgeschlagen\n");

            logArea.appendText("\nBuchung 2: " + bob.getName() + "  "
                    + kotti.getName() + " → " + potsdamer.getName() + "\n");
            boolean ok2 = bookingService.bookRide(kotti, potsdamer, bob);
            logArea.appendText(ok2
                    ? "  OK: Fahrzeug #" + bob.getAssignedVehicle().getId() + "\n"
                    : "  FEHLER: Buchung fehlgeschlagen\n");

            if (ok1) {
                logArea.appendText("\nWartezeit anna: " + timeService.getWaitingTime(anna) + " Min.\n");
                logArea.appendText("Wartezeit bob:  " + timeService.getWaitingTime(bob)  + " Min.\n");
            }
            if (ok1 || ok2) eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
            logArea.appendText("\n");
            drawRoute.run();
            stepBtn.setDisable(false);
            autoBtn.setDisable(false);
        });

        stepBtn.setOnAction(e -> {
            if (simVehicle[0] == null || simVehicle[0].getCurrentRoute() == null) return;

            Route route = simVehicle[0].getCurrentRoute();
            RouteStop currentStop = route.getCurrentStop();
            if (currentStop == null) {
                logArea.appendText("Simulation beendet.\n");
                stepBtn.setDisable(true);
                autoBtn.setDisable(true);
                return;
            }

            logArea.appendText("Haltepunkt: " + currentStop.getStation().getName() + "\n");
            if (!currentStop.getPassengersToPickUp().isEmpty()) {
                logArea.appendText("  Eingestiegen: " + currentStop.getPassengersToPickUp().stream()
                        .map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");
            }
            if (!currentStop.getPassengersToDropOff().isEmpty()) {
                logArea.appendText("  Ausgestiegen: " + currentStop.getPassengersToDropOff().stream()
                        .map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");
            }

            fleetService.confirmArrival(simVehicle[0], currentStop);
            eventBus.publish(RideEventBus.Event.STOP_CONFIRMED);
            drawRoute.run();

            if (simVehicle[0].getCurrentRoute().getCurrentStop() == null) {
                logArea.appendText("\nFahrzeug zurück in der Zentrale.\n");
                stepBtn.setDisable(true);
                autoBtn.setDisable(true);
                stopTimer();
                autoRunning[0] = false;
                autoBtn.setText("3.  ⚡  Auto-Simulation starten");
            }
        });

        autoBtn.setOnAction(e -> {
            if (autoRunning[0]) {
                stopTimer();
                autoRunning[0] = false;
                autoBtn.setText("3.  ⚡  Auto-Simulation starten");
                return;
            }
            autoRunning[0] = true;
            autoBtn.setText("Stop Auto-Simulation");
            liveTimer = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> stepBtn.fire()));
            liveTimer.setCycleCount(Timeline.INDEFINITE);
            liveTimer.play();
        });

        HBox buttonRow = new HBox(8, setupBtn, stepBtn, autoBtn);
        HBox.setHgrow(setupBtn, Priority.ALWAYS);
        HBox.setHgrow(stepBtn,  Priority.ALWAYS);
        HBox.setHgrow(autoBtn,  Priority.ALWAYS);

        root.getChildren().addAll(
            title("Simulation"), buttonRow, statusCard,
            lbl("Log:"), logArea,
            back(ev -> { stopTimer(); showRoleSelectionScene(); })
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        show(scroll, 600, 720);
    }

    // ── Routen aus gespeicherten Buchungen wiederherstellen ─────────────────────

    /**
     * Rekonstruiert Fahrzeugrouten und Passagierlisten aus den beim Start geladenen
     * aktiven Buchungen. Wird einmalig nach dem Erzeugen der Services aufgerufen,
     * damit der Fahrer-Tablet auch nach einem Neustart die korrekte Route sieht.
     */
    private static void restoreRoutesFromActiveBookings(IRouteService routeService) {
        List<Booking> active = Main.getDatabase().getBookings().stream()
                .filter(Booking::isActive)
                .sorted(Comparator.comparingInt(Booking::getBookingId))
                .toList();

        for (Booking booking : active) {
            Passenger passenger = Main.getDatabase().getRegisteredPassengers().stream()
                    .filter(p -> p.getId() == booking.getPassengerId())
                    .findFirst().orElse(null);
            Vehicle vehicle = Main.getDatabase().getVehicles().stream()
                    .filter(v -> v.getId() == booking.getVehicleId())
                    .findFirst().orElse(null);
            Station pickup = Main.getDatabase().getStations().stream()
                    .filter(s -> s.getName().equals(booking.getPickupStationName()))
                    .findFirst().orElse(null);
            Station dropoff = Main.getDatabase().getStations().stream()
                    .filter(s -> s.getName().equals(booking.getDropoffStationName()))
                    .findFirst().orElse(null);

            if (passenger == null || vehicle == null || pickup == null || dropoff == null) continue;

            // Route neu berechnen – erste Buchung calcInitialRoute, weitere calcNewRoute
            Route route = vehicle.getCurrentRoute() == null
                    ? routeService.calcInitialRoute(vehicle, pickup, dropoff, passenger)
                    : routeService.calcNewRoute(vehicle.getCurrentRoute(), passenger, pickup, dropoff);

            if (route != null) {
                vehicle.setCurrentRoute(route);
            }

            // Passagier ins Fahrzeug eintragen (falls noch nicht vorhanden)
            if (!vehicle.getPassengers().contains(passenger)) {
                vehicle.addPassenger(passenger);       // setzt intern IN_TRANSIT
                try {
                    // Gespeicherten Zustand (WAITING / IN_TRANSIT) wiederherstellen
                    passenger.setState(PassengerState.valueOf(booking.getState()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // ── Hilfsmethoden ───────────────────────────────────────────────────────────

    private VBox centered(VBox card) {
        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle(BG);
        root.setPadding(new Insets(30));
        return root;
    }

    private void show(javafx.scene.Parent root, double defaultWidth, double defaultHeight) {
        boolean wasMaximized = window.isMaximized();
        Scene current = window.getScene();
        double w = (current != null) ? current.getWidth()  : defaultWidth;
        double h = (current != null) ? current.getHeight() : defaultHeight;
        window.setScene(new Scene(root, w, h));
        if (wasMaximized) window.setMaximized(true);
        window.show();
    }

    private VBox card() {
        VBox card = new VBox(10);
        card.setMaxWidth(400);
        card.setStyle(CARD);
        card.setEffect(createShadow());
        return card;
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        return label;
    }

    private Label bold(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        return label;
    }

    private Label sub(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");
        return label;
    }

    private Label lbl(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 12px;");
        return label;
    }

    private Label errLabel() {
        Label label = new Label("");
        label.setStyle(ERR);
        label.setWrapText(true);
        return label;
    }

    private Region spacer() {
        Region region = new Region();
        region.setPrefHeight(6);
        return region;
    }

    private TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(FIELD);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private ComboBox<String> combo(List<String> items, String prompt) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setPromptText(prompt);
        combo.setStyle(FIELD);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private Button btn(String text, String style,
                       javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle(style);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(38);
        if (handler != null) button.setOnAction(handler);
        return button;
    }

    private Button back(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button("← Zurück");
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        button.setOnAction(handler);
        return button;
    }

    private Station findStation(String name) {
        return Main.getDatabase().getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private void stopTimer() {
        if (liveTimer != null) {
            liveTimer.stop();
            liveTimer = null;
        }
    }

    private DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.08));
        shadow.setRadius(12);
        shadow.setOffsetY(4);
        return shadow;
    }

    private String buildRouteDisplay(Route route) {
        StringBuilder sb = new StringBuilder();
        List<RouteStop> stops = route.getStops();
        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            String marker = i < route.getCurrentStopIndex() ? "✓ "
                          : (i == route.getCurrentStopIndex() ? "▶ " : "  ");
            sb.append(marker).append(stop.getStation().getName());
            if (!stop.getPassengersToPickUp().isEmpty()) {
                sb.append("  ⬆").append(stop.getPassengersToPickUp().stream()
                        .map(Passenger::getName).collect(Collectors.joining(",")));
            }
            if (!stop.getPassengersToDropOff().isEmpty()) {
                sb.append("  ⬇").append(stop.getPassengersToDropOff().stream()
                        .map(Passenger::getName).collect(Collectors.joining(",")));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) { launch(args); }
}
