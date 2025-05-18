package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

/**
 * Enum representing the different hand rankings in poker.
 * Each ranking has an associated number of kickers.
 * The lower the number, the better the hand.
 */
public enum HandRanking {
    HIGH_CARD(5),
    PAIR(4),
    TWO_PAIRS(3),
    THREE_OF_A_KIND(3),
    STRAIGHT(1),
    FLUSH(5),
    FULL_HOUSE(2),
    FOUR_OF_A_KIND(2),
    STRAIGHT_FLUSH(1),
    ROYAL_FLUSH(0);

    /**
     * The number of kickers associated with this hand ranking.
     * A kicker is a card that can be used to break ties between hands of the same rank.
     */
    public final int numberOfKickers;

    /**
     * Constructor for HandRanking enum.
     *
     * @param numberOfKickers The number of kickers associated with this hand ranking.
     */
    HandRanking(int numberOfKickers) {
        this.numberOfKickers = numberOfKickers;
    }
}
