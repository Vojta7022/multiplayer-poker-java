package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.resources.graphics.PokerTableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ClientMain extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        this.primaryStage = stage;
        graphicsInitialize();
        showMenuScene();
    }

    private void showMenuScene() {

        MenuView menuView = new MenuView();

        menuView.connectButton.setOnAction(e -> {
            String ip = MenuView.ipField.getText();
            String portStr = MenuView.portField.getText();
            String playerName = MenuView.playerNameField.getText();

            // TODO: Zkusit navázat spojení
            boolean connected = tryConnect(ip, portStr);

            if (connected) {
                gameState = GameState.PLAYING;
                showTableScene();
            } else {
                MenuView.statusLabel.setText("Could not connect.");
            }
        });

        Scene menuScene = new Scene(menuView);
        menuScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setTitle("Poker Client - Connection");
        primaryStage.setScene(menuScene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    private void showPokerTableScene() {
        PokerTableView tableView = new PokerTableView();
        Scene tableScene = new Scene(tableView);
        tableScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setScene(tableScene);
        primaryStage.setTitle("Poker Client - Game");
        primaryStage.setMaximized(true);
    }

    private boolean tryConnect(String ip, String portStr) {
        // TODO: Reálná logika připojení přes socket
        try {
            myEndpoint = new ClientEndpoint(ip, Integer.parseInt(portStr));
            System.out.println("Connecting to IP: " + ip + ", port: " + portStr);



            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
