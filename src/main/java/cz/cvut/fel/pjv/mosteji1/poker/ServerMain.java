package cz.cvut.fel.pjv.mosteji1.poker;

import cz.cvut.fel.pjv.mosteji1.poker.common.game.Table;
import cz.cvut.fel.pjv.mosteji1.poker.server.Server;

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


public class ServerMain {

    public static void main(String[] args) {
        Server server = new Server();
    }
}
