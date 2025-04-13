package cz.cvut.fel.pjv.mosteji1.poker.common.player;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private ServerEndpoint endpoint;
    private final List<Card> hand;
    private int chips;
    private boolean folded;
    private int bet;

    public Player(String name, int startingChips) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.chips = startingChips;
        this.folded = false;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void fold() {
        folded = true;
        System.out.println(name + " folds.");
    }

    public void discardCards() {
        hand.clear();
        bet = 0;
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

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public String getName() {
        return name;
    }

    public ServerEndpoint getEndpoint() {
        return endpoint;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    @Override
    public String toString() {
        return name + " with hand: " + hand + " and chips: " + chips;
    }
}
