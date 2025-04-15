package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;

public class ServerMain {

    public static void main(String[] args) {
        Server server = new Server();
        Table table = new Table(server);
    }
}
