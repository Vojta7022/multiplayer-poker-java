package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

import cz.cvut.fel.pjv.mosteji1.poker.ClientMain;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.PlayerRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation.TableRepresentation;
import cz.cvut.fel.pjv.mosteji1.poker.client.network.ClientEndpoint;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ChatMessage;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import cz.cvut.fel.pjv.mosteji1.poker.utils.MyUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.mosteji1.poker.utils.MyUtils.getSpriteIndex;

/**
 * Represents the poker table view in the client application.
 * <p>
 * This class is responsible for displaying the poker table, including player avatars,
 * community cards, action buttons, and chat functionality.
 * </p>
 */
public class PokerTableView extends BorderPane {

    private static final Logger logger = Logger.getLogger(PokerTableView.class.getName());

    // Components of the poker table view
    private final HBox playersPane;
    private final HBox communityCards;
    private final HBox playerCards;
    private final Label potSizeBox;
    public final TextFlow chatArea;
    public final TextField chatInput;

    // Action buttons
    private final Button foldButton;
    private final Button checkButton;
    private final Button callButton;
    private final Button raiseButton;
    private final Button allInButton;

    // Raise slider for increasing the bet
    private final Slider raiseSlider;
    private final Label raiseValueLabel;
    private final boolean[] raiseMode = {false};
    private int maxSliderValue = GameParameters.STARTING_CHIPS - 1;

    /**
     * Constructs the PokerTableView and initializes all graphical components.
     * Sets up layout, styles, and event handlers for action buttons.
     * Also spawns a thread to continuously receive updates from the server.
     *
     * @param clientEndpoint The network client used for communication with the server.
     */
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

        Button cancelRaiseButton = new Button("Cancel Raise");
        cancelRaiseButton.setVisible(false);
        cancelRaiseButton.getStyleClass().add("fancy-button");

        raiseSlider = new Slider(1, maxSliderValue, 50); // low, high, initial value
        raiseSlider.setShowTickLabels(true);
        raiseSlider.setShowTickMarks(true);
        raiseSlider.setMajorTickUnit(100);
        raiseSlider.setBlockIncrement(10);
        raiseSlider.setPrefWidth(300);
        raiseSlider.setVisible(false); // hidden by default

        raiseValueLabel = new Label("Raise: $50");
        raiseValueLabel.setStyle("-fx-text-fill: #C8E6C9; -fx-font-size: 16px;");
        raiseValueLabel.setVisible(false);

        raiseSlider.valueProperty().addListener((_, _, newVal) -> {
            int raiseValue = newVal.intValue();
            raiseValueLabel.setText("Raise: $" + raiseValue);
        });

        foldButton.setOnAction(_ -> {
            hideRaiseSlider();
            clientEndpoint.sendMessage("FOLD");
        });
        checkButton.setOnAction(_ -> {
            hideRaiseSlider();
            clientEndpoint.sendMessage("CHECK");
        });
        raiseButton.setOnAction(_ -> {
            hideRaiseSlider();
            clientEndpoint.sendMessage("RAISE");
        });
        callButton.setOnAction(_ -> {
            hideRaiseSlider();
            clientEndpoint.sendMessage("CALL");
        });
        allInButton.setOnAction(_ -> {
            hideRaiseSlider();
            clientEndpoint.sendMessage("ALLIN");
        });

        raiseButton.setOnAction(_ -> {
            if (!raiseMode[0]) {
                // First click - show slider
                raiseSlider.setVisible(true);
                raiseValueLabel.setVisible(true);
                cancelRaiseButton.setVisible(true);
                raiseButton.setText("Confirm Raise");
                raiseMode[0] = true;
            } else {
                // Second click - send raise amount
                int raiseAmount = (int) raiseSlider.getValue();
                clientEndpoint.sendMessage("RAISE " + raiseAmount);
                raiseSlider.setVisible(false);
                raiseValueLabel.setVisible(false);
                cancelRaiseButton.setVisible(false);
                raiseButton.setText("Raise");
                raiseMode[0] = false;
            }
        });

        cancelRaiseButton.setOnAction(_ -> {
            cancelRaiseButton.setVisible(false);
            hideRaiseSlider();
        });

        foldButton.getStyleClass().add("fancy-button");
        checkButton.getStyleClass().add("fancy-button");
        raiseButton.getStyleClass().add("fancy-button");
        callButton.getStyleClass().add("fancy-button");
        allInButton.getStyleClass().add("fancy-button");
        raiseSlider.getStyleClass().add("raise-slider");
        raiseValueLabel.getStyleClass().add("raise-label");

        HBox actionButtons = new HBox(20, foldButton, checkButton, callButton, raiseButton, allInButton, cancelRaiseButton);
        actionButtons.setAlignment(Pos.CENTER);

        playerCards = new HBox(10,
                createCardPlaceholder(false),
                createCardPlaceholder(false)
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
            communityCards.getChildren().add(createCardPlaceholder(false));
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

        chatArea = new TextFlow();
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
                        logger.info("Received table representation from server");
                        Platform.runLater(() -> updateView(tr));
                    } catch (IOException | ClassNotFoundException e) {
                        logger.severe("Error reading table representation from server: " + e.getMessage());
                        break;
                    }
                }
            } catch (IOException e) {
                logger.severe("Error reading table representation from server: " + e.getMessage());
            }
        });

        acceptUpdates.start();
    }

    // Sends a chat message to the server
    private void sendChatMessage(ClientEndpoint clientEndpoint) {
        String message = chatInput.getText();
        if (!message.isEmpty()) {
            message = "CHAT " + message;
            clientEndpoint.sendMessage(message);
        }
        chatInput.clear();
    }

    // Creates a placeholder for a card image
    private VBox createCardPlaceholder(boolean isGameStarted) {
        VBox box = new VBox();
        ImageView card;
        if (isGameStarted) {
            card = new ImageView(ClientMain.sprites.get(getSpriteIndex(MyUtils.Sprites.CARD_BACK)));
        } else {
            card = new ImageView(ClientMain.sprites.get(getSpriteIndex(MyUtils.Sprites.CARD_PLACEHOLDER)));
        }
        card.setFitWidth(130);
        card.setFitHeight(195);
        box.getChildren().add(card);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Updates the entire view based on the latest table state received from the server.
     * This includes player boxes, community cards, player cards, pot size, chat messages,
     * and enabling/disabling buttons according to the player's turn and available chips.
     *
     * @param tableRepresentation The current game state.
     */
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

            String labelText = playerRepresentation.name();
            if (tableRepresentation.getWaitingForIndex() == tableRepresentation.getPlayers().indexOf(playerRepresentation)) {
                labelText += "'s turn";
            } else if (tableRepresentation.getDealerIndex() == tableRepresentation.getPlayers().indexOf(playerRepresentation)) {
                labelText += " (Dealer)";
            }

            Label name = new Label(labelText);

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

        for (int i = 0; i < tableRepresentation.getCommunityCards().size(); i++) {
            ImageView card = new ImageView(ClientMain.sprites.get(getSpriteIndex(tableRepresentation.getCommunityCards().get(i))));
            card.setFitWidth(130);
            card.setFitHeight(195);

            communityCards.getChildren().add(card);
        }
        // Add placeholders for remaining community cards
        for (int i = tableRepresentation.getCommunityCards().size(); i < 5; i++) {
            communityCards.getChildren().add(createCardPlaceholder(tableRepresentation.isGameStarted()));
        }

        playerCards.getChildren().clear();

        for (Card card : tableRepresentation.getMyHand()) {
            ImageView cardView = new ImageView(ClientMain.sprites.get(getSpriteIndex(card)));
            cardView.setFitWidth(130);
            cardView.setFitHeight(195);
            playerCards.getChildren().add(cardView);
        }

        potSizeBox.setText("Pot: $" + tableRepresentation.getPotSize() +
                " | Bet Threshold: $" + tableRepresentation.getBetThreshold());

        // Chat messages
        updateChatArea(tableRepresentation.getChatMessages());

        // Slider values

        PlayerRepresentation you = tableRepresentation.getPlayers().get(tableRepresentation.getYourIndex());

        maxSliderValue = you.chips() - (tableRepresentation.getBetThreshold() - you.bet()) - 1;

        raiseSlider.setMax(maxSliderValue);
        raiseSlider.setValue(Math.min(maxSliderValue, raiseSlider.getValue()));

        // Enable/Disable Buttons
        boolean isYourTurn = tableRepresentation.getYourIndex() == tableRepresentation.getWaitingForIndex();
        foldButton.setDisable(!isYourTurn);
        checkButton.setDisable(!isYourTurn || tableRepresentation.getBetThreshold() != you.bet());
        callButton.setDisable(!isYourTurn || tableRepresentation.getBetThreshold() <= you.bet());
        raiseButton.setDisable(!isYourTurn || you.chips() <= tableRepresentation.getBetThreshold());
        allInButton.setDisable(!isYourTurn || you.chips() <= 0);
    }

    // Updates the chat area with all messages received from the server
    private void updateChatArea(List<ChatMessage> messages) {
        Platform.runLater(() -> {
            chatArea.getChildren().clear();
            for (ChatMessage message : messages) {
                Text textNode = new Text(message.text() + "\n");
                if (message.isBold()) {
                    textNode.setStyle("-fx-font-weight: bold;");
                }
                chatArea.getChildren().add(textNode);
            }
        });
    }

    // Hides raise slider when clicked on another action button
    private void hideRaiseSlider() {
        raiseSlider.setVisible(false);
        raiseValueLabel.setVisible(false);
        raiseButton.setText("Raise");
        raiseMode[0] = false;
    }
}
