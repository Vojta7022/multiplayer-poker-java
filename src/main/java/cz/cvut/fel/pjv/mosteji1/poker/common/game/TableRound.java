package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TableRound {
    private final Deck deck;
    private final List<Card> communityCards;
    private List<Player> players;
    private final Table parentTable;
    private final int dealerIndex;



    private final Scanner scanner;

    TableRound(Table parentTable) {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        scanner = new Scanner(System.in);

        this.parentTable = parentTable;
        this.dealerIndex = parentTable.getCurrentDealerIndex();
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
        int smallBlind = 10;
        System.out.println(players.getFirst().toString() + " posts small blind: " + smallBlind);
        placeBet(smallBlind, players.getFirst());
        int bigBlind = 20;
        placeBet(bigBlind, players.get(1));

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
                            placeBet(highestBet, player);
                        } else {
                            System.out.println("Nothing to call, choose a different action.");
                        }
                        break;
                    case "r":
                        System.out.println("Enter raise amount: ");
                        int raiseAmount = scanner.nextInt();
                        highestBet += raiseAmount;
                        placeBet(raiseAmount, player);
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

    /**
     *
     * @param player
     * @return
     *
     */
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
}
