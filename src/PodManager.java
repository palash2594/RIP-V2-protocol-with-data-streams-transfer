/**
 * @author: Palash Jain
 * @version: 1.0
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This is the main driver classes which initiates all the threads and
 * initializes the global variables.
 */

public class PodManager {

    private static ThreadPoolExecutor executor;

    /**
     * this method does all the initialization required a beginning of a pod
     * starting phase.
     * @param podID
     * @throws UnknownHostException
     */
    public void initialization(int podID) throws UnknownHostException {
        System.out.println("Booting up Pod : " + podID);
        DataStore.setPodID(podID);
        String podAddress = "10.0." + podID + ".0";
        DataStore.setPodAddress(podAddress);

        String podIP = InetAddress.getLocalHost().getHostAddress().trim();
        DataStore.setPodIP(podIP);

        int noOfProcessors = Runtime.getRuntime().availableProcessors();
        // Setting the Threadpool executor.
        DataStore.setExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(10));
        executor = DataStore.getExecutor();

        // initializing the routing table.
        RoutingTable routingTable = new RoutingTable(new ConcurrentHashMap<>());
        DataStore.setRoutingTable(routingTable);

        TableEntry tableEntry = new TableEntry(podAddress, podIP, 0, new Date().getTime());
        DataStore.getRoutingTable().addEntry(podAddress, tableEntry);

        DataStore.setAddressToIPMapping(new HashMap<>());

        DataStore.setNonRechableDirectly(new HashMap<String, Boolean>());

        DataStore.setPacketsQueue(new ArrayList<DatagramPacket>());
        DataStore.setNextHopIPToPodIP(new HashMap<>());
        DataStore.setOutputData(new HashMap<>());
        DataStore.setOutputStreams(new HashMap<>());

    }

    /**
     * this is the main function.
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        PodManager podManager = new PodManager();

        podManager.initialization(Integer.parseInt(args[0]));

        // starting send packet thread.
        podManager.sendPacket();

        // starting receive packet thread.
        podManager.receivePacket();

        //starting timeout checking thread.
        podManager.checkTimeouts();

        podManager.receiveData();

        try {
            if (args.length > 1) {
                InetAddress destinationAddress = InetAddress.getByName(args[1]);
                String fileName = args[2];
                System.out.println(args[1]);

                Thread.sleep(30 * 1000);
                displayRoutingTable();
                Thread.sleep(2 * 1000);

                podManager.sendData(destinationAddress, fileName);
            } else {
                Thread.sleep(25 * 1000);
                displayRoutingTable();
            }
        } catch (Exception e) {
            System.out.println("Improper inputs given.");
        }

    }

    private void takeInputs() throws UnknownHostException, InterruptedException {
        System.out.println("Waiting for network to get stabilize.");
        Thread.sleep(2000);
        Scanner src = new Scanner(System.in);

        PodManager manager = new PodManager();
        manager.receiveData();

        while (true) {
            System.out.println("Press 1 for routing table, 2 for send data:");
            int rt = src.nextInt();
            switch (rt) {
                case 1:
                    displayRoutingTable();
                    break;
                case 2:
                    System.out.println("Enter destination IP and file name");
                    InetAddress destinationIP = InetAddress.getByName(src.next().trim());
                    String fileName = src.next();
                    System.out.println(destinationIP);
                    System.out.println(fileName);
                    manager.sendData(destinationIP, fileName);
            }
        }
    }

    /**
     * this method displays the routing table.
     */
    public static void displayRoutingTable() {
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

    /**
     * this method starts a thread to send RIP packet.
     * @throws InterruptedException
     */
    public void sendPacket() throws InterruptedException {
        try {
            SendPacket sendPacket = new SendPacket();
            executor.submit(sendPacket);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * this method starts thread to receive the RIP packet.
     */
    public void receivePacket() {
        try {
            ReceivePacket receivePacket = new ReceivePacket();
            executor.submit(receivePacket);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * this method starts the thread to check if node went offline or unreachable.
     */
    public void checkTimeouts() {
        try {
            CheckTimeouts checkTimeouts = new CheckTimeouts();
            executor.execute(checkTimeouts);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendData(InetAddress destinationIP, String fileName) {
        try {
            System.out.println("sending data");
            SendSelfData sendSelfData = new SendSelfData(destinationIP, fileName);
            System.out.println("Hi");
            executor.execute(sendSelfData);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void receiveData() {
        try {
            Listener listener = new Listener();
            executor.execute(listener);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
