package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TableRound {
    private final Deck deck;
    private final List<Card> communityCards;
    private List<Player> players;
    private int potSize;
    private Table parentTable;
    private int dealerIndex;

    TableRound(Table parentTable, int dealerIndex) {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.parentTable = parentTable;
        this.players = parentTable.getPlayers();
    }

    public boolean placeBet(int amount, Player player) {
        if (amount <= player.getChips() && amount >= minimumValidBet(player)) {
            player.setBet(amount + player.getBet());
            player.setChips(player.getChips() - amount);
            System.out.println(player.getName() + " bets " + amount + " chips.");
            return true;
        } else {
            System.out.println(player.getName() + " does not have enough chips!");
            return false;
        }
    }


    public void postBlinds() {
        if (players.size() < 2) return;

        Player smallBlind = players.get((dealerIndex + 1) % players.size());
        Player bigBlind = players.get((dealerIndex + 2) % players.size());

        int smallBlindAmount = GameParameters.SMALL_BLIND.value;
        int bigBlindAmount = GameParameters.BIG_BLIND.value;
        placeBet(smallBlindAmount, smallBlind);
        placeBet(bigBlindAmount, bigBlind);

        System.out.println("Small blind: " + smallBlind.getName() + " posts " + smallBlindAmount);
        System.out.println("Big blind: " + bigBlind.getName() + " posts " + bigBlindAmount);
    }

    public void bettingRound() {
        for (Player player : players) {
            if (!player.hasFolded()) {
                player.getEndpoint().sendMessage("Your turn to bet. Current bet: " + minimumValidBet(player));
                int bet = 0; // Get bet from player
                placeBet(bet, player);
            }
        }
    }

    public void dealHoleCards() {
        for (Player player : players) {
            player.receiveCard(deck.dealCard());
            player.receiveCard(deck.dealCard());
        }
    }

    public void dealFlop() {
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
    }

    public void dealTurn() {
        communityCards.add(deck.dealCard());
    }

    public void dealRiver() {
        communityCards.add(deck.dealCard());
    }

    public int minimumValidBet(Player player) {
        int betPerPlayer = 0;
        for (Player otherPlayer : players) {
            if (otherPlayer.getBet() > betPerPlayer) betPerPlayer = otherPlayer.getBet();
        }
        return Math.max(betPerPlayer - player.getBet(), 0);
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public void playRound() {
        postBlinds();
    }
}
