package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

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

    public final int numberOfKickers;

    HandRanking(int numberOfKickers) {
        this.numberOfKickers = numberOfKickers;
    }
}
