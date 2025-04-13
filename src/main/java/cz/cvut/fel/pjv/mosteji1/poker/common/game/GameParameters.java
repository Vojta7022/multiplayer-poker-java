package cz.cvut.fel.pjv.mosteji1.poker.common.game;

public enum GameParameters {
    SMALL_BLIND(10),
    BIG_BLIND(20),
    STARTING_CHIPS(1000),;

    public final int value;

    GameParameters(int value) {
        this.value = value;
    }
}
