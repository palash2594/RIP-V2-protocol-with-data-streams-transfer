/**
 *
 * @author: Palash Jain
 *
 * @version: 1.0
 */

import java.io.BufferedOutputStream;
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
    public final static int PACKET_DATA_SIZE = 20000;
    public final static int PACKET_TOTAL_SIZE = 20000 + 13;

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
    private static Map<String, String> podIPtoNextHopIP;
    private static Map<String, List<byte[]>> outputData;
    private static Map<String, BufferedOutputStream> outputStreams;

    public static Map<String, BufferedOutputStream> getOutputStreams() {
        return outputStreams;
    }

    public static void setOutputStreams(Map<String, BufferedOutputStream> outputStreams) {
        DataStore.outputStreams = outputStreams;
    }

    public static Map<String, List<byte[]>> getOutputData() {
        return outputData;
    }

    public static void setOutputData(Map<String, List<byte[]>> outputData) {
        DataStore.outputData = outputData;
    }

    public static Map<String, String> getPodIPtoNextHopIP() {
        return podIPtoNextHopIP;
    }

    public static void setPodIPtoNextHopIP(Map<String, String> podIPtoNextHopIP) {
        DataStore.podIPtoNextHopIP = podIPtoNextHopIP;
    }

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
