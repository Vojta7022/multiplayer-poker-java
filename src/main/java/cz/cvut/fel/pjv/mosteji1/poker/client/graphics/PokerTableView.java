package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

import cz.cvut.fel.pjv.mosteji1.poker.ClientMain;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.PlayerRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Rank;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Suit;
import cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils.getSpriteIndex;

public class PokerTableView extends BorderPane {

    private final HBox playersPane;
    private final HBox communityCards;
    private final HBox playerCards;
    private final Label potSizeBox;

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

        playerCards = new HBox(10,
                createCardPlaceholder(),
                createCardPlaceholder()
        );
        playerCards.setAlignment(Pos.CENTER);
        playerCards.setPadding(new Insets(20,20,20,20));

        potSizeBox = new Label("Pot: $0");
        potSizeBox.setStyle("-fx-font-size: 20px; -fx-text-fill: #C8E6C9;");

        VBox bottomSection = new VBox(10, actionButtons, playerCards, potSizeBox);
        bottomSection.setAlignment(Pos.CENTER);
        setBottom(bottomSection);

        // Middle: community cards
        communityCards = new HBox(20);
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
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        TableRepresentation tr = new TableRepresentation();
        tr.setGameStarted(false);
        tr.setDealerIndex(0);
        tr.setWaitingForIndex(0);
        tr.setPotSize(0);
        tr.setBetThreshold(0);

        tr.addPlayer("Láďa", 0, 1000, 0, false, false);
        tr.addPlayer("Vojta", 1, 1000, 0, false, false);
        tr.addPlayer("Jirka", 2, 1000, 0, false, false);

        ArrayList<Card> communityCards = new ArrayList<Card>();
        communityCards.add(new Card(Rank.ACE, Suit.DIAMONDS));
        communityCards.add(new Card(Rank.KING, Suit.DIAMONDS));
        communityCards.add(new Card(Rank.QUEEN, Suit.DIAMONDS));
        communityCards.add(new Card(Rank.JACK, Suit.DIAMONDS));
        communityCards.add(new Card(Rank.TEN, Suit.DIAMONDS));

        tr.setMyHand(new ArrayList<>());
        tr.getMyHand().add(new Card(Rank.ACE, Suit.SPADES));
        tr.getMyHand().add(new Card(Rank.KING, Suit.SPADES));

        tr.setCommunityCards(communityCards);

        updateView(tr);

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
        playersPane.getChildren().clear();


        // UPDATE PLAYERS

        for (PlayerRepresentation playerRepresentation : tableRepresentation.getPlayers()) {
            VBox playerBox = new VBox(5);

            Image avatarImg = ClientMain.avatars.get(playerRepresentation.avatarIndex());
            ImageView avatarView = new ImageView(avatarImg);
            avatarView.setFitWidth(60);
            avatarView.setFitHeight(60);
            avatarView.setClip(new Circle(30, 30, 30));

            Label name = new Label( tableRepresentation.getPlayers().indexOf(playerRepresentation)
                == tableRepresentation.getWaitingForIndex() ?
                "Your turn: " + playerRepresentation.name() :
                ( tableRepresentation.getPlayers().indexOf(playerRepresentation) == tableRepresentation.getDealerIndex() ?
                playerRepresentation.name() + " (Dealer)" :
                playerRepresentation.name() )

            );

            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

            Label money = new Label("$" + playerRepresentation.chips());
            money.setStyle("-fx-font-size: 12px; -fx-text-fill: #C8E6C9;");
            if (playerRepresentation.folded()) {    // folded representation
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF5252;");
                money.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF5252;");
            }

            Label allInLabel = new Label("All In!");
            allInLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f3cc31;");

            playerBox.getChildren().addAll(avatarView, name, money);
            if (playerRepresentation.isAllIn()) playerBox.getChildren().add(allInLabel);



            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPadding(new Insets(10));
            playerBox.getStyleClass().add("player-box");

            playersPane.getChildren().add(playerBox);

            communityCards.getChildren().clear();
            for (int i = 0; i < tableRepresentation.getCommunityCards().size(); i++) {
                ImageView card = new ImageView(ClientMain.sprites.get(getSpriteIndex(tableRepresentation.getCommunityCards().get(i))));
                card.setFitWidth(100);
                card.setFitHeight(150);

                communityCards.getChildren().add(card);
            }

            playerCards.getChildren().clear();

            for (Card card : tableRepresentation.getMyHand()) {
                ImageView cardView = new ImageView(ClientMain.sprites.get(getSpriteIndex(card)));
                cardView.setFitWidth(100);
                cardView.setFitHeight(150);
                playerCards.getChildren().add(cardView);
            }

            potSizeBox.setText("Pot: $" + tableRepresentation.getPotSize() +
                    " | Bet Threshold: $" + tableRepresentation.getBetThreshold());



            // Threshold

        }
    }
}
