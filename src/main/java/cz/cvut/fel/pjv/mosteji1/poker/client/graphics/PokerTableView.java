package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

import cz.cvut.fel.pjv.mosteji1.poker.ClientMain;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.PlayerRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.ObjectInputStream;
import java.util.Random;

import static cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils.getSpriteIndex;

public class PokerTableView extends BorderPane {

    private final HBox playersPane;

    public PokerTableView(ClientEndpoint clientEndpoint) {

        BackgroundImage bg = new BackgroundImage(
                new Image("/table_background.jpg", 1920, 1080, false, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );

        setPrefSize(1920, 1080);
        setBackground(new Background(bg));

        // Bottom: action buttons and player cards
        Button foldButton = new Button("Fold");
        Button checkButton = new Button("Check");
        Button raiseButton = new Button("Raise");

        foldButton.getStyleClass().add("fancy-button");
        checkButton.getStyleClass().add("fancy-button");
        raiseButton.getStyleClass().add("fancy-button");

        HBox actionButtons = new HBox(20, foldButton, checkButton, raiseButton);
        actionButtons.setAlignment(Pos.CENTER);

        HBox playerCards = new HBox(10,
                createCardPlaceholder(),
                createCardPlaceholder()
        );
        playerCards.setAlignment(Pos.CENTER);
        playerCards.setPadding(new Insets(20,20,20,20));

        VBox bottomSection = new VBox(10, actionButtons, playerCards);
        bottomSection.setAlignment(Pos.CENTER);
        setBottom(bottomSection);

        // Middle: community cards
        HBox communityCards = new HBox(20);
        for (int i = 1; i <= 5; i++) {
            communityCards.getChildren().add(createCardPlaceholder());
        }
        communityCards.setAlignment(Pos.CENTER);
        setCenter(communityCards);


        // Other players
        playersPane = new HBox();
        playersPane.setAlignment(Pos.TOP_CENTER);
        playersPane.setSpacing(40);
        playersPane.setPadding(new Insets(20, 0, 0, 0));
        setTop(playersPane);

        Thread acceptUpdates = new Thread(() -> {
            while (!clientEndpoint.isClosed()) {
                try {
                    ObjectInputStream in = new ObjectInputStream(clientEndpoint.getSocket().getInputStream());
                    TableRepresentation tr = (TableRepresentation) in.readObject();

                    updateView(tr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        acceptUpdates.start();

    }

    private VBox createCardPlaceholder() {
        VBox box = new VBox();
        ImageView card = new ImageView(ClientMain.sprites.get(getSpriteIndex(MyUtils.Sprites.CARD_PLACEHOLDER)));
        card.setFitWidth(100);
        card.setFitHeight(150);
        box.getChildren().add(card);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public void updateView(TableRepresentation tableRepresentation) {
        playersPane.getChildren().clear();      // AllIn, Folded

        for (PlayerRepresentation playerRepresentation : tableRepresentation.getPlayers()) {
            VBox playerBox = new VBox(5);

            Image avatarImg = ClientMain.avatars.get(playerRepresentation.avatarIndex());
            ImageView avatarView = new ImageView(avatarImg);
            avatarView.setFitWidth(60);
            avatarView.setFitHeight(60);
            avatarView.setClip(new Circle(30, 30, 30));

            Label name = new Label(playerRepresentation.name());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

            Label money = new Label("$" + playerRepresentation.chips());
            money.setStyle("-fx-font-size: 12px; -fx-text-fill: #C8E6C9;");

            playerBox.getChildren().addAll(avatarView, name, money);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPadding(new Insets(10));
            playerBox.getStyleClass().add("player-box");

            playersPane.getChildren().add(playerBox);

            // Community Cards, Hole cards, PotSize, Threshold, Dealer, WaitingFor

        }
    }
}
