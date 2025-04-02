package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Table {
    private final List<Player> players;
    private final Deck deck;
    private final List<Card> communityCards;
    private final Scanner scanner;

    public Table() {
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void postBlinds() {
        if (players.size() < 2) return;
        int smallBlind = 10;
        System.out.println(players.getFirst().toString() + " posts small blind: " + smallBlind);
        players.get(0).placeBet(smallBlind);
        int bigBlind = 20;
        System.out.println(players.get(1).toString() + " posts big blind: " + bigBlind);
        players.get(1).placeBet(bigBlind);
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

    public void bettingRound() {
        int highestBet = 0;
        boolean bettingActive;
        do {
            bettingActive = false;
            for (Player player : players) {
                if (player.hasFolded()) continue;
                System.out.println(player + "'s turn. Current highest bet: " + highestBet);
                System.out.println("Choose action: check (c), call (o), raise (r), fold (f)");
                String action = scanner.next();

                switch (action) {
                    case "c":
                        if (highestBet == 0) {
                            System.out.println(player + " checks.");
                        } else {
                            System.out.println("Cannot check, must call or raise.");
                        }
                        break;
                    case "o":
                        if (highestBet > 0) {
                            player.placeBet(highestBet);
                        } else {
                            System.out.println("Nothing to call, choose a different action.");
                        }
                        break;
                    case "r":
                        System.out.println("Enter raise amount: ");
                        int raiseAmount = scanner.nextInt();
                        highestBet += raiseAmount;
                        player.placeBet(highestBet);
                        bettingActive = true;
                        break;
                    case "f":
                        player.fold();
                        break;
                    default:
                        System.out.println("Invalid action.");
                }
            }
        } while (bettingActive);
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    @Override
    public String toString() {
        return "Table with players: " + players + "\nCommunity Cards: " + communityCards;
    }
}
