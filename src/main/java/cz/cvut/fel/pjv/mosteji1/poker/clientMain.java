package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;


public class clientMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Card myCard = new Card(Rank.FIVE, Suit.CLUBS);
        Deck myDeck = new Deck();
        Card[] cardSeptuple = new Card[] {myDeck.dealCard(), myDeck.dealCard(), myDeck.dealCard(), myDeck.dealCard(), myDeck.dealCard(), myDeck.dealCard(), myDeck.dealCard()};
        CardCombo cardCombo = new CardCombo(cardSeptuple);

        System.out.println("Cards:");
        for (Card card : cardSeptuple) {
            System.out.println(card);
        }
        System.out.println(cardCombo.getHandRanking());
        System.out.println(Arrays.toString(cardCombo.getKickers()));




        Canvas canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

