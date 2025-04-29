package cz.cvut.fel.pjv.mosteji1.poker.myUtils;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Rank;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Suit;

import java.util.List;
import java.util.stream.Stream;

public abstract class MyUtils {

    public static enum Sprites {
        CARD_BACK,
        CARD_PLACEHOLDER,
        BUTTON_ABSENT,
        BUTTON_PRESENT,
        MENU_BACKGROUND,
    }

    public static <T> List<T> rotateList(List<T> list, int shift) {
        return Stream.concat(list.subList(shift, list.size()).stream(), list.subList(0, shift).stream()).toList();
    }

    public static int getSpriteIndex(Rank rank, Suit suit) {
        return rank.ordinal() + suit.ordinal() * 4;
    }

    public static int getSpriteIndex(Sprites sprite) {
        return switch (sprite) {
            case CARD_BACK -> 52;
            case BUTTON_ABSENT -> 54;
            case BUTTON_PRESENT -> 55;
            case MENU_BACKGROUND -> 56;


            default -> 53; // CARD_PLACEHOLDER
        };
    }


}
