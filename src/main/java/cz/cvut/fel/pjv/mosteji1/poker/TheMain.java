package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Rank;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Suit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TheMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Card myCard = new Card(Rank.FIVE, Suit.CLUBS);

        Canvas canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}