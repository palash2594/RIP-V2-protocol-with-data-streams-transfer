/**
 *
 * @author: Palash Jain
 *
 * @version: 1.0
 */

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class maintains all the data required globally in the application.
 */

public class DataStore {

    public final static String MULTICAST_IP = "224.0.0.9";
    public final static int INFINITY = 16;
    public final static int TEN_SECONDS = 10;
    public final static int TEN_THOUSAND_MILLISECOND = 10000;
    public final static String DEFAULT_IP = "0.0.0.0";

    public static String acknowledgementID = "NA";

    private static int podID;
    private static String podIP;
    private static String podAddress;
    private static RoutingTable routingTable;
    private static Map<String, String> addressToIPMapping;
    private static Map<String, Boolean> nonRechableDirectly;
    private static ThreadPoolExecutor executor;

    // using synchronized array list.
    private static List<DatagramPacket> packetsQueue = Collections.synchronizedList(new ArrayList<DatagramPacket>());
    private static Map<String, String> nextHopIPToPodIP;

    public static Map<String, String> getNextHopIPToPodIP() {
        return nextHopIPToPodIP;
    }

    public static void setNextHopIPToPodIP(Map<String, String> nextHopIPToPodIP) {
        DataStore.nextHopIPToPodIP = nextHopIPToPodIP;
    }

    public static List<DatagramPacket> getPacketsQueue() {
        return packetsQueue;
    }

    public static void setPacketsQueue(ArrayList<DatagramPacket> packetsQueue) {
        DataStore.packetsQueue = packetsQueue;
    }

    public static int getPodID() {
        return podID;
    }

    public static void setPodID(int podID) {
        DataStore.podID = podID;
    }

    public static String getPodIP() {
        return DataStore.podIP;
    }

    public static void setPodIP(String podIP) {
        DataStore.podIP = podIP;
    }

    public static String getPodAddress() {
        return podAddress;
    }

    public static void setPodAddress(String podAddress) {
        DataStore.podAddress = podAddress;
    }

    public static RoutingTable getRoutingTable() {
        return routingTable;
    }

    public static void setRoutingTable(RoutingTable routingTable) {
        DataStore.routingTable = routingTable;
    }

    public static Map<String, String> getAddressToIPMapping() {
        return addressToIPMapping;
    }

    public static void setAddressToIPMapping(Map<String, String> addressToIPMapping) {
        DataStore.addressToIPMapping = addressToIPMapping;
    }

    public static Map<String, Boolean> getNonRechableDirectly() {
        return nonRechableDirectly;
    }

    public static void setNonRechableDirectly(Map<String, Boolean> isSet) {
        DataStore.nonRechableDirectly = isSet;
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static void setExecutor(ThreadPoolExecutor executor) {
        DataStore.executor = executor;
    }
}
