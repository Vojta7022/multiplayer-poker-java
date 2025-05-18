package cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ChatMessage;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents the state of the poker table from the client's perspective.
 * Includes information about players, community cards, pot size, dealer, and chat messages.
 * Used for rendering the game state in the client UI.
 */
public class TableRepresentation implements Serializable {
    // ArrayList to hold player representations
    private final ArrayList<PlayerRepresentation> players;
    // ArrayList to hold community cards
    private ArrayList<Card> communityCards;
    // Size of the pot
    private int potSize;
    // Threshold for betting
    private int betThreshold;
    // Index of the dealer
    private int dealerIndex;
    // Index of the player currently waiting for their turn
    private int waitingForIndex;
    // ArrayList to hold the player's hand
    private ArrayList<Card> myHand;
    // Index of your player in the game
    private int yourIndex;
    // ArrayList to hold chat messages
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();

    /**
     * Constructs a new empty TableRepresentation with default values.
     */
    public TableRepresentation() {
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.potSize = 0;
        this.betThreshold = 0;
        this.dealerIndex = 0;
        this.waitingForIndex = 0;
    }

    /**
     * Adds a player to the table representation.
     *
     * @param name          the player's name
     * @param avatarIndex   the player's avatar index
     * @param startingChips the player's chip count
     * @param bet           the player's current bet
     * @param folded        whether the player has folded
     * @param isAllIn       whether the player is all-in
     * @param myHand        the player's hand (if visible to client)
     */
    public void addPlayer(String name, int avatarIndex, int startingChips, int bet, boolean folded, boolean isAllIn, ArrayList<Card> myHand) {
        PlayerRepresentation player = new PlayerRepresentation(name, avatarIndex, startingChips, bet, folded, isAllIn, myHand);
        players.add(player);
    }

    /**
     * Sets the current total size of the pot.
     *
     * @param potSize the total pot size
     */
    public void setPotSize(int potSize) {
        this.potSize = potSize;
    }

    /**
     * Sets the current betting threshold.
     *
     * @param betThreshold the minimum bet that must be called
     */
    public void setBetThreshold(int betThreshold) {
        this.betThreshold = betThreshold;
    }

    /**
     * Sets the index of the dealer in the players list.
     *
     * @param dealerIndex index of the dealer
     */
    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    /**
     * Sets the cards in the player's own hand.
     *
     * @param myHand the player's hand (must be size 2)
     */
    public void setMyHand(ArrayList<Card> myHand) {
        this.myHand = myHand;
        assert myHand.size() == 2;
    }

    /**
     * Sets the shared community cards on the table.
     *
     * @param communityCards list of community cards
     */
    public void setCommunityCards(ArrayList<Card> communityCards) {
        this.communityCards = communityCards;
    }

    /**
     * Sets the index of the player whose turn it is to act.
     *
     * @param waitingForIndex index of the player to act
     */
    public void setWaitingForIndex(int waitingForIndex) {
        this.waitingForIndex = waitingForIndex;
    }

    /**
     * Gets the list of all player representations.
     *
     * @return list of players
     */
    public ArrayList<PlayerRepresentation> getPlayers() {
        return players;
    }

    /**
     * Gets the community cards on the table.
     *
     * @return list of community cards
     */
    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    /**
     * Gets the index of the player whose turn it is.
     *
     * @return index of the current player to act
     */
    public int getWaitingForIndex() {
        return waitingForIndex;
    }

    /**
     * Gets the index of the dealer.
     *
     * @return dealer index
     */
    public int getDealerIndex() {
        return dealerIndex;
    }

    /**
     * Gets the player's own hand.
     *
     * @return the hand of the current client
     */
    public ArrayList<Card> getMyHand() {
        return myHand;
    }

    /**
     * Returns the pot size as a string.
     *
     * @return string representation of the pot size
     */
    public String getPotSize() {
        return String.valueOf(potSize);
    }

    /**
     * Returns the current bet threshold.
     *
     * @return minimum amount that must be called
     */
    public int getBetThreshold() {
        return betThreshold;
    }

    /**
     * Sets the index of the player representing the client.
     *
     * @param playersIndex the index of the local player
     */
    public void setYourIndex(int playersIndex) {
        this.yourIndex = playersIndex;
    }

    /**
     * Gets the index of the player representing the client.
     *
     * @return local player's index
     */
    public int getYourIndex() {
        return yourIndex;
    }

    /**
     * Gets the list of recent chat messages.
     *
     * @return list of chat messages
     */
    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    /**
     * Sets the list of chat messages displayed on the table.
     *
     * @param chatMessages new list of chat messages
     */
    public void setChatMessages(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }
}
