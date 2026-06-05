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
import repository.PasswordUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EasyRideApp extends Application {

    private Stage window;
    private static IBookingService bookingService;
    private static IFleetService   fleetService;
    private static ITimeService    timeService;
    private Passenger loggedInPassenger;
    private Driver    loggedInDriver;
    private int       loggedInDriverVehicleId;
    private Timeline  liveTimer;
    private static RideEventBus eventBus;

    // ── Design-System ────────────────────────────────────────────────────────────
    private static final String BG      = "-fx-background-color: #EEF2FF;";
    private static final String CARD    = "-fx-background-color: white; -fx-background-radius: 16px; -fx-padding: 28px;";
    private static final String BLK     = "-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand;";
    private static final String GRY     = "-fx-background-color: #F1F5F9; -fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand;";
    private static final String GRN     = "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand;";
    private static final String FIELD   = "-fx-background-color: #F9FAFB; -fx-background-radius: 8px; -fx-padding: 10px 12px; -fx-font-size: 13px; -fx-border-color: #D1D5DB; -fx-border-radius: 8px; -fx-border-width: 1;";
    private static final String ERR     = "-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String OKCLR   = "-fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold;";
    private static final String BLUE    = "-fx-text-fill: #4F46E5; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String GREEN   = "-fx-text-fill: #059669; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String HERO    = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #4F46E5, #7C3AED); -fx-background-radius: 12px;";

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
        window.setOnCloseRequest(event -> {
            if (loggedInDriver != null && loggedInDriverVehicleId != 0) {
                Vehicle own = Main.getDatabase().getVehicles().stream()
                        .filter(v -> v.getId() == loggedInDriverVehicleId)
                        .findFirst().orElse(null);
                if (own != null && own.getCurrentRoute() != null
                        && own.getCurrentRoute().getCurrentStop() != null) {
                    event.consume();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Schicht aktiv");
                    alert.setHeaderText("Fenster kann nicht geschlossen werden");
                    alert.setContentText(
                            "Du hast noch eine aktive Fahrt.\n"
                            + "Bitte bestätige erst alle Haltestellen,\n"
                            + "bevor du das Fenster schließt.");
                    alert.showAndWait();
                }
            }
        });
        showRoleSelectionScene();
    }

    static void openNewWindow() {
        EasyRideApp app = new EasyRideApp();
        Stage stage = new Stage();
        try { app.start(stage); } catch (Exception ignored) {}
    }

    // ── SZENE 1: Rollenauswahl ──────────────────────────────────────────────────

    private void showRoleSelectionScene() {
        stopTimer();

        VBox card = card();
        card.setSpacing(0);
        card.setPadding(Insets.EMPTY);

        VBox hero = heroHeader("🚗", "EasyRide", "Smart Mobility Platform");

        VBox buttons = new VBox(10);
        buttons.setPadding(new Insets(22, 28, 28, 28));
        buttons.getChildren().addAll(
            btn("👤  Ich bin Kunde",       BLK, e -> showCustomerAuthScene()),
            btn("🚘  Fahrer-Tablet",       GRY, e -> showDriverAuthScene()),
            btn("⚡  Simulation starten",  GRN, e -> showSimulationScene()),
            separatorLine(),
            btn("🖥  Neues Fenster öffnen", GRY, e -> openNewWindow())
        );

        card.getChildren().addAll(hero, buttons);
        show(centered(card), 460, 520);
    }

    // ── SZENE 2: Kunden-Anmeldung ───────────────────────────────────────────────

    private void showCustomerAuthScene() { showCustomerAuthScene(null); }

    private void showCustomerAuthScene(String successMsg) {
        VBox card = card();
        TextField     emailField = field("E-Mail Adresse");
        PasswordField passField  = passField("Passwort");
        Label errLabel = errLabel();

        Label successLabel = new Label(successMsg != null ? successMsg : "");
        successLabel.setStyle(OKCLR);
        successLabel.setWrapText(true);
        successLabel.setVisible(successMsg != null);

        card.getChildren().addAll(
            iconLabel("👤"), title("Einloggen"), successLabel,
            formLbl("E-Mail"),   emailField,
            formLbl("Passwort"), passField,
            errLabel,
            btn("Einloggen", BLK, e -> {
                String email    = emailField.getText().trim();
                String password = passField.getText();
                if (email.isEmpty())    { errLabel.setText("Bitte eine E-Mail-Adresse eingeben!"); return; }
                if (password.isEmpty()) { errLabel.setText("Bitte das Passwort eingeben!"); return; }
                Passenger found = Main.getDatabase().getRegisteredPassengers().stream()
                        .filter(p -> p.getEmail().equalsIgnoreCase(email)
                                  && PasswordUtils.verify(password, p.getPassword()))
                        .findFirst().orElse(null);
                if (found == null) { errLabel.setText("E-Mail oder Passwort falsch."); return; }
                loggedInPassenger = found;
                showBookingScene();
            }),
            orDivider(),
            btn("Neu registrieren", GRY, e -> showRegistrationScene()),
            back(e -> showRoleSelectionScene())
        );
        show(centered(card), 460, 520);
    }

    // ── SZENE 2.5: Kunden-Registrierung ────────────────────────────────────────

    private void showRegistrationScene() {
        VBox card = card();
        TextField     nameField    = field("z. B. Max Mustermann");
        TextField     emailField   = field("z. B. max@example.com");
        PasswordField passField    = passField("Mindestens 6 Zeichen");
        PasswordField confirmField = passField("Passwort wiederholen");
        Label errLabel = errLabel();

        card.getChildren().addAll(
            iconLabel("✏️"), title("Konto erstellen"),
            formLbl("Name"),                 nameField,
            formLbl("E-Mail"),               emailField,
            formLbl("Passwort"),             passField,
            formLbl("Passwort wiederholen"), confirmField,
            errLabel,
            btn("Registrieren", BLK, e -> {
                String name     = nameField.getText().trim();
                String email    = emailField.getText().trim();
                String password = passField.getText();
                String confirm  = confirmField.getText();
                if (name.isEmpty())                 { errLabel.setText("Bitte deinen Namen eingeben!"); return; }
                if (email.isEmpty())                { errLabel.setText("Bitte eine E-Mail-Adresse eingeben!"); return; }
                if (password.length() < 6)          { errLabel.setText("Das Passwort muss mindestens 6 Zeichen haben!"); return; }
                if (!password.equals(confirm))      { errLabel.setText("Die Passwörter stimmen nicht überein!"); return; }
                boolean exists = Main.getDatabase().getRegisteredPassengers().stream()
                        .anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
                if (exists) { errLabel.setText("Diese E-Mail-Adresse ist bereits registriert!"); return; }
                int newId = Main.getDatabase().getRegisteredPassengers().stream()
                        .mapToInt(Passenger::getId).max().orElse(0) + 1;
                Main.getDatabase().addPassenger(new Passenger(newId, name, email, PasswordUtils.hash(password)));
                showCustomerAuthScene("Konto erstellt! Bitte jetzt einloggen.");
            }),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 560);
    }

    // ── SZENE F1: Fahrer-Anmeldung ──────────────────────────────────────────────

    private void showDriverAuthScene() { showDriverAuthScene(null); }

    private void showDriverAuthScene(String successMsg) {
        VBox card = card();
        TextField     emailField = field("E-Mail Adresse");
        PasswordField passField  = passField("Passwort");
        Label errLabel = errLabel();

        Label successLabel = new Label(successMsg != null ? successMsg : "");
        successLabel.setStyle(OKCLR);
        successLabel.setWrapText(true);
        successLabel.setVisible(successMsg != null);

        card.getChildren().addAll(
            iconLabel("🚘"), title("Fahrer-Anmeldung"), successLabel,
            formLbl("E-Mail"),   emailField,
            formLbl("Passwort"), passField,
            errLabel,
            btn("Einloggen", BLK, e -> {
                String email    = emailField.getText().trim();
                String password = passField.getText();
                if (email.isEmpty())    { errLabel.setText("Bitte eine E-Mail-Adresse eingeben!"); return; }
                if (password.isEmpty()) { errLabel.setText("Bitte das Passwort eingeben!"); return; }
                Driver found = Main.getDatabase().getRegisteredDrivers().stream()
                        .filter(d -> d.getEmail().equalsIgnoreCase(email)
                                  && PasswordUtils.verify(password, d.getPassword()))
                        .findFirst().orElse(null);
                if (found == null) { errLabel.setText("E-Mail oder Passwort falsch."); return; }
                loggedInDriver = found;
                showDriverShiftStartScene();
            }),
            orDivider(),
            btn("Als Fahrer registrieren", GRY, e -> showDriverRegistrationScene()),
            back(e -> showRoleSelectionScene())
        );
        show(centered(card), 460, 520);
    }

    // ── SZENE F2: Fahrer-Registrierung ──────────────────────────────────────────

    private void showDriverRegistrationScene() {
        VBox card = card();
        TextField     nameField    = field("z. B. Ahmed Yilmaz");
        TextField     emailField   = field("z. B. ahmed@example.com");
        PasswordField passField    = passField("Mindestens 6 Zeichen");
        PasswordField confirmField = passField("Passwort wiederholen");
        Label errLabel = errLabel();

        card.getChildren().addAll(
            iconLabel("✏️"), title("Fahrer-Konto erstellen"),
            formLbl("Name"),                 nameField,
            formLbl("E-Mail"),               emailField,
            formLbl("Passwort"),             passField,
            formLbl("Passwort wiederholen"), confirmField,
            errLabel,
            btn("Registrieren", BLK, e -> {
                String name     = nameField.getText().trim();
                String email    = emailField.getText().trim();
                String password = passField.getText();
                String confirm  = confirmField.getText();
                if (name.isEmpty())            { errLabel.setText("Bitte deinen Namen eingeben!"); return; }
                if (email.isEmpty())           { errLabel.setText("Bitte eine E-Mail-Adresse eingeben!"); return; }
                if (password.length() < 6)     { errLabel.setText("Das Passwort muss mindestens 6 Zeichen haben!"); return; }
                if (!password.equals(confirm)) { errLabel.setText("Die Passwörter stimmen nicht überein!"); return; }
                boolean exists = Main.getDatabase().getRegisteredDrivers().stream()
                        .anyMatch(d -> d.getEmail().equalsIgnoreCase(email));
                if (exists) { errLabel.setText("Diese E-Mail-Adresse ist bereits als Fahrer registriert!"); return; }
                int newId = Main.getDatabase().getRegisteredDrivers().stream()
                        .mapToInt(Driver::getId).max().orElse(0) + 1;
                Main.getDatabase().addDriver(new Driver(newId, name, email, PasswordUtils.hash(password)));
                showDriverAuthScene("Fahrer-Konto erstellt! Bitte jetzt einloggen.");
            }),
            back(e -> showDriverAuthScene())
        );
        show(centered(card), 460, 540);
    }

    // ── SZENE F3: Fahrzeug für Schicht wählen ───────────────────────────────────

    private void showDriverShiftStartScene() {
        VBox card = card();

        Label greetLabel = new Label("Willkommen, " + loggedInDriver.getName() + "!");
        greetLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");

        List<Vehicle> allVehicles = Main.getDatabase().getVehicles();
        boolean anyFree = allVehicles.stream()
                .anyMatch(v -> !Main.getDatabase().isVehicleClaimed(v.getId()));

        Label errLabel = errLabel();

        if (!anyFree) {
            card.getChildren().addAll(
                iconLabel("🚘"), title("Schicht beginnen"), greetLabel,
                new Separator(),
                infoBox("Alle Fahrzeuge sind aktuell im Einsatz.\nBitte warte auf die Freigabe eines Fahrzeugs.", "#FEF3C7", "#B45309"),
                back(e -> { loggedInDriver = null; showRoleSelectionScene(); })
            );
            show(centered(card), 460, 380);
            return;
        }

        ComboBox<String> vehicleBox = new ComboBox<>();
        vehicleBox.setStyle(FIELD);
        vehicleBox.setMaxWidth(Double.MAX_VALUE);
        vehicleBox.setPromptText("Freies Fahrzeug wählen...");

        allVehicles.forEach(v -> {
            boolean claimed = Main.getDatabase().isVehicleClaimed(v.getId());
            vehicleBox.getItems().add(
                "Fahrzeug #" + v.getId() + "  –  " +
                (claimed ? "aktuell belegt"
                         : v.getPassengers().size() + "/" + v.getMaxCapacity() + " Fahrgäste"));
        });

        // Belegte Fahrzeuge werden ausgegraut und sind nicht auswählbar
        vehicleBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item);
                    int idx = getIndex();
                    if (idx >= 0 && idx < allVehicles.size()) {
                        boolean claimed = Main.getDatabase().isVehicleClaimed(allVehicles.get(idx).getId());
                        setDisable(claimed);
                        setOpacity(claimed ? 0.4 : 1.0);
                    }
                }
            }
        });

        // Erstes freies Fahrzeug vorauswählen
        for (int i = 0; i < allVehicles.size(); i++) {
            if (!Main.getDatabase().isVehicleClaimed(allVehicles.get(i).getId())) {
                vehicleBox.getSelectionModel().select(i);
                break;
            }
        }

        card.getChildren().addAll(
            iconLabel("🚘"), title("Schicht beginnen"), greetLabel,
            new Separator(),
            formLbl("Fahrzeug für diese Schicht:"), vehicleBox,
            errLabel,
            btn("🟢  Schicht starten", GRN, e -> {
                int idx = vehicleBox.getSelectionModel().getSelectedIndex();
                if (idx < 0) { errLabel.setText("Bitte ein Fahrzeug auswählen!"); return; }
                Vehicle selected = allVehicles.get(idx);
                if (Main.getDatabase().isVehicleClaimed(selected.getId())) {
                    errLabel.setText("Fahrzeug #" + selected.getId() + " ist bereits belegt – bitte ein freies Fahrzeug wählen.");
                    return;
                }
                if (!Main.getDatabase().claimVehicle(selected.getId())) {
                    errLabel.setText("Fahrzeug #" + selected.getId() + " wurde gerade belegt – bitte anderes wählen.");
                    return;
                }
                loggedInDriverVehicleId = selected.getId();
                showDriverScene();
            }),
            back(e -> { loggedInDriver = null; showRoleSelectionScene(); })
        );
        show(centered(card), 460, 440);
    }

    // ── SZENE 3: Buchung ────────────────────────────────────────────────────────

    private void showBookingScene() {
        stopTimer();
        VBox card = card();

        Label welcomeLabel = new Label("Hallo, " + loggedInPassenger.getName() + " 👋");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        card.getChildren().addAll(title("Wohin geht's?"), welcomeLabel);

        boolean driverActive = Main.getDatabase().getVehicles().stream()
                .anyMatch(v -> Main.getDatabase().isVehicleClaimed(v.getId()));
        if (!driverActive) {
            VBox noDriverBanner = new VBox(4);
            noDriverBanner.setStyle("-fx-background-color: #FEF3C7; -fx-padding: 12px; -fx-background-radius: 10px;");
            Label noDriverTitle = new Label("⚠  Kein Fahrer aktiv");
            noDriverTitle.setStyle("-fx-text-fill: #92400E; -fx-font-weight: bold; -fx-font-size: 13px;");
            Label noDriverSub = new Label("Buchungen sind erst möglich, wenn ein Fahrer eine Schicht gestartet hat.");
            noDriverSub.setStyle("-fx-text-fill: #B45309; -fx-font-size: 12px;");
            noDriverSub.setWrapText(true);
            noDriverBanner.getChildren().addAll(noDriverTitle, noDriverSub);
            card.getChildren().add(noDriverBanner);
        }

        Booking activeBooking = Main.getDatabase().getActiveBookingForPassenger(loggedInPassenger.getId());
        if (activeBooking != null) {
            VBox banner = new VBox(6);
            banner.setStyle("-fx-background-color: #D1FAE5; -fx-padding: 12px; -fx-background-radius: 10px;");
            Label bannerLabel = new Label("Aktive Fahrt: "
                    + activeBooking.getPickupStationName() + " → " + activeBooking.getDropoffStationName());
            bannerLabel.setStyle("-fx-text-fill: #065F46; -fx-font-weight: bold; -fx-font-size: 13px;");
            bannerLabel.setWrapText(true);
            Button viewBtn = btn("Fahrt anzeigen", GRN, e -> showRideStatusScene());
            viewBtn.setPrefHeight(32);
            banner.getChildren().addAll(bannerLabel, viewBtn);
            card.getChildren().add(banner);
        }

        List<String> stationNames = Main.getDatabase().getStations().stream()
                .filter(s -> !s.isDepot()).map(Station::getName).collect(Collectors.toList());

        ComboBox<String> startBox  = combo(stationNames, "Starthaltepunkt wählen...");
        ComboBox<String> targetBox = combo(stationNames, "Zielhaltepunkt wählen...");
        Label errLabel = errLabel();

        card.getChildren().addAll(
            new Separator(),
            formLbl("Neue Buchung"),
            formLbl("Von"), startBox,
            formLbl("Nach"), targetBox,
            errLabel,
            btn("📍  Jetzt buchen", BLK, e -> {
                if (!Main.getDatabase().getVehicles().stream()
                        .anyMatch(v -> Main.getDatabase().isVehicleClaimed(v.getId()))) {
                    errLabel.setText("Kein Fahrer aktiv – bitte warte, bis ein Fahrer eine Schicht gestartet hat.");
                    return;
                }
                if (Main.getDatabase().getActiveBookingForPassenger(loggedInPassenger.getId()) != null) {
                    errLabel.setText("Du hast bereits eine aktive Fahrt. Bitte schließe diese zuerst ab.");
                    return;
                }
                String startName  = startBox.getValue();
                String targetName = targetBox.getValue();
                if (startName == null || targetName == null) { errLabel.setText("Bitte Start und Ziel auswählen!"); return; }
                if (startName.equals(targetName))            { errLabel.setText("Start und Ziel dürfen nicht identisch sein!"); return; }
                Station start  = findStation(startName);
                Station target = findStation(targetName);
                if (start == null || target == null) { errLabel.setText("Haltepunkt nicht gefunden."); return; }
                errLabel.setText("");
                if (bookingService.bookRide(start, target, loggedInPassenger)) {
                    int bookingId = Main.getDatabase().nextBookingId();
                    Main.getDatabase().addBooking(new Booking(
                            bookingId, loggedInPassenger.getId(),
                            loggedInPassenger.getAssignedVehicle().getId(),
                            start.getName(), target.getName(),
                            PassengerState.WAITING.toString(),
                            LocalDateTime.now().withNano(0).toString()));
                    eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
                    showRideStatusScene();
                } else {
                    errLabel.setText("Buchung fehlgeschlagen – alle Fahrzeuge sind voll.");
                }
            }),
            btn("📋  Meine Fahrten", GRY, e -> showMyRidesScene()),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 600);
    }

    // ── SZENE 4: Live-Fahrstatus ────────────────────────────────────────────────

    private void showRideStatusScene() {
        stopTimer();
        VBox card = card();
        Passenger passenger = loggedInPassenger;
        Vehicle vehicle = passenger.getAssignedVehicle();

        Label vehicleLabel = new Label("Fahrzeug #" + (vehicle != null ? vehicle.getId() : "—"));
        vehicleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        vehicleLabel.setStyle("-fx-text-fill: #1E1B4B;");

        Label pickupLabel  = lbl("📍 Abholung: " + (passenger.getPickupStation()  != null ? passenger.getPickupStation().getName()  : "—"));
        Label dropoffLabel = lbl("🏁 Ziel:      " + (passenger.getDropoffStation() != null ? passenger.getDropoffStation().getName() : "—"));

        Label waitLabel      = new Label("⏳ wird berechnet...");
        Label remainingLabel = new Label("🚗 wird berechnet...");
        VBox  chipHolder     = new VBox();
        chipHolder.getChildren().add(stateChipFromPassenger(passenger));
        waitLabel.setStyle(BLUE);
        remainingLabel.setStyle(GREEN);

        Runnable refresh = () -> {
            chipHolder.getChildren().setAll(stateChipFromPassenger(passenger));
            if (passenger.getState() == PassengerState.ARRIVED) {
                waitLabel.setText("Alles erledigt!");
                waitLabel.setStyle(GREEN);
                remainingLabel.setText("🏁 Fahrt abgeschlossen.");
                remainingLabel.setStyle(GREEN);
            } else {
                int wait      = timeService.getWaitingTime(passenger);
                int remaining = timeService.getRemainingTime(passenger);
                waitLabel.setStyle(BLUE);
                waitLabel.setText(wait >= 0 ? "⏳ Wartezeit bis Abholung: " + wait + " Min." : "✅ Fahrzeug bereits am Startpunkt");
                remainingLabel.setText(remaining >= 0 ? "🚗 Restfahrzeit bis Ziel: " + remaining + " Min." : "—");
            }
        };
        refresh.run();
        eventBus.subscribe(RideEventBus.Event.STOP_CONFIRMED, refresh);

        liveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> refresh.run()));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();

        VBox statusBox = new VBox(8);
        statusBox.setStyle("-fx-background-color: #F0F2FF; -fx-padding: 14px; -fx-background-radius: 10px;");
        statusBox.getChildren().addAll(chipHolder, waitLabel, remainingLabel);

        card.getChildren().addAll(
            title("Meine Fahrt"),
            vehicleLabel, pickupLabel, dropoffLabel,
            new Separator(), statusBox,
            back(e -> {
                eventBus.unsubscribe(RideEventBus.Event.STOP_CONFIRMED, refresh);
                stopTimer();
                showBookingScene();
            })
        );
        show(centered(card), 460, 500);
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
                boolean active = booking.isActive();
                VBox entry = new VBox(5);
                entry.setStyle("-fx-background-color: " + (active ? "#EEF2FF" : "#F9FAFB")
                        + "; -fx-padding: 12px; -fx-background-radius: 10px;"
                        + (active ? " -fx-border-color: #C7D2FE; -fx-border-width: 1; -fx-border-radius: 10px;" : ""));

                Label routeLabel = new Label(booking.getPickupStationName() + "  →  " + booking.getDropoffStationName());
                routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E1B4B;");
                routeLabel.setWrapText(true);

                String dateDisplay = (booking.getBookedAt() != null && booking.getBookedAt().length() >= 16)
                        ? booking.getBookedAt().substring(0, 16).replace('T', ' ') : "—";

                HBox meta = new HBox(8);
                meta.setAlignment(Pos.CENTER_LEFT);
                meta.getChildren().addAll(
                    stateChipFromString(booking.getState()),
                    lbl("Fahrzeug #" + booking.getVehicleId() + "  ·  " + dateDisplay)
                );
                entry.getChildren().addAll(routeLabel, meta);

                if (active) {
                    Button viewBtn = btn("Fahrt anzeigen →", GRN, e -> showRideStatusScene());
                    viewBtn.setPrefHeight(32);
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
        show(scroll, 460, 640);
    }

    // ── SZENE 5: Fahrer-Tablet ──────────────────────────────────────────────────

    private void showDriverScene() {
        stopTimer();
        VBox card = card();
        card.setMaxWidth(460);

        Label driverInfoLabel = new Label(
                "👤 " + loggedInDriver.getName() + "   |   🚘 Fahrzeug #" + loggedInDriverVehicleId);
        driverInfoLabel.setStyle("-fx-text-fill: #4F46E5; -fx-font-size: 12px; -fx-font-weight: bold;");
        driverInfoLabel.setWrapText(true);

        List<Vehicle> vehicles = Main.getDatabase().getVehicles();
        if (vehicles.isEmpty()) {
            card.getChildren().addAll(
                title("Fahrer-Tablet"), driverInfoLabel,
                new Label("Keine Fahrzeuge vorhanden."),
                back(e -> { Main.getDatabase().releaseVehicle(loggedInDriverVehicleId); loggedInDriverVehicleId = 0; loggedInDriver = null; showRoleSelectionScene(); })
            );
            show(centered(card), 460, 300);
            return;
        }

        ComboBox<String> vehicleBox = new ComboBox<>();
        vehicleBox.setStyle(FIELD);
        vehicleBox.setMaxWidth(Double.MAX_VALUE);
        vehicles.forEach(v -> vehicleBox.getItems().add(buildVehicleLabel(v)));

        int ownIdx = 0;
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == loggedInDriverVehicleId) { ownIdx = i; break; }
        }
        vehicleBox.getSelectionModel().select(ownIdx);

        Label nextStopLabel  = new Label("—");
        nextStopLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        nextStopLabel.setStyle("-fx-text-fill: #1E1B4B;");
        Label pickupLabel    = new Label("—");
        Label dropoffLabel   = new Label("—");
        Label routeMapLabel  = new Label("—");
        Label ownershipBadge = new Label("");
        pickupLabel.setWrapText(true);
        dropoffLabel.setWrapText(true);
        routeMapLabel.setWrapText(true);
        routeMapLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #374151;");

        VBox infoPanel = new VBox(7);
        infoPanel.setStyle(
                "-fx-background-color: #F8FAFF; -fx-padding: 14px; -fx-background-radius: 10px;"
                + " -fx-border-color: transparent transparent transparent #4F46E5;"
                + " -fx-border-width: 0 0 0 4; -fx-border-radius: 0 0 0 10px;");
        infoPanel.getChildren().addAll(
            ownershipBadge,
            formLbl("Nächster Halt"), nextStopLabel,
            formLbl("Einsteiger"),    pickupLabel,
            formLbl("Aussteiger"),    dropoffLabel,
            new Separator(), formLbl("Route"), routeMapLabel
        );

        Label statusLabel = new Label("");

        Runnable redraw = () -> {
            int idx = vehicleBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= vehicles.size()) return;
            Vehicle vehicle = vehicles.get(idx);
            boolean isOwn   = vehicle.getId() == loggedInDriverVehicleId;

            ownershipBadge.setText(isOwn ? "★  Dein Fahrzeug" : "👁  Nur lesend");
            ownershipBadge.setStyle(isOwn
                    ? "-fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: bold;");

            Route route = vehicle.getCurrentRoute();
            if (route == null || route.getStops().isEmpty()) {
                nextStopLabel.setText("Kein aktiver Auftrag");
                pickupLabel.setText("—"); dropoffLabel.setText("—"); routeMapLabel.setText("—");
                return;
            }
            RouteStop currentStop = route.getCurrentStop();
            if (currentStop == null) {
                nextStopLabel.setText("Route abgeschlossen.");
                pickupLabel.setText("—"); dropoffLabel.setText("—");
                return;
            }
            nextStopLabel.setText(currentStop.getStation().getName());
            String pickupNames = currentStop.getPassengersToPickUp().stream()
                    .filter(p -> p.getState() == PassengerState.WAITING)
                    .map(Passenger::getName).collect(Collectors.joining(", "));
            pickupLabel.setText(pickupNames.isEmpty() ? "—" : pickupNames);
            String dropoffNames = currentStop.getPassengersToDropOff().stream()
                    .filter(p -> p.getState() == PassengerState.IN_TRANSIT)
                    .map(Passenger::getName).collect(Collectors.joining(", "));
            dropoffLabel.setText(dropoffNames.isEmpty() ? "—" : dropoffNames);
            routeMapLabel.setText(buildRouteDisplay(route));
            vehicleBox.getItems().set(idx, buildVehicleLabel(vehicle));
        };

        vehicleBox.setOnAction(e -> { statusLabel.setText(""); redraw.run(); });
        redraw.run();

        Runnable refreshLabels = () -> {
            for (int i = 0; i < vehicles.size(); i++)
                vehicleBox.getItems().set(i, buildVehicleLabel(vehicles.get(i)));
        };
        Runnable onBookingChanged = () -> { refreshLabels.run(); redraw.run(); };
        eventBus.subscribe(RideEventBus.Event.BOOKING_CHANGED, onBookingChanged);

        liveTimer = new Timeline(new KeyFrame(Duration.seconds(10), ev -> redraw.run()));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();

        Button confirmBtn = btn("✓  Haltepunkt bestätigen", GRN, null);
        confirmBtn.setOnAction(e -> {
            int idx = vehicleBox.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Vehicle vehicle = vehicles.get(idx);

            if (vehicle.getId() != loggedInDriverVehicleId) {
                statusLabel.setStyle(ERR);
                statusLabel.setText("Nur Haltepunkte von Fahrzeug #" + loggedInDriverVehicleId + " können bestätigt werden.");
                return;
            }
            if (vehicle.getCurrentRoute() == null) {
                statusLabel.setStyle(OKCLR);
                statusLabel.setText("Kein aktiver Auftrag.");
                return;
            }
            RouteStop currentStop = vehicle.getCurrentRoute().getCurrentStop();
            if (currentStop == null) {
                statusLabel.setStyle(OKCLR);
                statusLabel.setText("Alle Halte bestätigt.");
                return;
            }
            String stationName = currentStop.getStation().getName();
            fleetService.confirmArrival(vehicle, currentStop);
            eventBus.publish(RideEventBus.Event.STOP_CONFIRMED);
            statusLabel.setStyle(OKCLR);
            statusLabel.setText("✓  " + stationName + " bestätigt.");
            redraw.run();
        });

        Button logoutBtn = btn("🔓  Ausloggen", GRY, e -> {
            Vehicle own = vehicles.stream()
                    .filter(v -> v.getId() == loggedInDriverVehicleId)
                    .findFirst().orElse(null);
            boolean routeActive = own != null
                    && own.getCurrentRoute() != null
                    && own.getCurrentRoute().getCurrentStop() != null;
            if (routeActive) {
                statusLabel.setStyle(ERR);
                statusLabel.setText("Bitte erst alle Haltestellen bestätigen, bevor du dich ausloggst.");
                return;
            }
            eventBus.unsubscribe(RideEventBus.Event.BOOKING_CHANGED, onBookingChanged);
            stopTimer();
            Main.getDatabase().releaseVehicle(loggedInDriverVehicleId);
            loggedInDriverVehicleId = 0;
            loggedInDriver = null;
            showRoleSelectionScene();
        });

        card.getChildren().addAll(
            title("Fahrer-Tablet"), driverInfoLabel,
            formLbl("Fahrzeug wählen"), vehicleBox,
            infoPanel, confirmBtn, statusLabel, logoutBtn
        );

        VBox scrollContent = new VBox(card);
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(BG);
        scrollContent.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        show(scroll, 520, 720);
    }

    // ── SZENE 6: Simulation ─────────────────────────────────────────────────────

    private void showSimulationScene() {
        stopTimer();

        List<Vehicle> allV       = Main.getDatabase().getVehicles();
        String[]      drivers    = {"Klaus Weber", "Maria Schmidt", "Ahmed Yilmaz"};
        String[]      cardBg     = {"#EFF6FF", "#F0FDF4", "#FFF7ED"};
        String[]      cardBorder = {"#93C5FD", "#86EFAC", "#FCD34D"};
        String[]      cardFg     = {"#1D4ED8", "#166534", "#92400E"};
        boolean[]     autoOn     = {false};

        // ── Log ──────────────────────────────────────────────────────────────
        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(300);
        log.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        log.textProperty().addListener((obs, o, n) -> log.setScrollTop(Double.MAX_VALUE));

        // ── 3 Fahrzeug-Karten ─────────────────────────────────────────────────
        Label[] cardBody = new Label[3];
        HBox    cardsRow = new HBox(8);
        for (int i = 0; i < 3; i++) {
            Label hdr = new Label("Fzg #" + (i + 1) + "  ·  " + drivers[i].split(" ")[0]);
            hdr.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            hdr.setStyle("-fx-text-fill: " + cardFg[i] + ";");
            cardBody[i] = new Label("– nicht im Dienst –");
            cardBody[i].setStyle("-fx-font-family: monospace; -fx-font-size: 10px; -fx-text-fill: #374151;");
            cardBody[i].setWrapText(true);
            VBox card = new VBox(5, hdr, new Separator(), cardBody[i]);
            card.setStyle("-fx-background-color: " + cardBg[i] + "; -fx-padding: 10px;"
                    + " -fx-background-radius: 10px; -fx-border-color: " + cardBorder[i]
                    + "; -fx-border-radius: 10px; -fx-border-width: 1;");
            card.setMinWidth(160);
            card.setEffect(createShadow());
            HBox.setHgrow(card, Priority.ALWAYS);
            cardsRow.getChildren().add(card);
        }

        // ── Karten-Redraw ─────────────────────────────────────────────────────
        Runnable redraw = () -> {
            for (int i = 0; i < 3; i++) {
                Vehicle v = allV.get(i);
                if (!Main.getDatabase().isVehicleClaimed(v.getId())) {
                    cardBody[i].setText("– nicht im Dienst –"); continue;
                }
                if (v.getCurrentRoute() == null || v.getCurrentRoute().getCurrentStop() == null) {
                    cardBody[i].setText("Bereit – wartet auf Aufträge"); continue;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Bord (").append(v.getPassengers().size()).append("/")
                  .append(v.getMaxCapacity()).append("): ");
                sb.append(v.getPassengers().isEmpty() ? "leer"
                    : v.getPassengers().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
                sb.append("\n\n").append(buildRouteDisplay(v.getCurrentRoute()));
                cardBody[i].setText(sb.toString());
            }
        };

        // ── Buttons ───────────────────────────────────────────────────────────
        Button p1Btn   = btn("🚀  Phase 1 – Schichtstart & Rushhour", BLK, null);
        Button p2Btn   = btn("📈  Phase 2 – Neue Buchungswelle",       GRY, null);
        Button stepBtn = btn("▶   Schritt  (alle Fahrzeuge)",           GRY, null);
        Button autoBtn = btn("⚡  Auto-Simulation",                      GRN, null);
        p2Btn.setDisable(true); stepBtn.setDisable(true); autoBtn.setDisable(true);

        // ── SCHRITT – alle Fahrzeuge einen Halt weiter ────────────────────────
        stepBtn.setOnAction(e -> {
            boolean anyActive = false;
            StringBuilder tick = new StringBuilder();
            for (Vehicle v : allV) {
                if (v.getCurrentRoute() == null) continue;
                RouteStop cur = v.getCurrentRoute().getCurrentStop();
                if (cur == null) continue;
                anyActive = true;
                tick.append("  Fzg #").append(v.getId())
                    .append("  ▶  ").append(cur.getStation().getName());
                List<Passenger> ups = cur.getPassengersToPickUp().stream()
                        .filter(p -> p.getState() == PassengerState.WAITING)
                        .collect(Collectors.toList());
                List<Passenger> downs = cur.getPassengersToDropOff().stream()
                        .filter(p -> p.getState() == PassengerState.IN_TRANSIT)
                        .collect(Collectors.toList());
                if (!ups.isEmpty())
                    tick.append("   ⬆ ").append(ups.stream().map(Passenger::getName).collect(Collectors.joining("+")));
                if (!downs.isEmpty())
                    tick.append("   ⬇ ").append(downs.stream().map(Passenger::getName).collect(Collectors.joining("+")));
                tick.append("\n");
                fleetService.confirmArrival(v, cur);
                eventBus.publish(RideEventBus.Event.STOP_CONFIRMED);
                if (v.getCurrentRoute().getCurrentStop() == null)
                    tick.append("  Fzg #").append(v.getId()).append("  🏁  Zentrale erreicht\n");
            }
            if (!anyActive) {
                log.appendText("\n╔═══════════════════════════════════════════╗\n");
                log.appendText("║  ✅  Alle Fahrzeuge haben die Zentrale    ║\n");
                log.appendText("║      erreicht – Simulation abgeschlossen! ║\n");
                log.appendText("╚═══════════════════════════════════════════╝\n");
                stepBtn.setDisable(true); autoBtn.setDisable(true);
                stopTimer(); autoOn[0] = false; autoBtn.setText("⚡  Auto-Simulation");
            } else {
                log.appendText(tick.toString());
            }
            redraw.run();
        });

        // ── PHASE 1: Schichtstart + Rushhour-Welle 1 ─────────────────────────
        p1Btn.setOnAction(e -> {
            stopTimer(); autoOn[0] = false; autoBtn.setText("⚡  Auto-Simulation");
            for (Vehicle v : allV) {
                new ArrayList<>(v.getPassengers()).forEach(v::removePassenger);
                v.setCurrentRoute(null);
                Main.getDatabase().releaseVehicle(v.getId());
            }
            log.clear();
            log.appendText("╔══════════════════════════════════════════════════════════╗\n");
            log.appendText("║    EasyRide LIVE  ·  Berliner Rushhour-Simulation        ║\n");
            log.appendText("║    3 Fahrer · 3 Fahrzeuge · 12 Fahrgäste · Volllast      ║\n");
            log.appendText("╚══════════════════════════════════════════════════════════╝\n\n");

            log.appendText("🟢  SCHICHTSTART  –  alle Fahrer melden sich an\n");
            for (int i = 0; i < 3; i++) {
                Main.getDatabase().claimVehicle(allV.get(i).getId());
                log.appendText("    " + padRight(drivers[i], 16) + "  →  Fahrzeug #" + allV.get(i).getId() + "\n");
            }

            log.appendText("\n──────────────────────────────────────────────────────────\n");
            log.appendText("🚖  BUCHUNGSWELLE 1  –  8 Fahrgäste auf einmal\n\n");

            Station alex    = findStation("Alexanderplatz");
            Station zoo     = findStation("Zoologischer Garten");
            Station kotti   = findStation("Kottbusser Tor");
            Station potsd   = findStation("Potsdamer Platz");
            Station rosen   = findStation("Rosenthaler Platz");
            Station herman  = findStation("Hermannstraße");
            Station warsch  = findStation("Warschauer Straße");
            Station mehring = findStation("Mehringdamm");
            Station frankf  = findStation("Frankfurter Tor");
            Station branden = findStation("Brandenburger Tor");
            Station museen  = findStation("Museumsinsel");
            Station gleisdr = findStation("Gleisdreieck");
            Station friedr  = findStation("Friedrichstraße");
            Station witten  = findStation("Wittenbergplatz");

            // Fahrzeug #1 füllt sich auf 4/4
            simBookAndLog(log, new Passenger(91, "anna@sim.de"),  alex,    zoo,     allV);
            simBookAndLog(log, new Passenger(92, "ben@sim.de"),   kotti,   potsd,   allV);
            simBookAndLog(log, new Passenger(93, "clara@sim.de"), rosen,   herman,  allV);
            simBookAndLog(log, new Passenger(94, "david@sim.de"), warsch,  mehring, allV);
            log.appendText("    ⚠️  Fzg #1 voll (4/4) – Überlauf → Fzg #2\n\n");

            // Fahrzeug #2 füllt sich auf 4/4
            simBookAndLog(log, new Passenger(95, "eva@sim.de"),   frankf,  branden, allV);
            simBookAndLog(log, new Passenger(96, "felix@sim.de"), museen,  gleisdr, allV);
            simBookAndLog(log, new Passenger(97, "greta@sim.de"), alex,    friedr,  allV);
            simBookAndLog(log, new Passenger(98, "hans@sim.de"),  potsd,   witten,  allV);
            log.appendText("    ⚠️  Fzg #2 voll (4/4) – weitere Buchungen → Fzg #3\n");

            log.appendText("\n──────────────────────────────────────────────────────────\n");
            log.appendText("🗺️   ROUTENÜBERSICHT NACH WELLE 1\n");
            for (int i = 0; i < 3; i++) {
                Vehicle v = allV.get(i);
                log.appendText("\n  Fzg #" + v.getId() + "  (" + drivers[i] + ")\n");
                if (v.getCurrentRoute() != null)
                    for (String ln : buildRouteDisplay(v.getCurrentRoute()).split("\n"))
                        log.appendText("    " + ln + "\n");
                else
                    log.appendText("    – kein Auftrag –\n");
            }
            log.appendText("\n──────────────────────────────────────────────────────────\n");

            redraw.run();
            eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
            p2Btn.setDisable(false); stepBtn.setDisable(false); autoBtn.setDisable(false);
            p1Btn.setText("↺  Neustart");
        });

        // ── PHASE 2: Neue Welle während Fahrzeuge unterwegs sind ─────────────
        p2Btn.setOnAction(e -> {
            p2Btn.setDisable(true);
            Station kotti   = findStation("Kottbusser Tor");
            Station warsch  = findStation("Warschauer Straße");
            Station alex    = findStation("Alexanderplatz");
            Station potsd   = findStation("Potsdamer Platz");
            Station zoo     = findStation("Zoologischer Garten");
            Station gleisdr = findStation("Gleisdreieck");
            Station rosen   = findStation("Rosenthaler Platz");
            Station frankf  = findStation("Frankfurter Tor");
            Station museen  = findStation("Museumsinsel");
            Station branden = findStation("Brandenburger Tor");

            log.appendText("\n📈  BUCHUNGSWELLE 2  –  Dynamische Einfügung (Fzg. bereits unterwegs)\n\n");
            simBookAndLog(log, new Passenger(101, "ida@sim.de"),   kotti,  warsch,  allV);
            simBookAndLog(log, new Passenger(102, "jan@sim.de"),   alex,   potsd,   allV);
            simBookAndLog(log, new Passenger(103, "karla@sim.de"), zoo,    gleisdr, allV);

            log.appendText("\n⚠️   KAPAZITÄTSTEST  –  Noch ein Fahrgast wenn alle Fzg. voll?\n");
            Passenger luca = new Passenger(104, "luca@sim.de");
            boolean okLuca = bookingService.bookRide(rosen, frankf, luca);
            if (okLuca && luca.getAssignedVehicle() != null)
                log.appendText("    Luca  →  Fzg #" + luca.getAssignedVehicle().getId()
                        + "  (" + luca.getAssignedVehicle().getPassengers().size() + "/4)\n");
            else
                log.appendText("    Luca  →  ❌  Alle Fahrzeuge voll! Kein Platz verfügbar.\n"
                        + "               Bitte warten bis ein Fahrzeug Platz hat.\n");

            log.appendText("\n⚠️   GRENZFALL: Buchung ohne aktiven Fahrer möglich?\n");
            // Kurzzeitig alle Fahrzeuge freigeben und sofort testen
            for (Vehicle v : allV) Main.getDatabase().releaseVehicle(v.getId());
            Passenger mia = new Passenger(105, "mia@sim.de");
            boolean okMia = bookingService.bookRide(museen, branden, mia);
            log.appendText(okMia
                    ? "    Mia  →  Buchung ohne Fahrer erfolgreich?! (Bug)\n"
                    : "    Mia  →  ❌  Kein aktiver Fahrer – Buchung korrekt abgelehnt\n");
            // Fahrzeuge wieder beanspruchen
            for (Vehicle v : allV) Main.getDatabase().claimVehicle(v.getId());

            log.appendText("\n──────────────────────────────────────────────────────────\n");
            log.appendText("🗺️   ROUTENÜBERSICHT NACH WELLE 2\n");
            String[] drs = {"Klaus Weber", "Maria Schmidt", "Ahmed Yilmaz"};
            for (int i = 0; i < 3; i++) {
                Vehicle v = allV.get(i);
                log.appendText("\n  Fzg #" + v.getId() + "  (" + drs[i] + ")\n");
                if (v.getCurrentRoute() != null && v.getCurrentRoute().getCurrentStop() != null)
                    for (String ln : buildRouteDisplay(v.getCurrentRoute()).split("\n"))
                        log.appendText("    " + ln + "\n");
                else
                    log.appendText("    – Schicht beendet / wartet –\n");
            }
            log.appendText("\n──────────────────────────────────────────────────────────\n");
            log.appendText("▶   Weiter mit Auto-Simulation oder Schritt-für-Schritt!\n");
            log.appendText("──────────────────────────────────────────────────────────\n");

            redraw.run();
            eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
        });

        // ── AUTO ──────────────────────────────────────────────────────────────
        autoBtn.setOnAction(e -> {
            if (autoOn[0]) {
                stopTimer(); autoOn[0] = false; autoBtn.setText("⚡  Auto-Simulation"); return;
            }
            autoOn[0] = true; autoBtn.setText("⏹  Auto stoppen");
            liveTimer = new Timeline(new KeyFrame(Duration.seconds(1.2), ev -> stepBtn.fire()));
            liveTimer.setCycleCount(Timeline.INDEFINITE); liveTimer.play();
        });

        // ── Layout ────────────────────────────────────────────────────────────
        Label subLbl = new Label("3 Fahrer · 3 Fahrzeuge · Rushhour · Kapazitätstest · Dynamisches Routing");
        subLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        HBox row1 = new HBox(8, p1Btn, p2Btn);
        HBox.setHgrow(p1Btn, Priority.ALWAYS); HBox.setHgrow(p2Btn, Priority.ALWAYS);
        HBox row2 = new HBox(8, stepBtn, autoBtn);
        HBox.setHgrow(stepBtn, Priority.ALWAYS); HBox.setHgrow(autoBtn, Priority.ALWAYS);

        VBox root = new VBox(10);
        root.setStyle(BG); root.setPadding(new Insets(20));
        root.getChildren().addAll(
                title("EasyRide LIVE – Vollsimulation"), subLbl, new Separator(),
                row1, cardsRow, formLbl("Live-Log"), log, row2,
                back(ev -> {
                    stopTimer();
                    resetSimulation();
                    showRoleSelectionScene();
                }));
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true); scroll.setStyle(BG);
        show(scroll, 760, 840);
    }

    private void simBookAndLog(TextArea log, Passenger p, Station from, Station to, List<Vehicle> allV) {
        boolean ok = bookingService.bookRide(from, to, p);
        String name = padRight(p.getName(), 8);
        String f    = padRight(from.getName(), 22);
        String t    = padRight(to.getName(), 22);
        if (ok && p.getAssignedVehicle() != null) {
            Vehicle v   = p.getAssignedVehicle();
            String cap  = v.getPassengers().size() + "/" + v.getMaxCapacity();
            boolean full = v.getPassengers().size() >= v.getMaxCapacity();
            log.appendText("    " + name + "  " + f + " → " + t
                    + "  Fzg #" + v.getId() + "  [" + cap + (full ? " VOLL" : "") + "]\n");
        } else {
            log.appendText("    " + name + "  " + f + " → " + t + "  ❌  kein Fzg verfügbar\n");
        }
    }

    private void resetSimulation() {
        for (Vehicle v : Main.getDatabase().getVehicles()) {
            if (v.getCurrentRoute() != null) {
                RouteStop stop;
                while ((stop = v.getCurrentRoute().getCurrentStop()) != null)
                    fleetService.confirmArrival(v, stop);
            }
            new ArrayList<>(v.getPassengers()).forEach(v::removePassenger);
            v.setCurrentRoute(null);
            Main.getDatabase().releaseVehicle(v.getId());
        }
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    // ── Routen aus gespeicherten Buchungen wiederherstellen ─────────────────────

    private static void restoreRoutesFromActiveBookings(IRouteService routeService) {
        List<Booking> active = Main.getDatabase().getBookings().stream()
                .filter(Booking::isActive)
                .sorted(Comparator.comparingInt(Booking::getBookingId))
                .toList();

        for (Booking booking : active) {
            Passenger passenger = Main.getDatabase().getRegisteredPassengers().stream()
                    .filter(p -> p.getId() == booking.getPassengerId()).findFirst().orElse(null);
            Vehicle vehicle = Main.getDatabase().getVehicles().stream()
                    .filter(v -> v.getId() == booking.getVehicleId()).findFirst().orElse(null);
            Station pickup = Main.getDatabase().getStations().stream()
                    .filter(s -> s.getName().equals(booking.getPickupStationName())).findFirst().orElse(null);
            Station dropoff = Main.getDatabase().getStations().stream()
                    .filter(s -> s.getName().equals(booking.getDropoffStationName())).findFirst().orElse(null);

            if (passenger == null || vehicle == null || pickup == null || dropoff == null) continue;

            Route route = vehicle.getCurrentRoute() == null
                    ? routeService.calcInitialRoute(vehicle, pickup, dropoff, passenger)
                    : routeService.calcNewRoute(vehicle.getCurrentRoute(), passenger, pickup, dropoff);
            if (route != null) vehicle.setCurrentRoute(route);

            if (!vehicle.getPassengers().contains(passenger)) {
                vehicle.addPassenger(passenger);
                try { passenger.setState(PassengerState.valueOf(booking.getState())); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // ── Hilfs-Widgets ───────────────────────────────────────────────────────────

    private VBox heroHeader(String icon, String titleText, String subtitleText) {
        Label iconLbl  = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 34px;");
        Label titleLbl = new Label(titleText);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titleLbl.setStyle("-fx-text-fill: white;");
        Label subLbl   = new Label(subtitleText);
        subLbl.setStyle("-fx-text-fill: #C7D2FE; -fx-font-size: 13px;");
        VBox box = new VBox(4, iconLbl, titleLbl, subLbl);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(22, 20, 22, 20));
        box.setStyle(HERO);
        return box;
    }

    private Label iconLabel(String icon) {
        Label l = new Label(icon);
        l.setStyle("-fx-font-size: 28px;");
        return l;
    }

    private Label stateChipFromPassenger(Passenger p) {
        return stateChipFromString(p.getState().toString());
    }

    private Label stateChipFromString(String state) {
        return switch (state) {
            case "WAITING"    -> chip("⏳ Wartend",    "#FEF3C7", "#B45309");
            case "IN_TRANSIT" -> chip("🚗 Unterwegs",  "#DBEAFE", "#1D4ED8");
            case "ARRIVED"    -> chip("✅ Abgeschlossen", "#D1FAE5", "#065F46");
            default           -> chip(state, "#F3F4F6", "#374151");
        };
    }

    private Label chip(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-padding: 3px 10px; -fx-background-radius: 20px;"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private VBox infoBox(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 13px;");
        l.setWrapText(true);
        VBox box = new VBox(l);
        box.setStyle("-fx-background-color: " + bg + "; -fx-padding: 12px; -fx-background-radius: 8px;");
        return box;
    }

    private HBox separatorLine() {
        Region left = new Region(), right = new Region();
        left.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");
        right.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox box = new HBox(left, right);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private HBox orDivider() {
        Region left = new Region(), right = new Region();
        left.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");
        right.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        Label or = new Label(" oder ");
        or.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        HBox box = new HBox(left, or, right);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private String buildVehicleLabel(Vehicle v) {
        boolean isOwn = loggedInDriverVehicleId > 0 && v.getId() == loggedInDriverVehicleId;
        return "Fahrzeug #" + v.getId()
                + "  –  " + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Fahrgäste"
                + (isOwn ? "  ★" : "");
    }

    // ── Basis-Hilsfmethoden ──────────────────────────────────────────────────────

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
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        l.setStyle("-fx-text-fill: #1E1B4B;");
        return l;
    }

    private Label bold(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        l.setStyle("-fx-text-fill: #1E1B4B;");
        return l;
    }

    private Label sub(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        return l;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        return l;
    }

    private Label formLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: bold;");
        return l;
    }

    private Label errLabel() {
        Label l = new Label("");
        l.setStyle(ERR);
        l.setWrapText(true);
        return l;
    }

    private Region spacer() {
        Region r = new Region();
        r.setPrefHeight(6);
        return r;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private ComboBox<String> combo(List<String> items, String prompt) {
        ComboBox<String> c = new ComboBox<>();
        c.getItems().addAll(items);
        c.setPromptText(prompt);
        c.setStyle(FIELD);
        c.setMaxWidth(Double.MAX_VALUE);
        return c;
    }

    private Button btn(String text, String style,
                       javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(text);
        b.setStyle(style);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(42);
        if (handler != null) b.setOnAction(handler);
        return b;
    }

    private Button back(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button("← Zurück");
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        b.setOnAction(handler);
        return b;
    }

    private Station findStation(String name) {
        return Main.getDatabase().getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private void stopTimer() {
        if (liveTimer != null) { liveTimer.stop(); liveTimer = null; }
    }

    private DropShadow createShadow() {
        DropShadow s = new DropShadow();
        s.setColor(Color.rgb(79, 70, 229, 0.12));
        s.setRadius(16);
        s.setOffsetY(6);
        return s;
    }

    private String buildRouteDisplay(Route route) {
        StringBuilder sb = new StringBuilder();
        List<RouteStop> stops = route.getStops();
        int currentIdx = route.getCurrentStopIndex();
        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            boolean isPast = i < currentIdx;
            String marker = isPast ? "✓  " : (i == currentIdx ? "▶  " : "   ");
            sb.append(marker).append(stop.getStation().getName()).append("\n");
            if (!isPast) {
                stop.getPassengersToPickUp().stream()
                        .filter(p -> p.getState() == PassengerState.WAITING)
                        .map(Passenger::getName)
                        .forEach(name -> sb.append("      ⬆  ").append(name).append("\n"));
                stop.getPassengersToDropOff().stream()
                        .filter(p -> p.getState() == PassengerState.IN_TRANSIT)
                        .map(Passenger::getName)
                        .forEach(name -> sb.append("      ⬇  ").append(name).append("\n"));
            }
        }
        return sb.toString().stripTrailing();
    }

    public static void main(String[] args) { launch(args); }
}
