package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.server.Server;
import cz.cvut.fel.pjv.mosteji1.poker.utils.MyUtils;

/*
 To allow external clients to connect to the server on port 12345,
 you need to create an inbound rule in Windows Firewall:

 1. Open the "Windows Defender Firewall with Advanced Security".
 2. In the left pane, click on "Inbound Rules".
 3. In the right pane, click on "New Rule...".
 4. Choose "Port" and click "Next".
 5. Select "TCP", and in "Specific local ports", enter: 12345
 6. Click "Next", then choose "Allow the connection".
 7. Click "Next", and make sure all profiles (Domain, Private, Public) are checked.
 8. Click "Next", give the rule a name like "Poker Server TCP 12345", and click "Finish".

 After this, clients on the same network should be able to connect using your local IP (e.g., 192.168.x.x).
*/


/**
 * Entry point for the server-side of the poker application.
 * <p>
 * Before starting the server, ensure that port 12345 is open in the Windows Firewall,
 * following the instructions provided in the comment above. The server will then start
 * and listen on this port for incoming client connections on the same network.
 *
 * @see cz.cvut.fel.pjv.mosteji1.poker.server.Server
 */
public class ServerMain {

    /**
     * Main method to start the server.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--debug")) {
            MyUtils.initializeLogger(args);
        }

        Server server = new Server();
        server.startServer();
    }
}
