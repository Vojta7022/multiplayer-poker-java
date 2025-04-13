package cz.cvut.fel.pjv.mosteji1.poker.resources.graphics;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class PokerTableView extends BorderPane {

    private HBox communityCards;
    private HBox playerCards;
    private HBox actionButtons;
    private Pane playersPane;

    public PokerTableView() {
        setPrefSize(1920, 1080);

        // Spodní část: tlačítka + naše karty
        actionButtons = new HBox(20,
                new Button("Fold"),
                new Button("Check"),
                new Button("Raise")
        );
        actionButtons.setAlignment(Pos.CENTER);

        playerCards = new HBox(10,
                createCardPlaceholder("Card 1"),
                createCardPlaceholder("Card 2")
        );
        playerCards.setAlignment(Pos.CENTER);

        VBox bottomSection = new VBox(10, actionButtons, playerCards);
        bottomSection.setAlignment(Pos.CENTER);
        setBottom(bottomSection);

        // Střed: komunitní karty
        communityCards = new HBox(20);
        for (int i = 1; i <= 5; i++) {
            communityCards.getChildren().add(createCardPlaceholder("C" + i));
        }
        communityCards.setAlignment(Pos.CENTER);
        setCenter(communityCards);

        // Hráči kolem stolu
        playersPane = new Pane();
        updatePlayers(6); // defaultních 6 hráčů
        getChildren().add(playersPane);
    }

    private VBox createCardPlaceholder(String name) {
        Label card = new Label(name);
        card.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-padding: 20; -fx-min-width: 80; -fx-alignment: center;");
        VBox box = new VBox(card);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public void updatePlayers(int totalPlayers) {
        playersPane.getChildren().clear();
        double centerX = getPrefWidth() / 2.0;
        double centerY = getPrefHeight() / 2.0;
        double radius = 400;

        for (int i = 0; i < totalPlayers; i++) {
            double angle = 2 * Math.PI * i / totalPlayers;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Label player = new Label("Hráč " + (i + 1) + "\n$1000");
            player.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 10;");
            player.setLayoutX(x);
            player.setLayoutY(y);
            playersPane.getChildren().add(player);
        }
    }
}
