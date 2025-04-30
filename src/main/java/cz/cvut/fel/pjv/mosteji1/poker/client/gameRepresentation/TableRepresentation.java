package cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class TableRepresentation implements Serializable {
    private final ArrayList<PlayerRepresentation> players;
    private ArrayList<Card> communityCards;
    private int potSize;
    private int betThreshold;
    private int dealerIndex;
    private int waitingForIndex;
    private ArrayList<Card> myHand;

    private boolean gameStarted;

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public TableRepresentation() {
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.potSize = 0;
        this.betThreshold = 0;
        this.dealerIndex = 0;
        this.waitingForIndex = 0;
        this.gameStarted = false;
    }

    public void addPlayer(String name, int avatarIndex, int startingChips, int bet, boolean folded, boolean isAllIn) {
        PlayerRepresentation player = new PlayerRepresentation(name, avatarIndex, startingChips, bet, folded, isAllIn);
        players.add(player);
    }

    public void setPotSize(int potSize) {
        this.potSize = potSize;
    }

    public void setBetThreshold(int betThreshold) {
        this.betThreshold = betThreshold;
    }

    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    public void setMyHand(ArrayList<Card> myHand) {
        this.myHand = myHand;
        assert myHand.size() == 2;
    }

    public void dropHand() {
        myHand = null;
    }


    public void setCommunityCards(ArrayList<Card> communityCards) {
        this.communityCards = communityCards;
    }

    public void setWaitingForIndex(int waitingForIndex) {
        this.waitingForIndex = waitingForIndex;
    }

    public ArrayList<PlayerRepresentation> getPlayers() {
        return players;
    }

    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    public int getWaitingForIndex() {
        return waitingForIndex;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public ArrayList<Card> getMyHand() {
        return myHand;
    }

    public String getPotSize() {
        return String.valueOf(potSize);
    }

    public String getBetThreshold() {
        return String.valueOf(betThreshold);
    }
}
