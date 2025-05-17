package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

/**
 * Represents the ranks of cards in a standard deck.
 * The ranks are ordered from TWO (lowest) to ACE (highest).
 * Implements {@link Comparable} to allow rank comparison.
 */
public enum Rank implements Comparable<Rank> {
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;

    /**
     * Cards that make up a Royal Flush (TEN to ACE).
     */
    public final static Rank[] royalFlushCards = new Rank[] {TEN, JACK, QUEEN, KING, ACE};

    /**
     * Cards that make up the "Wheel" straight (Ace to Five).
     */
    public final static Rank[] theWheel = new Rank[] {ACE, TWO, THREE, FOUR, FIVE};
}
