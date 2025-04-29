package cz.cvut.fel.pjv.mosteji1.poker.client.graphics;

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

import java.util.Objects;

public class MenuView extends VBox {
    public final Button connectButton;
    public static final TextField ipField = new TextField();
    public static final TextField portField = new TextField();
    public static final TextField playerNameField = new TextField();
    public static final Label statusLabel = new Label();
    public static final ComboBox<Pair<String, Image>> avatarComboBox = new ComboBox<>();


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

        avatarComboBox.setCellFactory(param -> new ListCell<>() {
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
        getChildren().addAll(welcome, ipField, portField, playerNameField, avatarComboBox, connectButton, statusLabel);
    }
}
