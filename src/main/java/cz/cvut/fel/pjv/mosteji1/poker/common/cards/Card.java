package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import java.io.Serializable;

public record Card(Rank rank, Suit suit) implements Serializable {

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
