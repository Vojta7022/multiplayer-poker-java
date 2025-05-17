package cz.cvut.fel.pjv.mosteji1.poker.utils;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Rank;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Suit;

/**
 * Utility class for handling sprite indexing and related constants
 * used for rendering cards and UI elements in the poker game.
 */
public abstract class MyUtils {

    /**
     * Enumeration of special sprites used in the UI (non-card elements).
     */
    public enum Sprites {
        CARD_BACK,
        CARD_PLACEHOLDER,
        BUTTON_ABSENT,
        BUTTON_PRESENT,
        MENU_BACKGROUND,
    }

    /**
     * Returns the sprite index for a given card rank and suit.
     * The index is computed based on a standard order of ranks and suits.
     *
     * @param rank the rank of the card (e.g., ACE, KING)
     * @param suit the suit of the card (e.g., HEARTS, SPADES)
     * @return the integer index corresponding to the sprite
     */
    public static int getSpriteIndex(Rank rank, Suit suit) {
        return rank.ordinal() + suit.ordinal() * Rank.values().length;
    }

    /**
     * Returns the sprite index for a given card.
     *
     * @param card the card to get the sprite index for
     * @return the sprite index
     */
    public static int getSpriteIndex(Card card) {
        return getSpriteIndex(card.rank(), card.suit());
    }

    /**
     * Returns the sprite index for a non-card UI sprite.
     *
     * @param sprite the sprite enum constant
     * @return the sprite index for rendering
     */
    public static int getSpriteIndex(Sprites sprite) {
        return switch (sprite) {
            case CARD_BACK -> 52;
            case BUTTON_ABSENT -> 54;
            case BUTTON_PRESENT -> 55;
            case MENU_BACKGROUND -> 56;

            default -> 53; // CARD_PLACEHOLDER
        };
    }
}
