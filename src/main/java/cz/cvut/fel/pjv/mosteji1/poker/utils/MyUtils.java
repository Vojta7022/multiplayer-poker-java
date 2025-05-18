package cz.cvut.fel.pjv.mosteji1.poker.utils;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Rank;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Suit;

import java.util.logging.*;

/**
 * Utility class for handling sprite indexing and related constants
 * used for rendering cards and UI elements in the poker game.
 */
public abstract class MyUtils {

    /**
     * Enumeration of special sprites used in the UI (non-card elements).
     */
    public enum Sprites {
        CARD_BACK,
        CARD_PLACEHOLDER,
        BUTTON_ABSENT,
        BUTTON_PRESENT,
        MENU_BACKGROUND,
    }

    /**
     * Returns the sprite index for a given card rank and suit.
     * The index is computed based on a standard order of ranks and suits.
     *
     * @param rank the rank of the card (e.g., ACE, KING)
     * @param suit the suit of the card (e.g., HEARTS, SPADES)
     * @return the integer index corresponding to the sprite
     */
    public static int getSpriteIndex(Rank rank, Suit suit) {
        return rank.ordinal() + suit.ordinal() * Rank.values().length;
    }

    /**
     * Returns the sprite index for a given card.
     *
     * @param card the card to get the sprite index for
     * @return the sprite index
     */
    public static int getSpriteIndex(Card card) {
        return getSpriteIndex(card.rank(), card.suit());
    }

    /**
     * Returns the sprite index for a non-card UI sprite.
     *
     * @param sprite the sprite enum constant
     * @return the sprite index for rendering
     */
    public static int getSpriteIndex(Sprites sprite) {
        return switch (sprite) {
            case CARD_BACK -> 52;
            case BUTTON_ABSENT -> 54;
            case BUTTON_PRESENT -> 55;
            case MENU_BACKGROUND -> 56;

            default -> 53; // CARD_PLACEHOLDER
        };
    }

    /**
     * Initializes the logger based on command line arguments.
     * If "--debug" is passed, logging is enabled; otherwise, it is disabled.
     *
     * @param args command line arguments
     */
    public static void initializeLogger(String[] args) {
        boolean enableDebug = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--debug")) {
                enableDebug = true;
                break;
            }
        }

        if (!enableDebug) {
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.OFF);
            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(Level.OFF);
            }
            return;
        }

        // Enable detailed logging
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.INFO);
        }

        Logger myLogger = Logger.getLogger("cz.cvut.fel.pjv.mosteji1.poker");
        myLogger.setLevel(Level.ALL);
        myLogger.setUseParentHandlers(false);

        Handler handler = new StreamHandler(System.out, new ColorFormatter()) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        handler.setLevel(Level.ALL);
        myLogger.addHandler(handler);
    }

    private static class ColorFormatter extends Formatter {
        private static final String RESET = "\u001B[0m";
        private static final String RED = "\u001B[31m";
        private static final String YELLOW = "\u001B[33m";
        private static final String BLUE = "\u001B[34m";
        private static final String CYAN = "\u001B[36m";
        private static final String WHITE = "\u001B[37m";

        @Override
        public String format(LogRecord record) {
            String color;
            if (record.getLevel() == Level.SEVERE) {
                color = RED;
            } else if (record.getLevel() == Level.WARNING) {
                color = YELLOW;
            } else if (record.getLevel() == Level.INFO) {
                color = WHITE;
            } else if (record.getLevel() == Level.CONFIG) {
                color = CYAN;
            } else {
                color = BLUE;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(new java.util.Date(record.getMillis()));

            String loggerName = record.getLoggerName();

            return String.format("%s%s %s [%s]: %s%s%n", color, time, record.getLevel(), loggerName, formatMessage(record), RESET);
        }
    }
}
