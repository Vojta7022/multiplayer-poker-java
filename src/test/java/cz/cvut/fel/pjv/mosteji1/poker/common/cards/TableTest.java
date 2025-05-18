package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.common.player.Player;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TableTest {

    private Table table;

    @BeforeEach
    public void setUp() {
        Server server = new Server();
        table = server.getTable();
    }

    @Test
    public void testTableInitialization() {
        assertNotNull(table.getPlayers());
        assertNotNull(table.getCommunityCards());
        assertEquals(0, table.getPotSize());
        assertEquals(0, table.getBetThreshold());
        assertEquals(0, table.getDealerIndex());
        assertEquals(2, table.getWaitingForIndex());
    }

    @Test
    public void testAddPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Player1", 1, 1000));
        players.add(new Player("Player2", 2, 1000));

        table.addPlayersFromPointers(players);

        assertEquals(2, table.getPlayers().size());
        assertEquals("Player1", table.getPlayers().get(0).getName());
        assertEquals("Player2", table.getPlayers().get(1).getName());
    }

/*    @Test
    public void testPlaceBlinds() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Player1", 1, 1000));
        players.add(new Player("Player2", 2, 1000));
        table.addPlayersFromPointers(players);

        table.placeBlinds(0);

        assertEquals(990, table.getPlayers().get(0).getChips()); // Small blind
        assertEquals(980, table.getPlayers().get(1).getChips()); // Big blind
        assertEquals(30, table.getPotSize());
    }

    @Test
    public void testDealHoleCards() {
        ArrayList<Player> players = new ArrayList<>();
        table.resetTable();
        players.add(new Player("Player1", 1, 1000));
        players.add(new Player("Player2", 2, 1000));
        table.addPlayersFromPointers(players);

        table.dealHoleCards();

        for (Player player : table.getPlayers()) {
            assertEquals(2, player.getHand().size());
        }
    }

    @Test
    public void testResetTable() {
        table.resetTable();

        assertEquals(0, table.getCommunityCards().size());
        assertEquals(0, table.getPotSize());
        assertEquals(0, table.getBetThreshold());
    }*/
}
