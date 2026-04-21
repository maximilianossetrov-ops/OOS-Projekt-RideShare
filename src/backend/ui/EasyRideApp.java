package ui; // Falls dein Package anders heißt, ändere diese Zeile!

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

public class EasyRideApp extends Application {

    private Stage window;

    // --- FARBEN & STYLES ---
    private final String bgColor = "-fx-background-color: #F8F9FA;";
    private final String cardStyle = "-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 30px;";
    private final String btnPrimaryStyle = "-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String btnSecondaryStyle = "-fx-background-color: #E9ECEF; -fx-text-fill: #212529; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String textFieldStyle = "-fx-background-color: #F1F3F5; -fx-background-radius: 8px; -fx-padding: 12px; -fx-font-size: 14px;";

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
        // NEU: Leitet jetzt zur Login/Register-Auswahl weiter!
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
     * SZENE 2 (NEU): Login oder Registrierung für Kunden
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

        // Wir deuten hier nur den Login an, da die Logik noch fehlt
        TextField emailField = new TextField();
        emailField.setPromptText("E-Mail Adresse");
        emailField.setStyle(textFieldStyle);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Passwort");
        passwordField.setStyle(textFieldStyle);

        Button loginBtn = new Button("Einloggen");
        loginBtn.setStyle(btnPrimaryStyle);
        loginBtn.setPrefSize(350, 45);
        loginBtn.setOnAction(e -> showCustomerScene()); // Geht zur Buchung

        Label divider = new Label("--- ODER ---");
        divider.setStyle("-fx-text-fill: #ADB5BD; -fx-font-size: 12px;");

        Button registerBtn = new Button("Neu registrieren");
        registerBtn.setStyle(btnSecondaryStyle);
        registerBtn.setPrefSize(350, 45);
        registerBtn.setOnAction(e -> showCustomerScene()); // Geht ebenfalls zur Buchung

        Button backBtn = new Button("Zurück");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showRoleSelectionScene());

        card.getChildren().addAll(title, emailField, passwordField, loginBtn, divider, registerBtn);
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

        Button backBtn = new Button("Abmelden / Zurück");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        // Der Zurück-Button hier geht jetzt sinnvollerweise zum Auth-Screen zurück
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