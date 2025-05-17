package cz.cvut.fel.pjv.mosteji1.poker.common.player;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;

/**
 * {@code Player} class represents a participant in the poker game.
 * It stores information such as the player's name, avatar, hand of cards,
 * chip count, bet status, and connection endpoint.
 * <p>
 * Players can perform actions such as receiving cards, betting, folding,
 * and discarding their hand. The class also tracks whether a player has
 * gone all-in or disconnected from the game.
 */
public class Player {
    private final String name;
    private final int avatarIndex;

    private ServerEndpoint endpoint;
    private final ArrayList<Card> hand;
    private int chips;
    private boolean folded;
    private int bet;
    private boolean isAllIn;

    /**
     * Constructs a new {@code Player} with the specified name, avatar index,
     * and starting amount of chips.
     *
     * @param name         the name of the player
     * @param avatarIndex  the index of the player's avatar
     * @param startingChips the number of chips the player starts with
     */
    public Player(String name, int avatarIndex, int startingChips) {
        this.name = name;
        this.avatarIndex = avatarIndex;
        this.hand = new ArrayList<>();
        this.chips = startingChips;
        this.folded = false;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param card the card to receive
     */
    public void receiveCard(Card card) {
        hand.add(card);
    }

    /**
     * Folds the player, removing them from the current round.
     */
    public void fold() {
        folded = true;
    }

    /**
     * Discards the player's hand and resets their current bet.
     */
    public void discardCards() {
        hand.clear();
        bet = 0;
    }

    /**
     * Disconnects the player from the server by nullifying the endpoint.
     */
    public void disconnect() {
        endpoint = null;
    }

    /**
     * Checks if the player has folded.
     *
     * @return {@code true} if the player has folded; {@code false} otherwise
     */
    public boolean hasFolded() {
        return folded;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    /**
     * Returns the number of chips the player currently holds.
     *
     * @return the player's chip count
     */
    public int getChips() {
        return chips;
    }

    /**
     * Returns the player's current bet.
     *
     * @return the amount of chips the player has bet
     */
    public int getBet() {
        return bet;
    }

    /**
     * Sets the player's bet amount.
     *
     * @param bet the amount to set as the player's bet
     */
    public void setBet(int bet) {
        this.bet = bet;
    }

    /**
     * Returns the player's name.
     *
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the player is disconnected.
     *
     * @return {@code true} if the player is disconnected; {@code false} otherwise
     */
    public ServerEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the number of chips the player has.
     *
     * @param chips the chip count to assign
     */
    public void setChips(int chips) {
        this.chips = chips;
    }

    /**
     * Returns whether the player is all-in.
     *
     * @return {@code true} if the player is all-in; {@code false} otherwise
     */
    public boolean isAllIn() {
        return isAllIn;
    }

    /**
     * Sets whether the player is all-in.
     *
     * @param allIn the all-in status to set
     */
    public void setAllIn(boolean allIn) {
        isAllIn = allIn;
    }

    /**
     * Returns a string representation of the player, including their name,
     * hand, and chip count.
     *
     * @return string describing the player
     */
    @Override
    public String toString() {
        return name + " with hand: " + hand + " and chips: " + chips;
    }

    /**
     * Sets the folded status of the player.
     *
     * @param b {@code true} if the player has folded; {@code false} otherwise
     */
    public void setFolded(boolean b) {
        this.folded = b;
    }

    /**
     * Returns the index of the player's avatar.
     *
     * @return the avatar index
     */
    public int getAvatarIndex() {
        return avatarIndex;
    }

    /**
     * Sets the {@link ServerEndpoint} for the player.
     *
     * @param serverEndpoint the server endpoint to associate with the player
     */
    public void setEndpoint(ServerEndpoint serverEndpoint) {
        this.endpoint = serverEndpoint;
    }
}
