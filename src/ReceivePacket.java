/**
 * @author: Palash Jain
 * @version: 1.0
 */

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;

/**
 * This class receives and decodes the RIP packet containing the routing table
 * over the multicast channel and updates its routing table accordingly.
 *
 * This class performs the following functions:
 *  1. It always listens for an incoming RIP packet.
 *  2. After retrieving a packet, it retrieves the contents like sender IP,
 *  routing table and metric.
 *  3. It then updates own routing table by checking if any shorter path
 *  is available.
 *  4. If any shorter path found then sends a triggered update.
 *  5. Prints the routing table.
 */

public class ReceivePacket extends Thread {
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];

    /**
     * this run method continuously listens over the multicast channel for the
     * RIP packet and sends the received packet to update the routing table.
     */
    public void run() {
        try {
            socket = new MulticastSocket(520);
            InetAddress group = InetAddress.getByName(DataStore.MULTICAST_IP);
            socket.joinGroup(group);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String receivedIPAddress = packet.getAddress().toString().substring(1);
                extractRoutingTable(packet.getData(), packet.getLength(), receivedIPAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method extracts the routing table from the received RIP packet.
     * @param receivedPacket
     * @param length
     * @param receivedIP
     * @throws IOException
     */
    public synchronized void extractRoutingTable(byte[] receivedPacket, int length, String receivedIP) throws IOException {
        if (receivedIP.equals(DataStore.getPodIP())) {
            // its own packet, ignore.
            return;
        }
        RIPPacket ripPacket = new RIPPacket();
        // Parsing packet to get the routing table.
        RoutingTable receivedRoutingTable = ripPacket.readPacket(receivedPacket, length);

        updateRoutingTable(receivedRoutingTable, receivedIP);

    }

    /**
     * this method takes the received routing table and updates its own routing table.
     * @param receivedRoutingTable
     * @param receivedIP
     * @throws IOException
     */
    public void updateRoutingTable(RoutingTable receivedRoutingTable, String receivedIP) throws IOException {
        Map<String, TableEntry> myRoutingTable = DataStore.getRoutingTable().getRoutingTable();
        boolean triggerFlag = false;
        try {
            for (Map.Entry<String, TableEntry> entry : receivedRoutingTable.getRoutingTable().entrySet()) {
                TableEntry currentTableEntry = entry.getValue();

                String currentEntryAddress = currentTableEntry.getAddress();

                if (!DataStore.getAddressToIPMapping().containsKey(currentEntryAddress)) {
                    DataStore.getAddressToIPMapping().put(currentEntryAddress, receivedIP);
                }

                // proj 4 addition.
                if (currentTableEntry.getCost() == 0) {
                    String currentNextHopAddress = currentTableEntry.getNextHop();
                    if (!DataStore.getNextHopIPToPodIP().containsKey(currentNextHopAddress)) {
                        DataStore.getNextHopIPToPodIP().put(currentNextHopAddress, currentEntryAddress);
                    }
                }

                // calculating new cost.
                int newCost = currentTableEntry.getCost() + 1;

                // new entry.
                if (!myRoutingTable.containsKey(entry.getKey())) {
                    if (currentTableEntry.getCost() != DataStore.INFINITY) {
                        // setting next hop ip to the ip of the sending node (receivedIP)
                        currentTableEntry.setNextHop(receivedIP);
                        currentTableEntry.setCost(newCost);
                    }
                    myRoutingTable.put(entry.getKey(), currentTableEntry);
                    myRoutingTable.get(entry.getKey()).setTime(System.currentTimeMillis());
                } else { // existing entry.

                    // TODO: 3/1/20 changing cost to the cost received by via node to destination node.
                    if (myRoutingTable.get(currentTableEntry.getAddress()).getNextHop().equals(receivedIP)) {
                        if (newCost >= DataStore.INFINITY + 1) {
                            myRoutingTable.get(currentEntryAddress).setNextHop(DataStore.DEFAULT_IP);
                            myRoutingTable.get(currentEntryAddress).setCost(DataStore.INFINITY);
                            continue;
                        }
                        myRoutingTable.get(entry.getKey()).setCost(newCost);
                        myRoutingTable.get(entry.getKey()).setTime(System.currentTimeMillis());
                        continue;
                    }

                    // checking if the new cost is lower than the previous cost.
                    if (newCost < myRoutingTable.get(entry.getKey()).getCost()) {
                        myRoutingTable.get(entry.getKey()).setCost(newCost);
                        myRoutingTable.get(entry.getKey()).setNextHop(receivedIP);
                        myRoutingTable.get(entry.getKey()).setTime(System.currentTimeMillis());

                        triggerFlag = true;
                    }
                }

                // proj 4 addition.
                DataStore.getAddressToIPMapping().put(currentEntryAddress,
                        myRoutingTable.get(currentEntryAddress).getNextHop());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
//        displayRoutingTable();

        // TODO: 4/20/20 call display when a new entry comes.

        if (triggerFlag) {
            SendPacket sendPacket = new SendPacket();
            sendPacket.sendPacket();

            displayRoutingTable();
        }
    }

    /**
     * this method displays the routing table.
     */
    public void displayRoutingTable() {
        Map<String, TableEntry> routingTable = DataStore.getRoutingTable().getRoutingTable();
        System.out.println("Routing table for node : " + DataStore.getPodID());
        System.out.println("|-------------------------------------------------------|");
        System.out.println("| Address\t| Next Hop\t| Cost\t| Time\t\t|");
        System.out.println("|-------------------------------------------------------|");
        for (Map.Entry<String, TableEntry> entry : routingTable.entrySet()) {
            TableEntry currentEntry = entry.getValue();
            System.out.print("| " + currentEntry.getAddress() + "  \t| ");
            System.out.print(currentEntry.getNextHop() + "\t| ");
            System.out.print(currentEntry.getCost() + "\t| ");
            System.out.println(currentEntry.getTime() + "\t|");
        }
        System.out.println("|-------------------------------------------------------|");
    }

}
