package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import java.io.Serializable;

/**
 * Represents a chat message exchanged in the poker game.
 *
 * @param text   the content of the chat message
 * @param isBold indicates if the message should be displayed in bold (typically used for system messages)
 */
public record ChatMessage(String text, boolean isBold) implements Serializable {
}
