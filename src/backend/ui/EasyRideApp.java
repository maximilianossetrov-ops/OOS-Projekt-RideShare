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

        List<Vehicle> allVehicles  = Main.getDatabase().getVehicles();
        List<Vehicle> freeVehicles = allVehicles.stream()
                .filter(v -> !Main.getDatabase().isVehicleClaimed(v.getId()))
                .collect(Collectors.toList());

        Label errLabel = errLabel();

        if (freeVehicles.isEmpty()) {
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
        freeVehicles.forEach(v -> vehicleBox.getItems().add(
                "Fahrzeug #" + v.getId()
                + "  –  " + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Fahrgäste"));
        vehicleBox.getSelectionModel().selectFirst();

        card.getChildren().addAll(
            iconLabel("🚘"), title("Schicht beginnen"), greetLabel,
            new Separator(),
            formLbl("Fahrzeug für diese Schicht:"), vehicleBox,
            errLabel,
            btn("🟢  Schicht starten", GRN, e -> {
                int idx = vehicleBox.getSelectionModel().getSelectedIndex();
                if (idx < 0) { errLabel.setText("Bitte ein Fahrzeug auswählen!"); return; }
                Vehicle selected = freeVehicles.get(idx);
                if (!Main.getDatabase().claimVehicle(selected.getId())) {
                    errLabel.setText("Fahrzeug #" + selected.getId() + " wurde gerade belegt – bitte anderes wählen.");
                    vehicleBox.getItems().remove(idx);
                    freeVehicles.remove(idx);
                    vehicleBox.getSelectionModel().selectFirst();
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

        card.getChildren().addAll(
            title("Fahrer-Tablet"), driverInfoLabel,
            formLbl("Fahrzeug wählen"), vehicleBox,
            infoPanel, confirmBtn, statusLabel,
            back(e -> {
                eventBus.unsubscribe(RideEventBus.Event.BOOKING_CHANGED, onBookingChanged);
                stopTimer();
                Main.getDatabase().releaseVehicle(loggedInDriverVehicleId);
                loggedInDriverVehicleId = 0;
                loggedInDriver = null;
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
        show(scroll, 520, 720);
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
        statusCard.setStyle("-fx-background-color: white; -fx-padding: 14px; -fx-background-radius: 12px;");
        statusCard.setEffect(createShadow());
        Label simStatusTitle = new Label("Fahrzeug-Status");
        simStatusTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        simStatusTitle.setStyle("-fx-text-fill: #1E1B4B;");
        statusCard.getChildren().addAll(simStatusTitle, routeDisplay);

        Vehicle[] simVehicle = {null};
        boolean[] autoRunning = {false};

        Runnable drawRoute = () -> {
            if (simVehicle[0] == null || simVehicle[0].getCurrentRoute() == null) { routeDisplay.setText("—"); return; }
            Vehicle v = simVehicle[0];
            StringBuilder sb = new StringBuilder();
            sb.append("Fahrzeug #").append(v.getId()).append("  |  Im Fahrzeug: ");
            sb.append(v.getPassengers().isEmpty() ? "niemand"
                : v.getPassengers().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
            sb.append("\n\n").append(buildRouteDisplay(v.getCurrentRoute()));
            routeDisplay.setText(sb.toString());
        };

        Button setupBtn = btn("1.  Demo-Buchungen erstellen",    BLK, null);
        Button stepBtn  = btn("2.  ▶  Einen Halt simulieren",   GRY, null);
        Button autoBtn  = btn("3.  ⚡  Auto-Simulation starten", GRN, null);
        stepBtn.setDisable(true);
        autoBtn.setDisable(true);

        setupBtn.setOnAction(e -> {
            stopTimer(); autoRunning[0] = false; autoBtn.setText("3.  ⚡  Auto-Simulation starten");
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
            if (alex == null || zoo == null || kotti == null || potsdamer == null) { logArea.appendText("FEHLER: Stationen nicht gefunden!\n"); return; }
            Passenger anna = new Passenger(91, "anna@sim.de");
            Passenger bob  = new Passenger(92, "bob@sim.de");
            logArea.appendText("Buchung 1: " + anna.getName() + "  " + alex.getName() + " → " + zoo.getName() + "\n");
            boolean ok1 = bookingService.bookRide(alex, zoo, anna);
            logArea.appendText(ok1 ? "  OK: Fahrzeug #" + anna.getAssignedVehicle().getId() + "\n" : "  FEHLER\n");
            logArea.appendText("\nBuchung 2: " + bob.getName() + "  " + kotti.getName() + " → " + potsdamer.getName() + "\n");
            boolean ok2 = bookingService.bookRide(kotti, potsdamer, bob);
            logArea.appendText(ok2 ? "  OK: Fahrzeug #" + bob.getAssignedVehicle().getId() + "\n" : "  FEHLER\n");
            if (ok1) { logArea.appendText("\nWartezeit anna: " + timeService.getWaitingTime(anna) + " Min.\n"); logArea.appendText("Wartezeit bob:  " + timeService.getWaitingTime(bob) + " Min.\n"); }
            if (ok1 || ok2) eventBus.publish(RideEventBus.Event.BOOKING_CHANGED);
            logArea.appendText("\n"); drawRoute.run();
            stepBtn.setDisable(false); autoBtn.setDisable(false);
        });

        stepBtn.setOnAction(e -> {
            if (simVehicle[0] == null || simVehicle[0].getCurrentRoute() == null) return;
            Route route = simVehicle[0].getCurrentRoute();
            RouteStop currentStop = route.getCurrentStop();
            if (currentStop == null) { logArea.appendText("Simulation beendet.\n"); stepBtn.setDisable(true); autoBtn.setDisable(true); return; }
            logArea.appendText("Haltepunkt: " + currentStop.getStation().getName() + "\n");
            if (!currentStop.getPassengersToPickUp().isEmpty())
                logArea.appendText("  Eingestiegen: " + currentStop.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");
            if (!currentStop.getPassengersToDropOff().isEmpty())
                logArea.appendText("  Ausgestiegen: " + currentStop.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");
            fleetService.confirmArrival(simVehicle[0], currentStop);
            eventBus.publish(RideEventBus.Event.STOP_CONFIRMED);
            drawRoute.run();
            if (simVehicle[0].getCurrentRoute().getCurrentStop() == null) {
                logArea.appendText("\nFahrzeug zurück in der Zentrale.\n");
                stepBtn.setDisable(true); autoBtn.setDisable(true);
                stopTimer(); autoRunning[0] = false; autoBtn.setText("3.  ⚡  Auto-Simulation starten");
            }
        });

        autoBtn.setOnAction(e -> {
            if (autoRunning[0]) { stopTimer(); autoRunning[0] = false; autoBtn.setText("3.  ⚡  Auto-Simulation starten"); return; }
            autoRunning[0] = true; autoBtn.setText("Stop Auto-Simulation");
            liveTimer = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> stepBtn.fire()));
            liveTimer.setCycleCount(Timeline.INDEFINITE); liveTimer.play();
        });

        HBox buttonRow = new HBox(8, setupBtn, stepBtn, autoBtn);
        HBox.setHgrow(setupBtn, Priority.ALWAYS);
        HBox.setHgrow(stepBtn,  Priority.ALWAYS);
        HBox.setHgrow(autoBtn,  Priority.ALWAYS);

        root.getChildren().addAll(title("Simulation"), buttonRow, statusCard, formLbl("Log"), logArea,
            back(ev -> { stopTimer(); showRoleSelectionScene(); }));
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        show(scroll, 600, 720);
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
