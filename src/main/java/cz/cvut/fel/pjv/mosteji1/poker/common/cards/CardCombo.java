package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

public class CardCombo implements Comparable<CardCombo> {
    private final Card[] cards;

    public CardCombo(Card[] cards) {
        assert cards.length == 7;
        this.cards = cards;
    }



    @Override
    public int compareTo(CardCombo other) {
        return 0;
    }

    public int containsRoyalFLush(){
        return 0;
    }

    public int containsStraightFlush(){
        return 0;
    }
}
