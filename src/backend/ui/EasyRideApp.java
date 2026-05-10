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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EasyRideApp extends Application {

    private Stage window;
    private BookingService bookingService;
    private TimeService timeService;
    private Passenger loggedInPassenger;
    private Timeline liveTimer;

    // Styles
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

    @Override
    public void start(Stage stage) {
        window = stage;
        bookingService = new BookingService(Main.database);
        timeService = new TimeService();
        window.setTitle("EasyRide – Smart Mobility");
        showRoleSelectionScene();
    }

    // ── SCENE 1: Role Selection ─────────────────────────────────────────────

    private void showRoleSelectionScene() {
        stopTimer();
        VBox card = card();
        card.setAlignment(Pos.CENTER);
        card.getChildren().addAll(
            title("EasyRide"), sub("Smart Mobility Platform"), spacer(),
            btn("Ich bin Kunde",             BLK, e -> showCustomerAuthScene()),
            btn("Fahrer-Tablet",             GRY, e -> showDriverScene()),
            btn("Simulation starten",        GRN, e -> showSimulationScene())
        );
        show(centered(card), 460, 440);
    }

    // ── SCENE 2: Customer Auth ──────────────────────────────────────────────

    private void showCustomerAuthScene() {
        VBox card = card();
        card.getChildren().addAll(
            title("Kunden-Bereich"),
            btn("Einloggen (Demo)", BLK, e -> {
                List<Passenger> all = Main.database.getRegisteredPassengers();
                if (all.isEmpty()) {
                    loggedInPassenger = new Passenger(1, "demo@easyride.de");
                    Main.database.addPassenger(loggedInPassenger);
                } else {
                    loggedInPassenger = all.get(0);
                }
                showBookingScene();
            }),
            lbl("── oder ──"),
            btn("Neu registrieren", GRY, e -> showRegistrationScene()),
            back(e -> showRoleSelectionScene())
        );
        show(centered(card), 460, 360);
    }

    // ── SCENE 2.5: Registration ─────────────────────────────────────────────

    private void showRegistrationScene() {
        VBox card = card();
        TextField emailField = field("E-Mail Adresse");
        Label errLbl = errLabel();
        card.getChildren().addAll(
            title("Konto erstellen"), emailField, errLbl,
            btn("Registrieren", BLK, e -> {
                String email = emailField.getText().trim();
                if (email.isEmpty()) { errLbl.setText("Bitte E-Mail eingeben!"); return; }
                boolean exists = Main.database.getRegisteredPassengers().stream()
                        .anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
                if (exists) { errLbl.setText("E-Mail bereits registriert!"); return; }
                int id = Main.database.getRegisteredPassengers().size() + 1;
                loggedInPassenger = new Passenger(id, email);
                Main.database.addPassenger(loggedInPassenger);
                showBookingScene();
            }),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 360);
    }

    // ── SCENE 3: Booking ────────────────────────────────────────────────────

    private void showBookingScene() {
        stopTimer();
        VBox card = card();

        Label welcome = new Label("Hallo, " + loggedInPassenger.getName() + " 👋");
        welcome.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");

        List<String> stationNames = Main.database.getStations().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDepot()))
                .map(Station::getName)
                .collect(Collectors.toList());

        ComboBox<String> startBox  = combo(stationNames, "Starthaltepunkt...");
        ComboBox<String> targetBox = combo(stationNames, "Zielhaltepunkt...");
        Label errLbl = errLabel();

        card.getChildren().addAll(
            title("Wohin geht's?"), welcome,
            lbl("Starthaltepunkt"), startBox,
            lbl("Zielhaltepunkt"),  targetBox,
            errLbl,
            btn("Jetzt buchen", BLK, e -> {
                String s = startBox.getValue(), t = targetBox.getValue();
                if (s == null || t == null) { errLbl.setText("Bitte Start und Ziel wählen!"); return; }
                if (s.equals(t))            { errLbl.setText("Start und Ziel dürfen nicht gleich sein!"); return; }
                Station start  = station(s);
                Station target = station(t);
                if (start == null || target == null) { errLbl.setText("Haltepunkt nicht gefunden."); return; }
                errLbl.setText("");
                if (bookingService.bookRide(start, target, loggedInPassenger)) {
                    showRideStatusScene();
                } else {
                    errLbl.setText("Buchung fehlgeschlagen – kein Fahrzeug verfügbar oder keine Route möglich.");
                }
            }),
            back(e -> showCustomerAuthScene())
        );
        show(centered(card), 460, 520);
    }

    // ── SCENE 4: Live Ride Status ───────────────────────────────────────────

    private void showRideStatusScene() {
        stopTimer();
        VBox card = card();
        Passenger p = loggedInPassenger;
        Vehicle v   = p.getAssignedVehicle();

        Label header  = bold("Fahrzeug #" + (v != null ? v.getId() : "—"));
        Label pickup  = lbl("Abholung: " + (p.getPickupStation()  != null ? p.getPickupStation().getName()  : "—"));
        Label dropoff = lbl("Ziel:     " + (p.getDropoffStation() != null ? p.getDropoffStation().getName() : "—"));

        Label waitLbl  = new Label("⏳ wird berechnet...");
        Label remLbl   = new Label("🚗 wird berechnet...");
        Label stateLbl = new Label("Status: " + p.getState());
        waitLbl.setStyle(BLUE);
        remLbl.setStyle(GREEN);
        stateLbl.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");

        Runnable refresh = () -> {
            int w = timeService.getWaitingTime(p);
            int r = timeService.getRemainingTime(p);
            stateLbl.setText("Status: " + p.getState());
            waitLbl.setText(w >= 0
                ? "⏳ Wartezeit bis Abholung: " + w + " Min."
                : "✅ Fahrzeug bereits am Startpunkt");
            remLbl.setText(r >= 0
                ? "🚗 Restfahrzeit bis Ziel: " + r + " Min."
                : (p.getState() == PassengerState.ARRIVED ? "🏁 Angekommen!" : "—"));
        };
        refresh.run();

        // Refresh every 30 s automatically
        liveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> refresh.run()));
        liveTimer.setCycleCount(Timeline.INDEFINITE);
        liveTimer.play();

        card.getChildren().addAll(
            title("Meine Fahrt"), header, pickup, dropoff,
            new Separator(), waitLbl, remLbl, stateLbl,
            btn("Aktualisieren", GRY, e -> refresh.run()),
            back(e -> { stopTimer(); showBookingScene(); })
        );
        show(centered(card), 460, 480);
    }

    // ── SCENE 5: Driver Tablet ──────────────────────────────────────────────

    private void showDriverScene() {
        stopTimer();
        VBox card = card();
        card.setMaxWidth(460);

        List<Vehicle> vehicles = Main.database.getVehicles();
        if (vehicles.isEmpty()) {
            card.getChildren().addAll(title("Fahrer-Tablet"),
                new Label("Keine Fahrzeuge vorhanden."), back(e -> showRoleSelectionScene()));
            show(centered(card), 460, 280);
            return;
        }

        ComboBox<String> vBox = new ComboBox<>();
        vBox.setStyle(FIELD);
        vBox.setMaxWidth(Double.MAX_VALUE);
        vehicles.forEach(v ->
            vBox.getItems().add("Fahrzeug #" + v.getId() + "  (" + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Sitzpl.)"));
        vBox.getSelectionModel().selectFirst();

        Label nextStopLbl  = bold("—");
        Label pickupLbl    = new Label("—");
        Label dropoffLbl   = new Label("—");
        Label routeMapLbl  = new Label("—");
        pickupLbl.setWrapText(true);
        dropoffLbl.setWrapText(true);
        routeMapLbl.setWrapText(true);
        routeMapLbl.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #495057;");

        VBox infoPanel = new VBox(6);
        infoPanel.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 12px; -fx-background-radius: 8px;");
        infoPanel.getChildren().addAll(
            lbl("Nächster anzufahrender Halt:"), nextStopLbl,
            lbl("Einsteiger:"), pickupLbl,
            lbl("Aussteiger:"), dropoffLbl,
            new Separator(), lbl("Route:"), routeMapLbl
        );

        Label statusLbl = new Label("");

        Runnable redraw = () -> {
            int idx = vBox.getSelectionModel().getSelectedIndex();
            if (idx < 0 || idx >= vehicles.size()) return;
            Vehicle v = vehicles.get(idx);
            Route route = v.getCurrentRoute();
            if (route == null || route.getStops().isEmpty()) {
                nextStopLbl.setText("Keine aktive Route");
                pickupLbl.setText("—"); dropoffLbl.setText("—"); routeMapLbl.setText("—");
                return;
            }
            RouteStop cur = route.getCurrentStop();
            if (cur == null) { nextStopLbl.setText("Route abgeschlossen."); return; }

            nextStopLbl.setText(cur.getStation().getName());
            pickupLbl.setText(cur.getPassengersToPickUp().isEmpty() ? "Keine"
                : cur.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
            dropoffLbl.setText(cur.getPassengersToDropOff().isEmpty() ? "Keine"
                : cur.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(", ")));

            StringBuilder sb = new StringBuilder();
            List<RouteStop> stops = route.getStops();
            for (int i = 0; i < stops.size(); i++) {
                RouteStop s = stops.get(i);
                String m = i < route.getCurrentStopIndex() ? "✓ " : (i == route.getCurrentStopIndex() ? "▶ " : "  ");
                sb.append(m).append(s.getStation().getName());
                if (!s.getPassengersToPickUp().isEmpty())
                    sb.append("  ⬆").append(s.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(",")));
                if (!s.getPassengersToDropOff().isEmpty())
                    sb.append("  ⬇").append(s.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(",")));
                sb.append('\n');
            }
            routeMapLbl.setText(sb.toString());
            vBox.getItems().set(idx,
                "Fahrzeug #" + v.getId() + "  (" + v.getPassengers().size() + "/" + v.getMaxCapacity() + " Sitzpl.)");
        };

        vBox.setOnAction(e -> redraw.run());
        redraw.run();

        Button confirmBtn = btn("✓  Haltepunkt bestätigen", GRN, null);
        confirmBtn.setOnAction(e -> {
            int idx = vBox.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Vehicle v = vehicles.get(idx);
            if (v.getCurrentRoute() == null) { statusLbl.setStyle(ERR); statusLbl.setText("Keine aktive Route."); return; }
            RouteStop cur = v.getCurrentRoute().getCurrentStop();
            if (cur == null) { statusLbl.setStyle(ERR); statusLbl.setText("Alle Halte bereits bestätigt."); return; }
            String name = cur.getStation().getName();
            bookingService.confirmArrival(v, cur);
            statusLbl.setStyle(OKCLR);
            statusLbl.setText("✓  " + name + " bestätigt.");
            redraw.run();
        });

        card.getChildren().addAll(
            title("Fahrer-Tablet"),
            lbl("Fahrzeug wählen:"), vBox,
            infoPanel, confirmBtn, statusLbl,
            back(e -> showRoleSelectionScene())
        );

        VBox scrollContent = new VBox(card);
        scrollContent.setAlignment(Pos.CENTER);
        scrollContent.setStyle(BG);
        scrollContent.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        window.setScene(new Scene(scroll, 520, 700));
    }

    // ── SCENE 6: Simulation ─────────────────────────────────────────────────

    private void showSimulationScene() {
        stopTimer();

        VBox root = new VBox(12);
        root.setStyle(BG);
        root.setPadding(new Insets(20));

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(220);
        log.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Label routeDisplay = new Label("—");
        routeDisplay.setWrapText(true);
        routeDisplay.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        VBox statusCard = new VBox(8);
        statusCard.setStyle("-fx-background-color: white; -fx-padding: 14px; -fx-background-radius: 10px;");
        statusCard.setEffect(createShadow());
        statusCard.getChildren().addAll(bold("Fahrzeug-Status"), routeDisplay);

        // Mutable simulation state via arrays (lambda capture)
        Vehicle[] sim = {null};
        boolean[] autoOn = {false};

        Runnable drawRoute = () -> {
            if (sim[0] == null || sim[0].getCurrentRoute() == null) { routeDisplay.setText("—"); return; }
            Vehicle v = sim[0];
            Route route = v.getCurrentRoute();
            StringBuilder sb = new StringBuilder();
            sb.append("Fahrzeug #").append(v.getId()).append("  |  Im Fahrzeug: ");
            if (v.getPassengers().isEmpty()) sb.append("niemand");
            else sb.append(v.getPassengers().stream().map(Passenger::getName).collect(Collectors.joining(", ")));
            sb.append("\n\n");
            List<RouteStop> stops = route.getStops();
            for (int i = 0; i < stops.size(); i++) {
                RouteStop s = stops.get(i);
                String m = i < route.getCurrentStopIndex() ? "✓ " : (i == route.getCurrentStopIndex() ? "▶ " : "  ");
                sb.append(m).append(s.getStation().getName());
                if (!s.getPassengersToPickUp().isEmpty())
                    sb.append("  ⬆[").append(s.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(","))).append("]");
                if (!s.getPassengersToDropOff().isEmpty())
                    sb.append("  ⬇[").append(s.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(","))).append("]");
                sb.append('\n');
            }
            routeDisplay.setText(sb.toString());
        };

        Button setupBtn = btn("1.  Demo-Buchungen erstellen",     BLK, null);
        Button stepBtn  = btn("2.  ▶  Einen Halt simulieren",    GRY, null);
        Button autoBtn  = btn("3.  ⚡  Auto-Simulation starten", GRN, null);
        stepBtn.setDisable(true);
        autoBtn.setDisable(true);

        setupBtn.setOnAction(e -> {
            stopTimer();
            autoOn[0] = false;
            autoBtn.setText("3.  ⚡  Auto-Simulation starten");

            // Reset vehicle 1 completely
            Vehicle v = Main.database.getVehicles().get(0);
            sim[0] = v;
            new ArrayList<>(v.getPassengers()).forEach(p -> v.removePassenger(p));
            v.setCurrentRoute(null);

            Station alex      = station("Alexanderplatz");
            Station zoo       = station("Zoologischer Garten");
            Station kotti     = station("Kottbusser Tor");
            Station potsdamer = station("Potsdamer Platz");

            log.clear();
            log.appendText("=== Simulation gestartet ===\n\n");

            if (alex == null || zoo == null || kotti == null || potsdamer == null) {
                log.appendText("FEHLER: Stationen nicht gefunden!\n"); return;
            }

            Passenger anna = new Passenger(91, "anna@sim.de");
            Passenger bob  = new Passenger(92, "bob@sim.de");

            log.appendText("Buchung 1: " + anna.getName() + "  " + alex.getName() + " -> " + zoo.getName() + "\n");
            boolean ok1 = bookingService.bookRide(alex, zoo, anna);
            log.appendText(ok1 ? "  OK: Fahrzeug #" + anna.getAssignedVehicle().getId() + "\n"
                               : "  FEHLER: Buchung fehlgeschlagen\n");

            log.appendText("\nBuchung 2: " + bob.getName() + "  " + kotti.getName() + " -> " + potsdamer.getName() + "\n");
            boolean ok2 = bookingService.bookRide(kotti, potsdamer, bob);
            log.appendText(ok2 ? "  OK: Fahrzeug #" + bob.getAssignedVehicle().getId() + "\n"
                               : "  FEHLER: Buchung fehlgeschlagen\n");

            if (ok1) {
                log.appendText("\nWartezeit anna: " + timeService.getWaitingTime(anna) + " Min.\n");
                log.appendText("Wartezeit bob:  " + timeService.getWaitingTime(bob)  + " Min.\n");
            }
            log.appendText("\n");
            drawRoute.run();
            stepBtn.setDisable(false);
            autoBtn.setDisable(false);
        });

        stepBtn.setOnAction(e -> {
            if (sim[0] == null || sim[0].getCurrentRoute() == null) return;
            Route route = sim[0].getCurrentRoute();
            RouteStop cur = route.getCurrentStop();
            if (cur == null) {
                log.appendText("Simulation beendet.\n");
                stepBtn.setDisable(true); autoBtn.setDisable(true);
                return;
            }
            log.appendText("Haltepunkt: " + cur.getStation().getName() + "\n");
            if (!cur.getPassengersToPickUp().isEmpty())
                log.appendText("  Eingestiegen: " + cur.getPassengersToPickUp().stream().map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");
            if (!cur.getPassengersToDropOff().isEmpty())
                log.appendText("  Ausgestiegen: " + cur.getPassengersToDropOff().stream().map(Passenger::getName).collect(Collectors.joining(", ")) + "\n");

            bookingService.confirmArrival(sim[0], cur);
            drawRoute.run();

            if (sim[0].getCurrentRoute().getCurrentStop() == null) {
                log.appendText("\nFahrzeug zurueck in der Zentrale.\n");
                stepBtn.setDisable(true); autoBtn.setDisable(true);
                stopTimer(); autoOn[0] = false;
                autoBtn.setText("3.  ⚡  Auto-Simulation starten");
            }
        });

        autoBtn.setOnAction(e -> {
            if (autoOn[0]) {
                stopTimer(); autoOn[0] = false;
                autoBtn.setText("3.  ⚡  Auto-Simulation starten");
                return;
            }
            autoOn[0] = true;
            autoBtn.setText("Stop Auto-Simulation");
            liveTimer = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> stepBtn.fire()));
            liveTimer.setCycleCount(Timeline.INDEFINITE);
            liveTimer.play();
        });

        HBox btnRow = new HBox(8, setupBtn, stepBtn, autoBtn);
        HBox.setHgrow(setupBtn, Priority.ALWAYS);
        HBox.setHgrow(stepBtn,  Priority.ALWAYS);
        HBox.setHgrow(autoBtn,  Priority.ALWAYS);

        root.getChildren().addAll(
            title("Simulation"), btnRow, statusCard,
            lbl("Log:"), log,
            back(ev -> { stopTimer(); showRoleSelectionScene(); })
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle(BG);
        window.setScene(new Scene(scroll, 600, 720));
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private VBox centered(VBox card) {
        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle(BG);
        root.setPadding(new Insets(30));
        return root;
    }

    private void show(VBox root, double w, double h) {
        window.setScene(new Scene(root, w, h));
        window.show();
    }

    private VBox card() {
        VBox c = new VBox(10);
        c.setMaxWidth(400);
        c.setStyle(CARD);
        c.setEffect(createShadow());
        return c;
    }

    private Label title(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        return l;
    }

    private Label bold(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        return l;
    }

    private Label sub(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");
        return l;
    }

    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 12px;");
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
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private ComboBox<String> combo(List<String> items, String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setPromptText(prompt);
        cb.setStyle(FIELD);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private Button btn(String text, String style,
                       javafx.event.EventHandler<javafx.event.ActionEvent> h) {
        Button b = new Button(text);
        b.setStyle(style);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(38);
        if (h != null) b.setOnAction(h);
        return b;
    }

    private Button back(javafx.event.EventHandler<javafx.event.ActionEvent> h) {
        Button b = new Button("← Zurück");
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        b.setOnAction(h);
        return b;
    }

    private Station station(String name) {
        return Main.database.getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private void stopTimer() {
        if (liveTimer != null) { liveTimer.stop(); liveTimer = null; }
    }

    private DropShadow createShadow() {
        DropShadow s = new DropShadow();
        s.setColor(Color.rgb(0, 0, 0, 0.08));
        s.setRadius(12);
        s.setOffsetY(4);
        return s;
    }

    public static void main(String[] args) { launch(args); }
}
