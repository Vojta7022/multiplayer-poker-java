package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Table {
    private List<Player> players;
    private final Scanner scanner;
    private TableRound currentRound;
    private int currentDealerIndex;
    private int potSize;

    public Table() {
        this.players = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void newRound() {
        for (Player player : players) {
            player.discardCards();
        }
        currentRound = new TableRound(this);
    }

    public List<Player> getPlayers() {
        return players;
    }



    @Override
    public String toString() {
        return "Table with players: " + players + "\nCommunity Cards: " + currentRound.getCommunityCards();
    }

    public int getCurrentDealerIndex() {
        return currentDealerIndex;
    }
}
