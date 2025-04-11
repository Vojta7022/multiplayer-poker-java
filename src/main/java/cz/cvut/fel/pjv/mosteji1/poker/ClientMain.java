package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.resources.graphics.PokerTableView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class ClientMain extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showMenuScene();
    }

    private void showMenuScene() {
        VBox menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(50));

        TextField ipField = new TextField();
        ipField.setPromptText("IP adresa");

        TextField portField = new TextField();
        portField.setPromptText("Port");

        Button connectButton = new Button("Připojit se");

        Label statusLabel = new Label();

        connectButton.setOnAction(e -> {
            String ip = ipField.getText();
            String portStr = portField.getText();

            // TODO: Zkusit navázat spojení
            boolean connected = tryConnect(ip, portStr);

            if (connected) {
                showPokerTableScene();
            } else {
                statusLabel.setText("Nepodařilo se připojit.");
            }
        });

        menuBox.getChildren().addAll(ipField, portField, connectButton, statusLabel);
        Scene menuScene = new Scene(menuBox, 600, 400);
        primaryStage.setTitle("Poker Client - Připojení");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private boolean tryConnect(String ip, String portStr) {
        // TODO: Reálná logika připojení přes socket
        try {
            int port = Integer.parseInt(portStr);
            System.out.println("Připojuji se na IP: " + ip + ", port: " + port);
            // Simulace úspěchu
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showPokerTableScene() {
        PokerTableView tableView = new PokerTableView();
        Scene tableScene = new Scene(tableView);
        primaryStage.setScene(tableScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
