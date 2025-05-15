package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.CardCombo;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class Table {
    private final Server parent;
    private final ArrayList<Player> players;
    private Deck deck;
    private ArrayList<Card> communityCards;
    private int potSize;
    private int betThreshold;
    private int dealerIndex;
    private int waitingForIndex = 2;
    private final ArrayList<String> chatMessages = new ArrayList<>();
    private final BlockingQueue<String> commandQueue;
    Scanner scanner = new Scanner(System.in);       // TODO: remove

    public Table(Server parent, BlockingQueue<String> commandQueue) {
        // Logic to start the game
        this.parent = parent;
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.commandQueue = commandQueue;

        /*for (ServerEndpoint endpoint : parent.getServerEndpoints()) {
            Random random = new Random();
            Player player = new Player(GameParameters.names[random.nextInt(GameParameters.names.length)], 1, 1000);
            players.add(player);
        }*/
    }

    public void startRound() throws InterruptedException {

        deck = new Deck();
        potSize = 0;
        betThreshold = 0;

        for (Player player : players) {
            player.discardCards();
            player.setBet(0);
            player.setAllIn(false);
            player.setFolded(false);
        }

        dealerIndex = (dealerIndex + 1) % players.size();

        placeBlinds((dealerIndex + 1) % players.size()); // small blind is to the left of the dealer
        dealHoleCards();
        System.out.println("Dealing hole cards...");

        parent.sendUpdatesToAllPlayers();

        letPlayersBet((dealerIndex + 3) % players.size());        // start from player to the left of big blind


        // TODO: checkni jestli jsou hraci bez chipu a odpoj je

        if (numberOfActivePlayers() > 1) {
            dealFlop();
            System.out.println("Dealing flop...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        if (numberOfActivePlayers() > 1) {
            dealTurn();
            System.out.println("Dealing turn...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        if (numberOfActivePlayers() > 1) {
            dealRiver();
            System.out.println("Dealing river...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        Player winner = determineWinner();

        if (winner != null) {winner.setChips(winner.getChips() + potSize);}
        potSize = 0;

    }

    private Player determineWinner() {                  // TODO: co když je to remíza?
        List<Player> contenders = new ArrayList<>();
        for (Player player : players) {
            if (!player.hasFolded()) {
                contenders.add(player);
            }
        }

        if (contenders.size() == 1) {
            System.out.println("Player " + contenders.getFirst().getName() + " wins by default.");
            return contenders.getFirst();
        }
        if (contenders.isEmpty()) {
            System.out.println("Vy debilove proc jste to vsichni foldli?");
            return null;
        }

        Player runningWinner = contenders.getFirst();
        CardCombo runningCombo = new CardCombo(runningWinner.getHand(), communityCards);

        for (Player contender : contenders) {
            CardCombo contenderCombo = new CardCombo(contender.getHand(), communityCards);
            if (contenderCombo.compareTo(runningCombo) > 0) {
                runningWinner = contender;
                runningCombo = contenderCombo;
            }
        }

        System.out.println("Player " + runningWinner.getName() + " wins with " + runningCombo.getHandRanking());
        return runningWinner;
    }

    private void letPlayersBet(int bettingStartIndex) throws InterruptedException {
        boolean allPlayersFolded = false;
        while (!allPlayersFolded) {
            for (Player player : players) {
                waitingForIndex = players.indexOf(player);
                if (!player.hasFolded() && !player.isAllIn()) {
                    int bet = player.getBet();
                    if (bet < betThreshold) {
                        int minimumBet = betThreshold - bet;
                        System.out.println(player.getName() + ", you need to bet at least " + minimumBet + " chips.");
                        String action = commandQueue.take();
                        switch (action) {
                            case "FOLD" -> {
                                player.fold();
                                System.out.println(player.getName() + " folds.");
                            }
                            case "CALL" -> {
                                if (canPlaceBet(minimumBet, player)) {
                                    System.out.println(player.getName() + " calls.");
                                    potSize += minimumBet;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    System.out.println("Player " + player.getName() + " has disconnected.");
                                }
                            }
                            case "RAISE" -> {
                                int amountToRaise = Integer.parseInt(commandQueue.take());
                                if (canPlaceBet(amountToRaise + minimumBet, player)) {
                                    System.out.println(player.getName() + " raises to " + (amountToRaise + minimumBet) + ".");
                                    betThreshold = player.getBet();
                                    potSize += amountToRaise;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    System.out.println("Player " + player.getName() + " has disconnected.");
                                }
                            }
                            case "ALLIN" -> {
                                potSize += player.getChips();
                                betThreshold = Math.max(player.getBet(), betThreshold);
                                player.setAllIn(true);
                                player.setChips(0);
                            }
                        }
                    } else {
                        System.out.println(player.getName() + ", you can either check or raise.");
                        String action = commandQueue.take();
                        switch (action) {
                            case "CHECK" -> System.out.println(player.getName() + " checks.");
                            case "RAISE" -> {
                                int amountToRaise = Integer.parseInt(commandQueue.take());
                                if (canPlaceBet(amountToRaise, player)) {
                                    System.out.println(player.getName() + " raises to " + amountToRaise + ".");
                                    betThreshold = player.getBet();
                                    potSize += amountToRaise;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    System.out.println("Player " + player.getName() + " has disconnected.");
                                }
                            }
                            case "FOLD" -> {
                                player.fold();
                                System.out.println(player.getName() + " folds.");
                            }
                            case "ALLIN" -> {
                                potSize += player.getChips();
                                betThreshold = Math.max(player.getBet(), betThreshold);
                                player.setAllIn(true);
                                player.setChips(0);
                            }
                        }
                    }
                }
            }
            allPlayersFolded = true;
            System.out.println("Bet threshold: " + betThreshold);
            for (Player player : players) {
                if (!player.hasFolded() && player.getBet() < betThreshold) {
                    allPlayersFolded = false;

                }
            }
            if (allPlayersFolded) {
                System.out.println("All players have folded or called the bet.");
                break;
            }
        }
    }

    private int numberOfActivePlayers() {
        int activePlayers = 0;
        for (Player player : players) {
            if (!player.hasFolded()) {
                activePlayers++;
            }
        }
        return activePlayers;
    }

    private void placeBlinds(int smallBlindIndex) {
        if (players.size() < 2) return;
        System.out.println(players.get(smallBlindIndex).toString() + " posts small blind: " + GameParameters.SMALL_BLIND);
        if (!canPlaceBet(GameParameters.SMALL_BLIND, players.get(smallBlindIndex))) {
            players.get(smallBlindIndex).disconnect();
            players.remove(smallBlindIndex);
            System.out.println("Player " + players.get(smallBlindIndex).getName() + " has disconnected.");
        } else {
            System.out.println(players.get(smallBlindIndex).toString() + " posts small blind: " + GameParameters.SMALL_BLIND);
        }

        if (!canPlaceBet(GameParameters.BIG_BLIND, players.get((smallBlindIndex + 1) % players.size()))) {
            players.get(smallBlindIndex).disconnect();
            players.remove(smallBlindIndex);
            System.out.println("Player " + players.get((smallBlindIndex + 1) % players.size()).getName() + " has disconnected.");
        } else {
            System.out.println(players.get((smallBlindIndex + 1) % players.size()).toString() + " posts big blind: " + GameParameters.BIG_BLIND);
        }


        potSize = GameParameters.SMALL_BLIND + GameParameters.BIG_BLIND;
        betThreshold = GameParameters.BIG_BLIND;

    }

    public void dealHoleCards() {
        for (Player player : players) {
            player.receiveCard(deck.dealCard());
            player.receiveCard(deck.dealCard());
            System.out.println(player.getName() + " receives " + player.getHand().get(0) + " and " + player.getHand().get(1));
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

    public boolean canPlaceBet(int amount, Player player) {
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

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getPotSize() {
        return potSize;
    }

    public int getBetThreshold() {
        return betThreshold;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    public int getWaitingForIndex() {
        return waitingForIndex;
    }

    public void addPlayers(List<ServerEndpoint> serverEndpoints) {
        for (ServerEndpoint serverEndpoint : serverEndpoints) {
            Player player = new Player(serverEndpoint.getName(),serverEndpoint.getAvatarIndex(), GameParameters.STARTING_CHIPS);
            player.setEndpoint(serverEndpoint);
            players.add(player);
        }
    }

    public boolean isGameWon() {
        return players.size() == 1;
    }

    public void appendMessageToChat(String s) {
        chatMessages.add(s);
        System.out.println("ChatMessages: " + chatMessages);
    }

    public ArrayList<String> getChatMessages() {
        return chatMessages;
    }

    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }
}
