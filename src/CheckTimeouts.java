/**
 *
 * @author: Palash Jain
 *
 * @version: 1.0
 */

import java.util.Map;

/**
 *
 * This class performs the following fucntions:
 *  1. Checks the routing table in every 10 seconds to if any node update is
 *  more than 10 seconds old.
 *  2. If it finds a node with last updated time more than 10 seconds old,
 *  then it make it unreachable by setting the cost to infinity (16).
 *  Then it parses the routing table and removes the reference of all the nodes
 *  using the old node as next hop, and makes all those nodes unreachable.
 */

public class CheckTimeouts extends Thread {

    /**
     * this run method checks the routing table in every 10 seconds to see if
     * a node is unreachable. If a node has no update in the last 10 seconds
     * then it will set the cost to INFINITY and treat it as unreachable.
     */
    public void run() {
        // storing nodes in this to not set its cost to INFINITY again.
        // storing only those nodes who are not directly reachable.
        Map<String, Boolean> nonRechableDirectly = DataStore.getNonRechableDirectly();
        try {
            Map<String, TableEntry> myRoutingTable = DataStore.getRoutingTable().getRoutingTable();
            String myAddress = DataStore.getPodAddress();
            ReceivePacket receivePacket = new ReceivePacket();
            while (true) {
                for (Map.Entry<String, TableEntry> entry : myRoutingTable.entrySet()) {

                    if (!entry.getValue().getAddress().equals(myAddress)) {
                        TableEntry currentEntry = entry.getValue();

                        // consider only those nodes who have exceeded 10 seconds.
                        if (((System.currentTimeMillis() - currentEntry.getTime()) / 1000) > DataStore.TEN_SECONDS) {// && !nonRechableDirectly.containsKey(currentEntry.getAddress())) {
//                            nonRechableDirectly.put(currentEntry.getAddress(), true);
                            currentEntry.setCost(DataStore.INFINITY);
                            removeAllUsageOfUnreachableIP(currentEntry.getAddress());
                            receivePacket.displayRoutingTable();
                        }
                    }
                }
                try {
                    sleep(DataStore.TEN_THOUSAND_MILLISECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * this method removes the usage of every unreachable node in the
     * routing table.
     * @param unreachableAddress
     */
    public void removeAllUsageOfUnreachableIP(String unreachableAddress) {
        String unreachableIP = DataStore.getAddressToIPMapping().get(unreachableAddress);
        Map<String, TableEntry> myRoutingTable = DataStore.getRoutingTable().getRoutingTable();

        for (Map.Entry<String, TableEntry> entry : myRoutingTable.entrySet()) {
            TableEntry currentEntry = entry.getValue();
            if (currentEntry.getNextHop().equals(unreachableIP)) {
                currentEntry.setCost(DataStore.INFINITY);
                currentEntry.setNextHop(DataStore.DEFAULT_IP);
            }
        }
    }
}
