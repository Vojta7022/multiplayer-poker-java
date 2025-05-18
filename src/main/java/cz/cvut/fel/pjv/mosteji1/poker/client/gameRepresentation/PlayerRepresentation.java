package cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a player in a representation of the poker game that is being sent over the server.
 * Implements {@link Serializable} to allow serialization of player objects.
 *
 * @param name        the name of the player
 * @param avatarIndex the index of the player's avatar
 * @param chips       the number of chips the player has
 * @param bet         the amount the player has bet
 * @param folded      indicates if the player has folded
 * @param isAllIn     indicates if the player is all-in
 * @param myHand      the player's hand of cards
 */
public record PlayerRepresentation(String name, int avatarIndex, int chips, int bet, boolean folded, boolean isAllIn, ArrayList<Card> myHand) implements Serializable {

}
