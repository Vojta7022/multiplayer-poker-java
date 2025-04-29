package cz.cvut.fel.pjv.mosteji1.poker.client.gameRepresentation;

import java.io.Serializable;

public record PlayerRepresentation(String name, int avatarIndex,int chips, int bet, boolean folded, boolean isAllIn) implements Serializable {

}
