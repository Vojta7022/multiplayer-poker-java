package cz.cvut.fel.pjv.mosteji1.poker.common.player;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final List<Card> hand;
    private int chips;
    private boolean folded;

    public Player(String name, int startingChips) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.chips = startingChips;
        this.folded = false;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void placeBet(int amount) {
        if (amount <= chips) {
            chips -= amount;
            System.out.println(name + " bets " + amount + " chips.");
        } else {
            System.out.println(name + " does not have enough chips!");
        }

    }

    public void fold() {
        folded = true;
        System.out.println(name + " folds.");
    }

    public boolean hasFolded() {
        return folded;
    }

    public List<Card> getHand() {
        return hand;
    }

    public int getChips() {
        return chips;
    }

    @Override
    public String toString() {
        return name + " with hand: " + hand + " and chips: " + chips;
    }
}
