package cz.cvut.fel.pjv.mosteji1.poker.common.game;

import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Card;
import cz.cvut.fel.pjv.mosteji1.poker.common.cards.Deck;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.network.ServerEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Table {
    private List<Player> players;
    private final Scanner scanner;
    private TableRound currentRound;
    private int currentDealerIndex;


    public Table(List<ServerEndpoint> endpoints) {
        this.players = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.currentDealerIndex = 0;
        this.currentRound = new TableRound(this, currentDealerIndex);

        for (ServerEndpoint endpoint : endpoints) {
            Player player = new Player(endpoint.getName(), 1000);
            players.add(player);

        }
    }

    private Player getNextRoundsWinner() {
        currentRound = initNewRound();
        currentRound.postBlinds();
        currentRound.dealHoleCards();

        for (int i = 0; i < players.size(); i++) {
            players.get((i + currentDealerIndex + 2) % players.size()).getBet();
        }

        letPlayersBet();
        /* if only one player remains, return them */

        currentRound.dealFlop();
        letPlayersBet();
        /* if only one player remains, return them */

        currentRound.dealTurn();
        letPlayersBet();
        /* if only one player remains, return them */

        currentRound.dealRiver();
        letPlayersBet();
        /* if only one player remains, return them */
        return players.get(currentDealerIndex); // replace with player with the strongest CardCombo

    }

    private void letPlayersBet() {
        while (true /* There is a player who hasnt folded and hasnt bet enough */) {
            /* Cycle through players and let them bet*/

        }
    }




    public void addPlayer(Player player) {
        players.add(player);
    }

    private TableRound initNewRound() {

        currentDealerIndex = (currentDealerIndex + 1) % players.size();
        for (Player player : players) {
            player.discardCards();
        }
        return new TableRound(this, currentDealerIndex);
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
