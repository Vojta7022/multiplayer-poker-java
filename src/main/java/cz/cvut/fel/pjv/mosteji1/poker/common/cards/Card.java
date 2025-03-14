package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

public record Card(Rank rank, Suit suit) {

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
