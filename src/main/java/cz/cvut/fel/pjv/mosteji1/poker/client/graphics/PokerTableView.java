package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

import cz.cvut.fel.pjv.mosteji1.poker.ClientMain;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.PlayerRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.ObjectInputStream;

import static cz.cvut.fel.pjv.mosteji1.poker.myUtils.MyUtils.getSpriteIndex;

public class PokerTableView extends BorderPane {

    // TODO: tlacitka zmizi/ jsou vysedla podle toho, jestli jses na tahu
    // TODO: raise slider
    // TODO: chat spravit
    // TODO: check/call podle toho jestli musis dorovnavat

    // TODO: jak funguje remíza / když je někdo all in
    //

    private final HBox playersPane;
    private final HBox communityCards;
    private final HBox playerCards;
    private final Label potSizeBox;
    public final TextArea chatArea;
    public final TextField chatInput;
    private int low, high;

    private final Button foldButton;
    private final Button checkButton;
    private final Button callButton;
    private final Button raiseButton;
    private final Button allInButton;



    public PokerTableView(ClientEndpoint clientEndpoint) {

        BackgroundImage bg = new BackgroundImage(
                new Image("/table_background.png", 0, 0, true, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(100, 100, true, true, true, true)
        );

        setPrefSize(1920, 1080);
        setBackground(new Background(bg));

        // Bottom: action buttons and player cards, slider for raising
        foldButton = new Button("Fold");
        checkButton = new Button("Check");
        callButton = new Button("Call");
        raiseButton = new Button("Raise");
        allInButton = new Button("All In!");

        final boolean[] raiseMode = {false};
        low = 1;
        high = GameParameters.STARTING_CHIPS;

        Slider raiseSlider = new Slider(low, high, 50); // low, high, initial value
        raiseSlider.setShowTickLabels(true);
        raiseSlider.setShowTickMarks(true);
        raiseSlider.setMajorTickUnit(100);
        raiseSlider.setBlockIncrement(10);
        raiseSlider.setPrefWidth(300);
        raiseSlider.setVisible(false); // hidden by default

        Label raiseValueLabel = new Label("Raise: $50");
        raiseValueLabel.setStyle("-fx-text-fill: #C8E6C9; -fx-font-size: 16px;");
        raiseValueLabel.setVisible(false);

        raiseSlider.valueProperty().addListener((_, _, newVal) -> {
            int raiseValue = newVal.intValue();
            raiseValueLabel.setText("Raise: $" + raiseValue);
        });

        foldButton.setOnAction(_ -> clientEndpoint.sendMessage("FOLD"));
        checkButton.setOnAction(_ -> clientEndpoint.sendMessage("CHECK"));
        raiseButton.setOnAction(_ -> clientEndpoint.sendMessage("RAISE"));
        callButton.setOnAction(_ -> clientEndpoint.sendMessage("CALL"));
        allInButton.setOnAction(_ -> clientEndpoint.sendMessage("ALLIN"));

        raiseButton.setOnAction(_ -> {
            if (!raiseMode[0]) {
                // First click - show slider
                raiseSlider.setVisible(true);
                raiseValueLabel.setVisible(true);
                raiseButton.setText("Confirm Raise");
                raiseMode[0] = true;
            } else {
                // Second click - send raise amount
                int raiseAmount = (int) raiseSlider.getValue();
                clientEndpoint.sendMessage("RAISE " + raiseAmount);
                raiseSlider.setVisible(false);
                raiseValueLabel.setVisible(false);
                raiseButton.setText("Raise");
                raiseMode[0] = false;
            }
        });

        foldButton.getStyleClass().add("fancy-button");
        checkButton.getStyleClass().add("fancy-button");
        raiseButton.getStyleClass().add("fancy-button");
        callButton.getStyleClass().add("fancy-button");
        allInButton.getStyleClass().add("fancy-button");
        raiseSlider.getStyleClass().add("raise-slider");
        raiseValueLabel.getStyleClass().add("raise-label");

        HBox actionButtons = new HBox(20, foldButton, checkButton, callButton, raiseButton, allInButton);
        actionButtons.setAlignment(Pos.CENTER);

        playerCards = new HBox(10,
                createCardPlaceholder(),
                createCardPlaceholder()
        );
        playerCards.setAlignment(Pos.CENTER);
        playerCards.setPadding(new Insets(20,20,20,20));

        potSizeBox = new Label("Pot: $0");
        potSizeBox.setStyle("-fx-font-size: 20px; -fx-text-fill: #C8E6C9;");

        VBox bottomSection = new VBox(10, actionButtons, raiseSlider, raiseValueLabel, playerCards, potSizeBox);
        bottomSection.setAlignment(Pos.CENTER);
        setBottom(bottomSection);

        // Middle: community cards
        communityCards = new HBox(20);
        for (int i = 1; i <= 5; i++) {
            communityCards.getChildren().add(createCardPlaceholder());
        }
        communityCards.setAlignment(Pos.CENTER);
        communityCards.setPadding(new Insets(20, 0, 10, 0));

        StackPane centerWrapper = new StackPane(communityCards);
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setCenter(centerWrapper);

        // TOP: Players
        playersPane = new HBox();
        playersPane.setAlignment(Pos.TOP_CENTER);
        playersPane.setSpacing(40);
        playersPane.setPadding(new Insets(20, 0, 0, 0));
        setTop(playersPane);

        // LEFT: Chat box
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.getStyleClass().add("chat-box");
        chatBox.setPrefWidth(250);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.getStyleClass().add("chat-area");
        chatArea.setPrefHeight(400);
        chatArea.setPrefWidth(230);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        chatInput = new TextField();
        chatInput.setPromptText("Type your message...");
        chatInput.getStyleClass().add("chat-input");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("chat-send-button");
        sendButton.setOnAction(_ -> sendChatMessage(clientEndpoint));

        HBox chatControls = new HBox(5, chatInput, sendButton);
        chatControls.setAlignment(Pos.CENTER);
        chatControls.setPadding(new Insets(5));

        chatBox.getChildren().addAll(chatArea, chatControls);
        setLeft(chatBox);

        Thread acceptUpdates = new Thread(() -> {
            try {
                ObjectInputStream in = new ObjectInputStream(clientEndpoint.getSocket().getInputStream());

                while (!clientEndpoint.isClosed()) {
                    try {
                        TableRepresentation tr = (TableRepresentation) in.readObject();
                        System.out.println("Received table representation from server");
                        System.out.println(tr);
                        System.out.println("Your hand before update: " + tr.getMyHand());
                        Platform.runLater(() -> updateView(tr));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        acceptUpdates.start();
    }

    private void sendChatMessage(ClientEndpoint clientEndpoint) {
        String message = chatInput.getText();
        if (!message.isEmpty()) {
            message = "CHAT " + message;
            clientEndpoint.sendMessage(message);
        }
        chatInput.clear();
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

            Label name = new Label(tableRepresentation.getPlayers().indexOf(playerRepresentation)
                    == tableRepresentation.getWaitingForIndex() ?
                    playerRepresentation.name() + "'s turn" :
                    (tableRepresentation.getPlayers().indexOf(playerRepresentation) == tableRepresentation.getDealerIndex() ?
                            playerRepresentation.name() + " (Dealer)" :
                            playerRepresentation.name())

            );

            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            if (tableRepresentation.getYourIndex() == tableRepresentation.getPlayers().indexOf(playerRepresentation)) {
                name.setStyle("-fx-text-fill: #FF5252;");
            } else {
                name.setStyle("-fx-text-fill: #C8E6C9;");
            }

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
        }

        communityCards.getChildren().clear();

        System.out.println("community cards: " + tableRepresentation.getCommunityCards());
        for (int i = 0; i < tableRepresentation.getCommunityCards().size(); i++) {
            ImageView card = new ImageView(ClientMain.sprites.get(getSpriteIndex(tableRepresentation.getCommunityCards().get(i))));
            card.setFitWidth(100);
            card.setFitHeight(150);

            communityCards.getChildren().add(card);
        }

        playerCards.getChildren().clear();

        System.out.println("my hand: " + tableRepresentation.getMyHand());
        for (Card card : tableRepresentation.getMyHand()) {
            ImageView cardView = new ImageView(ClientMain.sprites.get(getSpriteIndex(card)));
            cardView.setFitWidth(100);
            cardView.setFitHeight(150);
            playerCards.getChildren().add(cardView);
        }

        potSizeBox.setText("Pot: $" + tableRepresentation.getPotSize() +
                " | Bet Threshold: $" + tableRepresentation.getBetThreshold());

        // Chat messages
        chatArea.clear();
        for (String message : tableRepresentation.getChatMessages()) {
            chatArea.appendText(message + "\n");
        }

        // Slider values

        high = tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex()).chips();
        low = tableRepresentation.getBetThreshold();

        // Threshold
        // TODO: update this when the player raises

        // Enable/Disable Buttons
        boolean isYourTurn = tableRepresentation.getYourIndex() == tableRepresentation.getWaitingForIndex();
        foldButton.setDisable(!isYourTurn);
        checkButton.setDisable(!isYourTurn || tableRepresentation.getBetThreshold() != tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex()).bet());
        callButton.setDisable(!isYourTurn || tableRepresentation.getBetThreshold() <= tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex()).bet());
        raiseButton.setDisable(!isYourTurn || tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex()).chips() < tableRepresentation.getBetThreshold());
        allInButton.setDisable(!isYourTurn || tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex()).chips() <= 0);
    }
}
