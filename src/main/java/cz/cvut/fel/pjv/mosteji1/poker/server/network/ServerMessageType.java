package cz.cvut.fel.pjv.mosteji1.poker.server.network;

public enum ServerMessageType {
    JOIN_ACCEPT("JOIN_ACCEPT"),

    TABLE_UPDATE("TABLE_UPDATE"),
    SERVER_FAIL("SERVER_FAIL"),
    PLAYER_WON("PLAYER_WON"),;

    public final String message;

    ServerMessageType(String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }

    public static ServerMessageType fromString(String message) {
        for (ServerMessageType type : ServerMessageType.values()) {
            if (type.message.equalsIgnoreCase(message)) {
                return type;
            }
        }
        return null;
    }
}
