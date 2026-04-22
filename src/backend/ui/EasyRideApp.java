package ui; // Muss exakt so heißen wie in der Main.java

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import model.Passenger;


public class EasyRideApp extends Application {

    private Stage window;

    // --- FARBEN & STYLES ---
    private final String bgColor = "-fx-background-color: #F8F9FA;";
    private final String cardStyle = "-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 30px;";
    private final String btnPrimaryStyle = "-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String btnSecondaryStyle = "-fx-background-color: #E9ECEF; -fx-text-fill: #212529; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String textFieldStyle = "-fx-background-color: #F1F3F5; -fx-background-radius: 8px; -fx-padding: 12px; -fx-font-size: 14px;";
    private final String errorTextStyle = "-fx-text-fill: #E74C3C; -fx-font-size: 13px; -fx-font-weight: bold;";

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        this.window.setTitle("EasyRide - Smart Mobility");
        showRoleSelectionScene();
    }

    /**
     * SZENE 1: Rollenauswahl
     */
    private void showRoleSelectionScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        Label title = new Label("Willkommen");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Wähle deine Rolle, um zu starten.");
        subtitle.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 14px;");

        Button customerBtn = new Button("Ich bin Kunde");
        customerBtn.setStyle(btnPrimaryStyle);
        customerBtn.setPrefSize(250, 45);
        customerBtn.setOnAction(e -> showCustomerAuthScene());

        Button driverBtn = new Button("Zum Dashboard (Fahrer)");
        driverBtn.setStyle(btnSecondaryStyle);
        driverBtn.setPrefSize(250, 45);
        driverBtn.setOnAction(e -> showDriverScene());

        card.getChildren().addAll(title, subtitle, customerBtn, driverBtn);
        mainLayout.getChildren().add(card);

        window.setScene(new Scene(mainLayout, 500, 700));
        window.show();
    }

    /**
     * SZENE 2: Login oder Registrierungsauswahl
     */
    private void showCustomerAuthScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        Label title = new Label("Kunden-Bereich");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Button loginBtn = new Button("Einloggen (Mockup)");
        loginBtn.setStyle(btnPrimaryStyle);
        loginBtn.setPrefSize(350, 45);
        loginBtn.setOnAction(e -> showCustomerScene()); // Simulierter Login geht direkt weiter

        Label divider = new Label("--- ODER ---");
        divider.setStyle("-fx-text-fill: #ADB5BD; -fx-font-size: 12px;");

        Button registerBtn = new Button("Neu registrieren");
        registerBtn.setStyle(btnSecondaryStyle);
        registerBtn.setPrefSize(350, 45);
        registerBtn.setOnAction(e -> showRegistrationScene()); // <--- Geht zur neuen Registrierungs-Szene!

        Button backBtn = new Button("Zurück");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showRoleSelectionScene());

        card.getChildren().addAll(title, loginBtn, divider, registerBtn);
        mainLayout.getChildren().addAll(card, backBtn);

        window.setScene(new Scene(mainLayout, 500, 700));
    }

    /**
     * SZENE 2.5 (NEU): Die echte Registrierungs-Logik
     */
    private void showRegistrationScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        Label title = new Label("Konto erstellen");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        TextField emailField = new TextField();
        emailField.setPromptText("Deine E-Mail Adresse");
        emailField.setStyle(textFieldStyle);

        // Das rote Label für Fehler (Startet leer)
        Label errorLabel = new Label("");
        errorLabel.setStyle(errorTextStyle);

        Button registerBtn = new Button("Registrieren");
        registerBtn.setStyle(btnPrimaryStyle);
        registerBtn.setPrefSize(350, 45);

        // --- DIE DATENBANK-LOGIK ---
        registerBtn.setOnAction(e -> {
            String inputEmail = emailField.getText().trim();

            if (inputEmail.isEmpty()) {
                errorLabel.setText("Bitte eine E-Mail eingeben!");
                return; // Bricht hier ab
            }

            // 1. Prüfen, ob die E-Mail schon existiert
            boolean emailExists = false;
            for (Passenger p : Main.database.getRegisteredPassengers()) {
                // Wir tun für den MVP so, als wäre der Name in der Passenger-Klasse die E-Mail
                if (p.getName().equalsIgnoreCase(inputEmail)) {
                    emailExists = true;
                    break;
                }
            }

            // 2. Entscheidung treffen
            if (emailExists) {
                // Fehler: Abweisen!
                errorLabel.setText("Fehler: Diese E-Mail existiert bereits!");
            } else {
                // Erfolg: Neuen Passenger anlegen und in den Store packen!
                // Wir erzeugen eine neue ID basierend auf der Listen-Größe
                int newId = Main.database.getRegisteredPassengers().size() + 1;
                Passenger neuerKunde = new Passenger(newId, inputEmail);

                Main.database.addPassenger(neuerKunde);
                System.out.println("Neuer Kunde registriert: " + inputEmail); // Log in der Konsole

                // Weiter zur Buchung!
                showCustomerScene();
            }
        });

        Button backBtn = new Button("Abbrechen");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showCustomerAuthScene());

        card.getChildren().addAll(title, emailField, errorLabel, registerBtn);
        mainLayout.getChildren().addAll(card, backBtn);

        window.setScene(new Scene(mainLayout, 500, 700));
    }

    /**
     * SZENE 3: Kunden-Ansicht (Buchen)
     */
    private void showCustomerScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        Label title = new Label("Wohin soll's gehen?");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        TextField startField = new TextField();
        startField.setPromptText("Startpunkt eingeben...");
        startField.setStyle(textFieldStyle);

        TextField zielField = new TextField();
        zielField.setPromptText("Zielpunkt eingeben...");
        zielField.setStyle(textFieldStyle);

        Button bookBtn = new Button("Jetzt buchen");
        bookBtn.setStyle(btnPrimaryStyle);
        bookBtn.setPrefSize(350, 45);

        Button backBtn = new Button("Abmelden");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showCustomerAuthScene());

        card.getChildren().addAll(title, new Label("Start"), startField, new Label("Ziel"), zielField, bookBtn);
        mainLayout.getChildren().addAll(card, backBtn);

        window.setScene(new Scene(mainLayout, 500, 700));
    }

    /**
     * SZENE 4: Fahrer-Ansicht (Tablet)
     */
    private void showDriverScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        Label title = new Label("Fahrer Tablet");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #E9ECEF; -fx-padding: 15px; -fx-background-radius: 8px;");
        infoBox.getChildren().addAll(
                new Label("Nächster Halt: Hauptbahnhof"),
                new Label("Einsteiger: Max Mustermann"),
                new Label("Aussteiger: Keine")
        );

        Button confirmBtn = new Button("Stopp bestätigen");
        confirmBtn.setStyle(btnPrimaryStyle);
        confirmBtn.setPrefSize(350, 45);

        Button backBtn = new Button("Schicht beenden");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showRoleSelectionScene());

        card.getChildren().addAll(title, infoBox, confirmBtn);
        mainLayout.getChildren().addAll(card, backBtn);

        window.setScene(new Scene(mainLayout, 500, 700));
    }

    private DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(15);
        shadow.setOffsetY(5);
        return shadow;
    }

    public static void main(String[] args) {
        launch(args);
    }
}