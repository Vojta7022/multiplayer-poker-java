package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code Deck} class represents a standard deck of playing cards.
 * It contains methods to shuffle the deck and deal cards.
 * <p>
 * The deck is initialized with 52 unique cards, each representing a combination
 * of rank and suit. The deck can be shuffled to randomize the order of the cards.
 */
public class Deck {
    // List to hold the cards in the deck
    private final List<Card> cards = new ArrayList<>();

    /**
     * Constructs a new {@code Deck} object and initializes it with 52 unique cards.
     * The deck is shuffled upon creation.
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        this.shuffle();

    }

    /**
     * Shuffles the deck of cards.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals a card from the top of the deck.
     *
     * @return the dealt card, or null if the deck is empty
     */
    public Card dealCard() {
        return cards.isEmpty() ? null : cards.removeFirst();
    }
}
