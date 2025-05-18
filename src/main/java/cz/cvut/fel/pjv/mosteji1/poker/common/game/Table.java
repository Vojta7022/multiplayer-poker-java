package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.CardCombo;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ChatMessage;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Represents a table in a poker game. Manages the game flow,
 * community cards, betting rounds, and determines the winner.
 */
public class Table {

    private static final Logger logger = Logger.getLogger(Table.class.getName());

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
    private final List<ChatMessage> chatMessages = new LinkedList<>();
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

        resetTable();

        dealerIndex = (dealerIndex + 1) % players.size();

        placeBlinds((dealerIndex + 1) % players.size()); // small blind is to the left of the dealer
        dealHoleCards();
        logger.fine("Dealing hole cards...");

        waitingForIndex = (dealerIndex + 3) % players.size();
        parent.sendUpdatesToAllPlayers();

        for (Player player : players) {
            if (player.getChips() == 0) {
                player.disconnect();
                players.remove(player);
                String message = player.getName() + " has disconnected.";
                appendMessageToChat(message, true);
            }
        }

        letPlayersBet((dealerIndex + 3) % players.size());        // start from player to the left of big blind

        if (numberOfActivePlayers() > 1) {
            dealFlop();
            logger.fine("Dealing flop...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        if (numberOfActivePlayers() > 1) {
            dealTurn();
            logger.fine("Dealing turn...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        if (numberOfActivePlayers() > 1) {
            dealRiver();
            logger.fine("Dealing river...");
            letPlayersBet((dealerIndex + 1) % players.size());
        }

        List<Player> winners = determineWinners();
        dividePot(winners);

        parent.sendUpdatesToAllPlayers();
    }

    // Resets the table for a new round, clearing community cards and resetting player states.
    private void resetTable() {
        communityCards.clear();
        deck = new Deck();
        potSize = 0;
        betThreshold = 0;

        for (Player player : players) {
            player.discardCards();
            player.setBet(0);
            player.setAllIn(false);
            player.setFolded(false);
        }

        parent.sendUpdatesToAllPlayers();
    }

    // Determines the winner of the round based on the players' hands and community cards.
    private List<Player> determineWinners() {
        List<Player> contenders = new ArrayList<>();
        for (Player player : players) {
            if (!player.hasFolded()) {
                contenders.add(player);
            }
        }

        if (contenders.isEmpty()) {
            logger.fine("WTF why did you all fold?");
            return null;
        }

        if (contenders.size() == 1) {
            String message = contenders.getFirst().getName() + " wins by default.";
            appendMessageToChat(message, true);
            logger.fine(message);
            return contenders;
        }

        CardCombo runningCombo = new CardCombo(contenders.getFirst().getHand(), communityCards);
        List<Player> runningWinners = new ArrayList<>();

        for (Player contender : contenders) {
            CardCombo contenderCombo = new CardCombo(contender.getHand(), communityCards);

            if (runningWinners.isEmpty()) {
                runningWinners.add(contender);
            } else if (contenderCombo.compareTo(runningCombo) == 0) {
                runningWinners.add(contender);
            } else if (contenderCombo.compareTo(runningCombo) > 0) {
                runningWinners.clear();
                runningWinners.add(contender);
                runningCombo = contenderCombo;
            }
        }

        StringBuilder winnersString = new StringBuilder();
        for (Player winner : runningWinners) {
            winnersString.append(winner.getName()).append(" ");
        }
        String message = winnersString + "win(s) with " + runningCombo.getHandRanking();
        logger.fine(message);
        appendMessageToChat(message, true);
        message = "Winning hands:\n";
        for (Player player : runningWinners) {
            message += player.getName() + ": " + player.getHand() + "\n";
        }
        logger.fine(message);
        appendMessageToChat(message, true);
        return runningWinners;
    }

    // Divides the pot among the winners based on their bets.
    private void dividePot(List<Player> winners) {
        int sumWinnerBets = 0;
        if (winners == null) {
            logger.fine("No winners, no one wins anything.");
            return;
        }
        for (Player winner : winners) {
            sumWinnerBets += winner.getBet();
        }

        if (sumWinnerBets == 0) {
            logger.fine("No one bet anything, no one wins anything. (Also this shouldn't happen)");
            return;
        }

        for (Player winner : winners) {
            int chipsPerWinner = potSize * winner.getBet() / sumWinnerBets;

            winner.setChips(winner.getChips() + chipsPerWinner);
            String message = winner.getName() + " wins " + chipsPerWinner + " chips.";
            appendMessageToChat(message, true);
            logger.fine(message);
            message = "Waiting 5 seconds before starting a new round.";
            appendMessageToChat(message, true);
            logger.fine(message);
        }
        potSize = 0;
    }

    // Handles the betting process for players in the current round.
    private void letPlayersBet(int bettingStartIndex) throws InterruptedException {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get((bettingStartIndex + i) % players.size());
            if (numberOfActivePlayers() > 1) letPlayerBet(player);
        }
        for (int i = 0; !canGoToNextRound(); i++) {
            Player player = players.get((bettingStartIndex + i) % players.size());
            if (numberOfActivePlayers() > 1) letPlayerBet(player);
        }
    }

    private void letPlayerBet(Player player) throws InterruptedException {
        waitingForIndex = players.indexOf(player);
        String message;
        if (!player.hasFolded() && !player.isAllIn()) {
            int bet = player.getBet();
            if (bet < betThreshold) {
                int minimumBet = betThreshold - bet;
                logger.fine(player.getName() + ", you need to bet at least " + minimumBet + " chips.");
                String action = commandQueue.take();
                switch (action) {
                    case "FOLD" -> {
                        player.fold();
                        if (numberOfActivePlayers() == 1) {
                            return;
                        }
                        message = player.getName() + " has folded.";
                        appendMessageToChat(message, true);
                        logger.fine(message);
                    }
                    case "CALL" -> {
                        if (canPlaceBet(minimumBet, player)) {
                            message = player.getName() + " has called.";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                            potSize += minimumBet;
                        } else {
                            player.disconnect();
                            players.remove(player);
                            message = player.getName() + " has disconnected.";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                        }
                    }
                    case "RAISE" -> {
                        int amountToRaise = Integer.parseInt(commandQueue.take());
                        if (canPlaceBet(amountToRaise + minimumBet, player)) {
                            message = player.getName() + " has raised by " + amountToRaise + ".";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                            betThreshold = player.getBet();
                            potSize += (amountToRaise + minimumBet);
                        } else {
                            player.disconnect();
                            players.remove(player);
                            message = player.getName() + " has disconnected.";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                        }
                    }
                    case "ALLIN" -> goAllIn(player);
                }
            } else {
                logger.fine(player.getName() + ", you can either check or raise.");
                String action = commandQueue.take();
                switch (action) {
                    case "CHECK" -> {
                        message = player.getName() + " has checked.";
                        appendMessageToChat(message, true);
                        logger.fine(message);
                    }
                    case "RAISE" -> {
                        int amountToRaise = Integer.parseInt(commandQueue.take());
                        if (canPlaceBet(amountToRaise, player)) {
                            message = player.getName() + " has raised by " + amountToRaise + ".";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                            betThreshold = player.getBet();
                            potSize += amountToRaise;
                        } else {
                            player.disconnect();
                            players.remove(player);
                            message = player.getName() + " has disconnected.";
                            appendMessageToChat(message, true);
                            logger.fine(message);
                        }
                    }
                    case "FOLD" -> {
                        player.fold();
                        if (numberOfActivePlayers() == 1) {
                            return;
                        }
                        message = player.getName() + " has folded.";
                        appendMessageToChat(message, true);
                        logger.fine(message);
                    }
                    case "ALLIN" -> goAllIn(player);

                }
            }
        }
        logger.fine("Bet threshold: " + betThreshold);
    }

    private boolean canGoToNextRound() {
        for (Player player : players) {
            if (!(player.hasFolded() || player.isAllIn() || player.getBet() == betThreshold)) {
                return false;
            }
        }
        return true;
    }

    // Handles the all-in action for a player.
    private void goAllIn(Player player) {
        potSize += player.getChips();
        betThreshold = Math.max(player.getBet(), betThreshold);
        player.setAllIn(true);
        player.setChips(0);
        String message = player.getName() + " has gone all-in.";
        appendMessageToChat(message, true);
        logger.fine(message);
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

        Player smallBlindPlayer = players.get(smallBlindIndex);
        Player bigBlindPlayer = players.get((smallBlindIndex + 1) % players.size());

        if (!canPlaceBet(GameParameters.SMALL_BLIND, smallBlindPlayer)) {
            smallBlindPlayer.disconnect();
            message = players.get(smallBlindIndex).getName() + " has disconnected.";
            players.remove(smallBlindIndex);
        } else {
            message = smallBlindPlayer.getName() + " posts small blind: " + GameParameters.SMALL_BLIND;
        }
        appendMessageToChat(message, true);
        logger.fine(message);

        if (!canPlaceBet(GameParameters.BIG_BLIND, bigBlindPlayer)) {
            bigBlindPlayer.disconnect();
            message = bigBlindPlayer.getName() + " has disconnected.";
            players.remove((smallBlindIndex + 1) % players.size());
        } else {
            message = bigBlindPlayer.getName() + " posts big blind: " + GameParameters.BIG_BLIND;
        }
        appendMessageToChat(message, true);
        logger.fine(message);

        potSize = GameParameters.SMALL_BLIND + GameParameters.BIG_BLIND;
        betThreshold = GameParameters.BIG_BLIND;
    }

    // Deals hole cards to each player at the table.
    private void dealHoleCards() {
        for (Player player : players) {
            player.receiveCard(deck.dealCard());
            player.receiveCard(deck.dealCard());
            logger.fine(player.getName() + " receives " + player.getHand().get(0) + " and " + player.getHand().get(1));
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

    // Checks if a player can place a bet of a given amount and if they can, immediately places it.
    private boolean canPlaceBet(int amount, Player player) {
        if (amount <= player.getChips() && amount >= minimumValidBet(player)) {
            player.setBet(amount + player.getBet());
            player.setChips(player.getChips() - amount);
            logger.fine(player.getName() + " bets " + amount + " chips.");
            return true;
        } else {
            logger.fine(player.getName() + " does not have enough chips!");
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
            serverEndpoint.setPlayerPTR(player);
            players.add(player);
        }
    }

    /**
     * Adds players to the table, used for testing purposes.
     *
     * @param players List of players to add.
     */
    public void addPlayersFromPointers(List<Player> players) {
        this.players.addAll(players);
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
    public synchronized void appendMessageToChat(String message, boolean isSystemMessage) {
        chatMessages.add(new ChatMessage(message, isSystemMessage));
        if (chatMessages.size() > 20) {
            chatMessages.removeFirst();
        }
    }

    /**
     * Returns the list of chat messages exchanged during the game.
     *
     * @return List of chat messages.
     */
    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }
}
