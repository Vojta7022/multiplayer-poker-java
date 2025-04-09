package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

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

    public final static Rank[] royalFlushCards = new Rank[] {TEN, JACK, QUEEN, KING, ACE};
    public final static Rank[] theWheel = new Rank[] {ACE, TWO, THREE, FOUR, FIVE};
}
