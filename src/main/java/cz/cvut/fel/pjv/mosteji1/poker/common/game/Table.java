package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.CardCombo;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Represents a table in a poker game. Manages the game flow,
 * community cards, betting rounds, and determines the winner.
 */
public class Table {
    // Parent server instance
    private final Server parent;
    // List of players at the table
    private final ArrayList<Player> players;
    // Deck of cards used in the game
    private Deck deck;
    // List of community cards
    private final ArrayList<Card> communityCards;
    // Size of the pot
    private int potSize;
    // Minimum bet threshold for the current round
    private int betThreshold;
    // Index of the dealer
    private int dealerIndex;
    // Index of the player waiting for their turn
    private int waitingForIndex = 2;
    // List of chat messages exchanged during the game
    private final ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    // Queue for player commands
    private final BlockingQueue<String> commandQueue;

    /**
     * Constructs a new Table instance with reference to the parent server and command queue.
     *
     * @param parent        The server managing this table.
     * @param commandQueue  A queue containing commands sent by players.
     */
    public Table(Server parent, BlockingQueue<String> commandQueue) {
        this.parent = parent;
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.commandQueue = commandQueue;
    }

    /**
     * Starts a new round of poker. Handles dealing cards, betting rounds,
     * community cards, and determines the winner.
     *
     * @throws InterruptedException if interrupted while waiting for commands.
     */
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

//        waitingForIndex = (dealerIndex + 1) % players.size();
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

    // Determines the winner of the round based on the players' hands and community cards.
    private Player determineWinner() {                  // TODO: co když je to remíza?
        List<Player> contenders = new ArrayList<>();
        for (Player player : players) {
            if (!player.hasFolded()) {
                contenders.add(player);
            }
        }

        if (contenders.size() == 1) {
            System.out.println("Player " + contenders.getFirst().getName() + " wins by default.");
            String message = contenders.getFirst().getName() + " wins by default.";
            appendMessageToChat(message, true);
            return contenders.getFirst();
        }
        if (contenders.isEmpty()) {
            System.out.println("WTF why did you all fold?");
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
        System.out.println("Winning hand: " + runningWinner.getHand() + " and community cards: " + communityCards);
        String message = runningWinner.getName() + " wins with " + runningCombo.getHandRanking() + " and " + runningWinner.getHand() + " and community cards: " + communityCards;
        appendMessageToChat(message, true);
        return runningWinner;
    }

    // Handles the betting process for players in the current round.
    private void letPlayersBet(int bettingStartIndex) throws InterruptedException {
        String message;
        while (true) {
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
                                if (numberOfActivePlayers() == 1) {
                                    return;
                                }
                                message = player.getName() + " has folded.";
                                appendMessageToChat(message, true);
                                System.out.println(message);
                            }
                            case "CALL" -> {
                                if (canPlaceBet(minimumBet, player)) {
                                    message = player.getName() + " has called.";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                    potSize += minimumBet;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    message = player.getName() + " has disconnected.";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                }
                            }
                            case "RAISE" -> {
                                int amountToRaise = Integer.parseInt(commandQueue.take());
                                if (canPlaceBet(amountToRaise + minimumBet, player)) {
                                    message = player.getName() + " has raised to " + (amountToRaise + minimumBet) + ".";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                    betThreshold = player.getBet();
                                    potSize += amountToRaise;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    message = player.getName() + " has disconnected.";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                }
                            }
                            case "ALLIN" -> goAllIn(player);
                        }
                    } else {
                        System.out.println(player.getName() + ", you can either check or raise.");
                        String action = commandQueue.take();
                        switch (action) {
                            case "CHECK" -> {
                                message = player.getName() + " has checked.";
                                appendMessageToChat(message, true);
                                System.out.println(message);
                            }
                            case "RAISE" -> {
                                int amountToRaise = Integer.parseInt(commandQueue.take());
                                if (canPlaceBet(amountToRaise, player)) {
                                    message = player.getName() + " has raised to " + amountToRaise + ".";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                    betThreshold = player.getBet();
                                    potSize += amountToRaise;
                                } else {
                                    player.disconnect();
                                    players.remove(player);
                                    message = player.getName() + " has disconnected.";
                                    appendMessageToChat(message, true);
                                    System.out.println(message);
                                }
                            }
                            case "FOLD" -> {
                                player.fold();
                                if (numberOfActivePlayers() == 1) {
                                    break;
                                }
                                message = player.getName() + " has folded.";
                                appendMessageToChat(message, true);
                                System.out.println(message);
                            }
                            case "ALLIN" -> goAllIn(player);

                        }
                    }
                }
            }
            System.out.println("Bet threshold: " + betThreshold);
        }
    }

    // Handles the all-in action for a player.
    private void goAllIn(Player player) {
        potSize += player.getChips();
        betThreshold = Math.max(player.getBet(), betThreshold);
        player.setAllIn(true);
        player.setChips(0);
        String message = player.getName() + " has gone all-in.";
        appendMessageToChat(message, true);
        System.out.println(message);
    }

    // Returns the number of active players at the table.
    private int numberOfActivePlayers() {
        int activePlayers = 0;
        for (Player player : players) {
            if (!player.hasFolded()) {
                activePlayers++;
            }
        }
        return activePlayers;
    }

    // Places the small and big blinds for the current round.
    private void placeBlinds(int smallBlindIndex) {
        String message;
        if (players.size() < 2) return;

        if (!canPlaceBet(GameParameters.SMALL_BLIND, players.get(smallBlindIndex))) {
            players.get(smallBlindIndex).disconnect();
            players.remove(smallBlindIndex);
            message = players.get(smallBlindIndex).getName() + " has disconnected.";
        } else {
            message = players.get(smallBlindIndex).getName() + " posts small blind: " + GameParameters.SMALL_BLIND;
        }
        appendMessageToChat(message, true);
        System.out.println(message);

        if (!canPlaceBet(GameParameters.BIG_BLIND, players.get((smallBlindIndex + 1) % players.size()))) {
            players.get(smallBlindIndex).disconnect();
            players.remove(smallBlindIndex);
            message = players.get((smallBlindIndex + 1) % players.size()).getName() + " has disconnected.";
        } else {
            message = players.get((smallBlindIndex + 1) % players.size()).getName() + " posts big blind: " + GameParameters.BIG_BLIND;
        }
        appendMessageToChat(message, true);
        System.out.println(message);


        potSize = GameParameters.SMALL_BLIND + GameParameters.BIG_BLIND;
        betThreshold = GameParameters.BIG_BLIND;

    }

    // Deals hole cards to each player at the table.
    private void dealHoleCards() {
        for (Player player : players) {
            player.receiveCard(deck.dealCard());
            player.receiveCard(deck.dealCard());
            System.out.println(player.getName() + " receives " + player.getHand().get(0) + " and " + player.getHand().get(1));
        }
    }

    // Deals the flop (three community cards).
    private void dealFlop() {
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
    }

    // Deals the turn (fourth community card).
    private void dealTurn() {
        communityCards.add(deck.dealCard());
    }

    // Deals the river (fifth community card).
    private void dealRiver() {
        communityCards.add(deck.dealCard());
    }

    // Returns the minimum valid bet for a player based on the current bets of all players.
    private int minimumValidBet(Player player) {
        int betPerPlayer = 0;
        for (Player otherPlayer : players) {
            if (otherPlayer.getBet() > betPerPlayer) betPerPlayer = otherPlayer.getBet();
        }
        return Math.max(betPerPlayer - player.getBet(), 0);
    }

    // Checks if a player can place a bet of a given amount.
    private boolean canPlaceBet(int amount, Player player) {
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

    /**
     * Returns the list of players currently at the table.
     *
     * @return List of players.
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the size of the pot.
     *
     * @return size of the pot.
     */
    public int getPotSize() {
        return potSize;
    }

    /**
     * Returns the current bet threshold.
     *
     * @return current bet threshold.
     */
    public int getBetThreshold() {
        return betThreshold;
    }

    /**
     * Returns the index of the dealer.
     *
     * @return index of the dealer.
     */
    public int getDealerIndex() {
        return dealerIndex;
    }

    /**
     * Returns the deck of cards used in the game.
     *
     * @return deck of cards.
     */
    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    /**
     * Returns the index of the player currently waiting for their turn.
     *
     * @return index of the player waiting for their turn.
     */
    public int getWaitingForIndex() {
        return waitingForIndex;
    }

    /**
     * Adds players to the table.
     *
     * @param serverEndpoints List of server endpoints representing players.
     */
    public void addPlayers(List<ServerEndpoint> serverEndpoints) {
        for (ServerEndpoint serverEndpoint : serverEndpoints) {
            Player player = new Player(serverEndpoint.getName(),serverEndpoint.getAvatarIndex(), GameParameters.STARTING_CHIPS);
            player.setEndpoint(serverEndpoint);
            players.add(player);
        }
    }

    /**
     * Checks whether the game has been won (only one player left).
     *
     * @return true if only one player remains, false otherwise.
     */
    public boolean isGameWon() {
        return players.size() == 1;
    }

    /**
     * Appends a message to the game chat log.
     *
     * @param message         The message to add.
     * @param isSystemMessage Whether the message is from the system or a player.
     */
    public void appendMessageToChat(String message, boolean isSystemMessage) {
        chatMessages.add(new ChatMessage(message, isSystemMessage));
    }

    /**
     * Returns the list of chat messages exchanged during the game.
     *
     * @return List of chat messages.
     */
    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    /**
     * Returns the player with the specified name.
     *
     * @param name The name of the player to find.
     * @return The player with the specified name, or null if not found.
     */
    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }
}
