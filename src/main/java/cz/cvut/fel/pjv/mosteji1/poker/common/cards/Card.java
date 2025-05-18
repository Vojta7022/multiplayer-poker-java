package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import java.io.Serializable;

/**
 * Represents a playing card with a rank and suit.
 * Implements {@link Serializable} to allow serialization of card objects.
 */
public record Card(Rank rank, Suit suit) implements Serializable {

    /**
     * Prints the card in a human-readable format.
     * @return String representation of the card.
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
