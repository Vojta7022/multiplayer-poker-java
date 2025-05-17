package cz.cvut.fel.pjv.mosteji1.poker.common.game;

/**
 * Defines the constant configuration parameters for the poker game.
 * This class contains various global constants such as blind values,
 * starting chips, port number, and limits on player count.
 *
 * <p>This class is abstract and cannot be instantiated.</p>
 */
public abstract class GameParameters {
    /** The port number for the server. */
    public static final int PORT = 12345;
    /** The number of avatars available for players. */
    public static final int AVATAR_COUNT = 40;
    /** The amount of chips for the small blind. */
    public static final int SMALL_BLIND = 10;
    /** The amount of chips for the big blind. */
    public static final int BIG_BLIND = 20;
    /** The starting amount of chips for each player. */
    public static final int STARTING_CHIPS = 1000;
    /** The maximum number of players allowed in a game. */
    public static final int MAX_PLAYERS = 10;
    /** The minimum number of players required to start a game. */
    public static final int MIN_PLAYERS = 2;
    /** The number of cards in a standard deck. */
    public static final int CARD_COUNT = 52;
}
