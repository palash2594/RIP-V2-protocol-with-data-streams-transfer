/**
 * @author: Palash Jain
 * @version: 1.0
 */

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class listens for all the incoming packets and does two thing:
 * 1. If the packet belongs to the pod itself, then it either writes on
 * the output stream or updates acknowledgement ID.
 * 2. Else it forwards the packet to the next hop.
 */

public class Listener extends Thread {
    private DatagramSocket socket;
    private byte[] buf = new byte[DataStore.PACKET_TOTAL_SIZE];
    private Map<String, String> lastPacketReceived = new HashMap<>();
    private String output = "";
    private Map<String, String> fileNames;
    private Map<String, String> fileHashes;

    public void run() {
        try {
            packetListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function extracts the IP from the packet received.
     * @param data
     * @param start
     * @return
     */
    public String extractIP(byte[] data, int start) {
        String ip = "";

        for (int i = start; i < start + 4; i++) {
            ip += (data[i] & 0xff) + ".";
        }
        ip = ip.substring(0, ip.length() - 1); // removing the last dot (.).
        return ip;
    }

    /**
     * this file extracts the file name from the packet.
     * @param data
     * @param packetLength
     * @return
     */
    public String getFileName(byte[] data, int packetLength) {
        String fileName = new String(Arrays.copyOfRange(data, 11, packetLength));
        return fileName.trim();
    }

    /**
     * this function if the packet is the last function of the file.
     * @param data
     * @param packetLength
     * @return
     */
    public boolean checkIfLastPacket(byte[] data, int packetLength) {
        String eof = new String(Arrays.copyOfRange(data, 11, packetLength));
        if (eof.equals("EOF")) {
            return true;
        }
        return false;
    }

    /**
     * this function filters the data by separating the data part from the packet.
     * @param data
     * @return
     */
    public byte[] filterData(byte[] data) {

        int offset = (data[11] & 0xFF);
        for (int i = 12; i < 13; i++) {
            offset = (offset << 8) | (data[i]) & 0xFF;
        }

        return Arrays.copyOfRange(data, 13, offset + 13);
    }

    /**
     * this function extracts the unique packet identifier from packet.
     * @param packetData
     * @return
     */
    public String getUniquePacketIdentifier(byte[] packetData) {
        String uniquePacketIdentifier = "";
        uniquePacketIdentifier = (packetData[9] & 0xFF) + "_" + (packetData[10] & 0xFF);

        return uniquePacketIdentifier;

    }

    /**
     * this function IP address to the byte array.
     * @param ipAddress
     * @return
     */
    public byte[] convertIPToByteArray(InetAddress ipAddress) {
        String[] address = ipAddress.getHostAddress().trim().split("\\.");
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(address[i]);
        }
        return ipBytes;
    }

    /**
     * this function finds the nextHopIP for the given destination.
     * @param destinationIP
     * @return
     * @throws UnknownHostException
     */
    public InetAddress findTheNextHopIp(InetAddress destinationIP) throws UnknownHostException {
        String nextHopIP = DataStore.getAddressToIPMapping().get(destinationIP.getHostAddress().trim());
        return InetAddress.getByName(nextHopIP);
    }

    /**
     * this function prepares the acknowledgement.
     * @param destinationIP
     * @param receivedPacket
     * @return
     * @throws UnknownHostException
     */
    public byte[] prepareAcknowledgement(String destinationIP, byte[] receivedPacket) throws UnknownHostException {
        byte firstByte = 1;

        // add source address -> 4 bytes.
        InetAddress sourceIPAddress = InetAddress.getLocalHost();
        byte[] sourceIPBytes = convertIPToByteArray(sourceIPAddress);

        // add destination address -> 4 bytes.
        byte[] destinationIPBytes = convertIPToByteArray(InetAddress.getByName(destinationIP));

        // unique packet identifier -> 2 bytes.
        byte[] uniquePacketIdentifierBytes = new byte[2];
        uniquePacketIdentifierBytes[0] = receivedPacket[9];
        uniquePacketIdentifierBytes[1] = receivedPacket[10];

        // declaring the data in bytes.
        byte[] dataToSend = new byte[sourceIPBytes.length +
                destinationIPBytes.length + uniquePacketIdentifierBytes.length + 1];

        // filling up all the data in dataToSend.
        dataToSend[0] = firstByte;
        System.arraycopy(sourceIPBytes, 0, dataToSend, 1, sourceIPBytes.length);
        System.arraycopy(destinationIPBytes, 0, dataToSend, sourceIPBytes.length + 1,
                destinationIPBytes.length);
        System.arraycopy(uniquePacketIdentifierBytes, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length + 1
                , uniquePacketIdentifierBytes.length);

        return dataToSend;


    }

    /**
     * this function handles the foreign packet.
     * @param packetData
     * @param destinationIP
     * @throws IOException
     */
    public void handleForeignDataPacket(byte[] packetData, InetAddress destinationIP) throws IOException {
        // TODO: 4/22/20 find the next hop ip before sending it.
        // forward the received packet as it is.
        System.out.println("Forwarding packet.");
        socket = new DatagramSocket(4446);
        InetAddress nextHopIP = findTheNextHopIp(destinationIP);
        DatagramPacket packet
                = new DatagramPacket(packetData, packetData.length, nextHopIP, 4445);
        DataStore.getPacketsQueue().add(packet);
        socket.send(packet);
        socket.close();
    }

    /**
     * this packet sends the acknowledgment for the received packet.
     * @param destinationIP
     * @param receivedPacket
     * @throws IOException
     */
    public void sendAcknowledgement(String destinationIP, byte[] receivedPacket) throws IOException {
        socket = new DatagramSocket(4446);
        byte[] buffer = prepareAcknowledgement(destinationIP, receivedPacket);
        InetAddress nextHop = findTheNextHopIp(InetAddress.getByName(destinationIP));
        DatagramPacket acknowledgement = new DatagramPacket(buffer, buffer.length, nextHop, 4445);
        socket.send(acknowledgement);
        socket.close();
    }

    /**
     * this function handles the local packet.
     * @param receivedPacket
     * @param packetLength
     * @throws IOException
     */
    public void handleLocalDataPacket(byte[] receivedPacket, int packetLength) throws IOException {
        String sourceIP = extractIP(receivedPacket, 1);
        String uniquePacketIdentifier = getUniquePacketIdentifier(receivedPacket);
        System.out.println("Packet received " + uniquePacketIdentifier);
        Map<String, BufferedOutputStream> streams = DataStore.getOutputStreams();
        if (!streams.containsKey(sourceIP)) {
            // first packet from the source.
            String fileName = getFileName(receivedPacket, packetLength);
            FileOutputStream fout = null;
            if (!fileName.equals("EOF") && !fileHashes.containsKey(sourceIP)) {
                fileNames.put(sourceIP, fileName);
            }

            try {
                output = sourceIP + "_" + fileName;
//                System.out.println("length of file, first packet " + output.length());
                fout = new FileOutputStream(output);
            } catch (FileNotFoundException e) {
                System.out.println("File cannot be created : " + e);
                return;
            }

            BufferedOutputStream bout = new BufferedOutputStream(fout);
            streams.put(sourceIP, bout);

            // the packet number is present at the 10th index.
            lastPacketReceived.put(sourceIP, uniquePacketIdentifier);
        } else {
            // remaining packets.

            // check for duplicate packet.
            if (lastPacketReceived.get(sourceIP).equals(uniquePacketIdentifier)) {
                // duplicate packet.
                sendAcknowledgement(sourceIP, receivedPacket);
                return;
            } else {
                // update the last packet received for the particular source.
                lastPacketReceived.put(sourceIP, uniquePacketIdentifier);
            }

            byte[] filteredData = filterData(receivedPacket);

            try {
                if (filteredData.length < DataStore.PACKET_DATA_SIZE && checkIfLastPacket(receivedPacket, packetLength)) {
                    // last packet
                    streams.get(sourceIP).write(filteredData);
                    streams.get(sourceIP).flush();
                    System.out.println("File received.");

                    System.out.println("\nList of files in the directory, to verify if the file is created:");
                    File f = new File(System.getProperty("user.dir"));
                    File[] list = f.listFiles();
                    for (File f1 : list) {
                        System.out.print(f1.getName() + " | ");
                    }
                    System.out.println();


                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    System.out.println("Received File name: " + fileNames.get(sourceIP));
                    String hex = checksum(System.getProperty("user.dir") + "//" + fileNames.get(sourceIP).trim(), md);
                    System.out.println("\nHash of the file: \n" + hex);

                    // data completely written.
                    streams.remove(sourceIP);
                    fileHashes.put(sourceIP, hex);

                } else {
                    // not the last packet.
                    streams.get(sourceIP).write(filteredData);
                }
            } catch (Exception e) {
                System.err.println("Error while writing on file : " + e);
            }
        }

        if (fileHashes.size() != 0 && streams.size() == 0) {
            System.out.println("\nHashes of the file received from the sources: ");
            for (Map.Entry<String, String> entry : fileHashes.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
        }

        uniquePacketIdentifier = getUniquePacketIdentifier(receivedPacket);
        sendAcknowledgement(sourceIP, receivedPacket);
    }

    /**
     * this function calculates the checksum of the given file.
     * @param filepath
     * @param md
     * @return
     * @throws IOException
     */
    private static String checksum(String filepath, MessageDigest md) throws IOException {

        // DigestInputStream is better, but you also can hash file like this.
        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

    /**
     * this function handles the acknowledgment.
     * @param receivedPacket
     */
    public void handleAcknowledgement(byte[] receivedPacket) {
        String packetIdentifier = String.valueOf(receivedPacket[9] & 0xFF) + "_" +
                String.valueOf(receivedPacket[10] & 0xFF);

        DataStore.acknowledgementID = packetIdentifier;
    }

    /**
     * this packet checks if the packet belongs to its machine.
     * @param receivedPacket
     * @param packetLength
     * @throws IOException
     */
    public void checkPacketDestination(byte[] receivedPacket, int packetLength) throws IOException {
        String destinationIP = extractIP(receivedPacket, 5).trim();
        if (DataStore.getPodAddress().equals(destinationIP)) {
            // packet belongs to itself.
            if (receivedPacket[0] == 0) {
                // data packet -> this is the base station.
                handleLocalDataPacket(receivedPacket, packetLength);
            } else {
                // acknowledgement.
                System.out.println("Acknowledgement received.");
                handleAcknowledgement(receivedPacket);
            }
        } else {
            // foreign packet.
            try {
                byte[] dataToForward = Arrays.copyOfRange(receivedPacket, 0, packetLength);
                handleForeignDataPacket(dataToForward, InetAddress.getByName(destinationIP));
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this is the driver fucntion which listens for the incoming packets.
     * @throws IOException
     */
    public void packetListener() throws IOException {
        System.out.println("listening..");

        fileNames = new HashMap<>();
        fileHashes = new HashMap<>();

        while (true) {
            socket = new DatagramSocket(4445);
            // receiving the packet.
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            byte[] receivedPacket = packet.getData();
            socket.close();
            checkPacketDestination(receivedPacket, packet.getLength());
        }
    }
}
