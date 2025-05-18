package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import cz.cvut.fel.pjv.mosteji1.poker.common.game.GameParameters;
import javafx.util.Pair;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents the main menu view of the Poker Texas Hold'em client application.
 * <p>
 * This view allows the user to enter the IP address, port, player name, and select an avatar,
 * as well as connect to the server or load saved game configuration from a JSON file.
 * </p>
 *
 * Components:
 * <ul>
 *     <li>Text fields for IP, port, and player name</li>
 *     <li>ComboBox for avatar selection with preview images</li>
 *     <li>Buttons for connecting to a game or loading saved data</li>
 *     <li>Status label for showing feedback</li>
 * </ul>
 *
 * Styles and layout are applied using CSS classes and background image.
 */
public class MenuView extends VBox {

    private static final Logger logger = Logger.getLogger(MenuView.class.getName());

    /** Button to connect to the game server. */
    public final Button connectButton;
    /** Button to load game configuration from a file. */
    public final Button loadButton;
    /** Text field for entering the server's IP address. */
    public static final TextField ipField = new TextField();
    /** Text field for entering the server's port. */
    public static final TextField portField = new TextField();
    /** Text field for entering the player's name. */
    public static final TextField playerNameField = new TextField();
    /** Label displaying connection or loading status. */
    public static final Label statusLabel = new Label();
    /** ComboBox for selecting an avatar, shown with images. */
    public static final ComboBox<Pair<String, Image>> avatarComboBox = new ComboBox<>();

    /**
     * Constructs a new instance of {@code MenuView}, setting up all GUI elements,
     * including background, layout, input fields, buttons, and avatar selection.
     */
    public MenuView() {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image("/menu_background.png", 900, 600, false, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );

        setAlignment(Pos.CENTER);
        setPrefSize(900, 600);
        setPadding(new Insets(50));

        Text welcome = new Text("POKER TEXAS HOLD'EM");
        welcome.getStyleClass().add("welcome-text");

        ipField.setPromptText("IP address");
        ipField.setMaxWidth(250);
        ipField.getStyleClass().add("menu-text-field");

        portField.setPromptText("Port");
        portField.setMaxWidth(250);
        portField.getStyleClass().add("menu-text-field");

        playerNameField.setPromptText("Player name");
        playerNameField.setMaxWidth(250);
        playerNameField.getStyleClass().add("menu-text-field");

        connectButton = new Button("Connect");
        connectButton.getStyleClass().add("fancy-button");

        loadButton = new Button("Load Game Data");
        loadButton.getStyleClass().add("fancy-button");
        loadButton.setOnAction(_ -> loadGameData());

        avatarComboBox.setPrefWidth(250);
        avatarComboBox.getStyleClass().add("avatar-combobox");
        avatarComboBox.setPromptText("Select avatar");

        ObservableList<Pair<String, Image>> avatars = FXCollections.observableArrayList();
        for (int i = 0; i < GameParameters.AVATAR_COUNT; i++) {
            String name = "Avatar " + (i + 1);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/avatars/" + (i + 1) + ".png")));
            avatars.add(new Pair<>(name, image));
        }
        avatarComboBox.setItems(avatars);

        avatarComboBox.setCellFactory(_ -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Pair<String, Image> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    imageView.setImage(item.getValue());
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);
                    setGraphic(imageView);
                    setText(item.getKey());
                }
            }
        });

        avatarComboBox.setButtonCell(new ListCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Pair<String, Image> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    imageView.setImage(item.getValue());
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);
                    setGraphic(imageView);
                    setText(item.getKey());
                }
            }
        });


        avatarComboBox.setButtonCell(avatarComboBox.getCellFactory().call(null));

        setBackground(new Background(backgroundImage));
        getChildren().addAll(welcome, ipField, portField, playerNameField, avatarComboBox, connectButton, loadButton, statusLabel);
    }

    /**
     * Loads saved game configuration (IP, port, player name, avatar index) from
     * a JSON file located in the resource folder under the name {@code game_data.json}.
     * <p>
     * If successful, updates the corresponding input fields and avatar selection.
     * Displays success or error message in the {@code statusLabel}.
     * </p>
     */
    private void loadGameData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Load the file from the resources folder
            File file = new File(Objects.requireNonNull(getClass().getResource("/game_data.json")).toURI());

            // Read the JSON data
            Map<String, String> data = objectMapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<>() {});

            ipField.setText(data.getOrDefault("ip", ""));
            portField.setText(data.getOrDefault("port", ""));
            playerNameField.setText(data.getOrDefault("playerName", ""));

            String avatarIndexStr = data.get("avatarIndex");
            if (avatarIndexStr != null) {
                int avatarIndex = Integer.parseInt(avatarIndexStr);
                if (avatarIndex >= 0 && avatarIndex < avatarComboBox.getItems().size()) {
                    avatarComboBox.getSelectionModel().select(avatarIndex);
                }
            }

            statusLabel.setText("Values loaded successfully.");
        } catch (Exception e) {
            statusLabel.setText("Error loading data.");
            logger.severe("Error loading game data: " + e.getMessage());
        }
    }
}
