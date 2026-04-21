package easyride.ui;

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

public class EasyRideApp extends Application {

    private Stage window;

    // --- FARBEN & STYLES ---
    private final String bgColor = "-fx-background-color: #F8F9FA;"; // Helles Grau/Weiß
    private final String cardStyle = "-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 30px;";
    private final String btnPrimaryStyle = "-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String btnSecondaryStyle = "-fx-background-color: #E9ECEF; -fx-text-fill: #212529; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;";
    private final String textFieldStyle = "-fx-background-color: #F1F3F5; -fx-background-radius: 8px; -fx-padding: 12px; -fx-font-size: 14px;";

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        this.window.setTitle("EasyRide - Smart Mobility");

        // Startbildschirm aufrufen
        showRoleSelectionScene();
    }

    //Startbildschirm (Auswahl Kunde oder Fahrer)
    private void showRoleSelectionScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(bgColor);

        // Die "Karte" in der Mitte
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(350);
        card.setStyle(cardStyle);
        card.setEffect(createShadow());

        // Titel
        Label title = new Label("Willkommen");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Wähle deine Rolle, um zu starten.");
        subtitle.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 14px;");

        // Buttons
        Button customerBtn = new Button("Fahrt buchen (Kunde)");
        customerBtn.setStyle(btnPrimaryStyle);
        customerBtn.setPrefSize(250, 45);
        customerBtn.setOnAction(e -> showCustomerScene());

        Button driverBtn = new Button("Zum Dashboard (Fahrer)");
        driverBtn.setStyle(btnSecondaryStyle);
        driverBtn.setPrefSize(250, 45);
        driverBtn.setOnAction(e -> showDriverScene());

        card.getChildren().addAll(title, subtitle, customerBtn, driverBtn);
        mainLayout.getChildren().add(card);

        Scene scene = new Scene(mainLayout, 500, 700);
        window.setScene(scene);
        window.show();
    }

    //Kunden-Ansicht (Buchen)
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

        // Eingabefelder
        TextField startField = new TextField();
        startField.setPromptText("Startpunkt eingeben...");
        startField.setStyle(textFieldStyle);

        TextField zielField = new TextField();
        zielField.setPromptText("Zielpunkt eingeben...");
        zielField.setStyle(textFieldStyle);

        // Buchen Button
        Button bookBtn = new Button("Jetzt buchen");
        bookBtn.setStyle(btnPrimaryStyle);
        bookBtn.setPrefSize(350, 45); // Nimmt die ganze Breite der Karte ein

        // Zurück Button
        Button backBtn = new Button("Zurück");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6C757D; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showRoleSelectionScene());

        card.getChildren().addAll(title, new Label("Start"), startField, new Label("Ziel"), zielField, bookBtn);
        mainLayout.getChildren().addAll(card, backBtn);

        window.setScene(new Scene(mainLayout, 500, 700));
    }

    //Fahrer-Ansicht (Tablet)
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

        // Mock-Up für die Routen-Info
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

    // Hilfsmethode für einen weichen, modernen Schatten hinter den Karten
    private DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1)); // Sehr helles schwarz (transparent)
        shadow.setRadius(15);
        shadow.setOffsetY(5);
        return shadow;
    }

    // --- DER STARTPUNKT ---
    public static void main(String[] args) {
        launch(args);
    }
}