import javax.xml.crypto.Data;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;

/**
 * this class sends the self data and waits for acknowledgement.
 */

public class SendSelfData extends Thread {
    private DatagramSocket socket;
    private InetAddress address;

    private int packetNumber = 0;
    private byte[] dataToSend;
    private InetAddress destinationIP;
    private String uniquePacketIdentifier;
    private long packetSentTime;
    private String fileName;
    private String EOF = "EOF";

    public void run() {
        System.out.println("inside send data");
        sendFileData();
    }

    public SendSelfData(InetAddress destinationIP, String fileName) throws SocketException, UnknownHostException {
        System.out.println("Hello");
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        this.destinationIP = destinationIP;
        this.fileName = fileName;
    }

    public String findTheNextHopIp() {
        String nextHopIP = DataStore.getAddressToIPMapping().get(destinationIP.getHostAddress().trim());
        return nextHopIP;
    }

    public byte[] convertIPToByteArray(InetAddress ipAddress) {
        String[] address = ipAddress.getHostAddress().trim().split("\\.");
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(address[i]);
        }
        return ipBytes;
    }

    public byte[] getUniquePacketIdentifier() {
        byte[] uniquePacketIdentifierBytes = new byte[2];
        uniquePacketIdentifierBytes[0] = (byte) DataStore.getPodID();
        uniquePacketIdentifierBytes[1] = (byte) packetNumber;

        System.out.println("UID : ");
        System.out.println(uniquePacketIdentifierBytes[0] + " " + uniquePacketIdentifierBytes[1]);
        return uniquePacketIdentifierBytes;
    }

    public byte[] getOffset(int offset) {
        String binary = Integer.toBinaryString(offset);
        int len = binary.length();

        for (int i = 0; i < 16 - len; i++) {
            binary = "0" + binary;
        }
        byte[] offsetBytes = new BigInteger(binary, 2).toByteArray();

        return offsetBytes;
    }

    public void fillUpDataToSend(byte[] bufferFileData, int offset) throws UnknownHostException {
        packetNumber = (packetNumber + 1) % 127;
        // first byte for packet/ acknowledgement
        byte firstByte = 0;

        // add source address -> 4 bytes.
        InetAddress sourceIPAddress = InetAddress.getByName(DataStore.getPodAddress());
        byte[] sourceIPBytes = convertIPToByteArray(sourceIPAddress);

        // add destination address -> 4 bytes.
        byte[] destinationIPBytes = convertIPToByteArray(destinationIP);

        // add unique packet identifier -> sender ID + packet number -> 2 bytes.
        uniquePacketIdentifier = String.valueOf(DataStore.getPodID()) + "_" + String.valueOf(packetNumber);
        byte[] uniquePacketIdentifierBytes = getUniquePacketIdentifier();

        // offset -> 2 bytes.
        byte[] offsetBytes = getOffset(offset);

        // declaring the data in bytes.
        dataToSend = new byte[bufferFileData.length + sourceIPBytes.length +
                destinationIPBytes.length + uniquePacketIdentifierBytes.length + offsetBytes.length + 1];

        System.out.println("size " + dataToSend.length);

        // filling up all the data in dataToSend.
        dataToSend[0] = firstByte;
        System.arraycopy(sourceIPBytes, 0, dataToSend, 1, sourceIPBytes.length);
        System.arraycopy(destinationIPBytes, 0, dataToSend, sourceIPBytes.length + 1,
                destinationIPBytes.length);
        System.arraycopy(uniquePacketIdentifierBytes, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length + 1
                , uniquePacketIdentifierBytes.length);
        System.arraycopy(offsetBytes, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length +
                        uniquePacketIdentifierBytes.length + 1, offsetBytes.length);
        System.arraycopy(bufferFileData, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length +
                        uniquePacketIdentifierBytes.length +
                        offsetBytes.length + 1,
                bufferFileData.length);

        System.out.println("reached until here.");

    }

    public void buildFirstPacket() throws UnknownHostException {
        System.out.println("sending first packet.");
        packetNumber = (packetNumber + 1) % 127;

        // data indicator byte -> 1 byte.
        byte firstByte = 0;

        // add source address -> 4 bytes.
        InetAddress sourceIPAddress = InetAddress.getByName(DataStore.getPodAddress());
        byte[] sourceIPBytes = convertIPToByteArray(sourceIPAddress);

        // add destination address -> 4 bytes.
        byte[] destinationIPBytes = convertIPToByteArray(destinationIP);

        // add unique packet identifier -> sender ID + packet number -> 2 bytes.
        uniquePacketIdentifier = String.valueOf(DataStore.getPodID()) + "_" + String.valueOf(packetNumber);
        byte[] uniquePacketIdentifierBytes = getUniquePacketIdentifier();

        // name of the file.
        byte[] fileNameBytes = fileName.getBytes();

        // declaring the data in bytes.
        dataToSend = new byte[fileNameBytes.length + sourceIPBytes.length +
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
        System.arraycopy(fileNameBytes, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length +
                        uniquePacketIdentifierBytes.length + 1,
                fileNameBytes.length);
        System.out.println("first packet length: " + dataToSend.length);
    }

    public void buildLastPacket() throws UnknownHostException {
        System.out.println("sending first packet.");
        packetNumber = (packetNumber + 1) % 127;

        // data indicator byte -> 1 byte.
        byte firstByte = 0;

        // add source address -> 4 bytes.
        InetAddress sourceIPAddress = InetAddress.getByName(DataStore.getPodAddress());
        byte[] sourceIPBytes = convertIPToByteArray(sourceIPAddress);

        // add destination address -> 4 bytes.
        byte[] destinationIPBytes = convertIPToByteArray(destinationIP);

        // add unique packet identifier -> sender ID + packet number -> 2 bytes.
        uniquePacketIdentifier = String.valueOf(DataStore.getPodID()) + "_" + String.valueOf(packetNumber);
        byte[] uniquePacketIdentifierBytes = getUniquePacketIdentifier();

        // name of the file.
        byte[] endOfFileBytes = EOF.getBytes();

        // declaring the data in bytes.
        dataToSend = new byte[endOfFileBytes.length + sourceIPBytes.length +
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
        System.arraycopy(endOfFileBytes, 0, dataToSend,
                sourceIPBytes.length +
                        destinationIPBytes.length +
                        uniquePacketIdentifierBytes.length + 1,
                endOfFileBytes.length);
        System.out.println("first packet length: " + dataToSend.length);
    }

    public void waitForAcknowledgement() throws IOException {
        // wait for the acknowledgement which matches the uniquePacketIdentifier.
        while (true) {
            if (DataStore.acknowledgementID.equals(uniquePacketIdentifier)) {
                // acknowledgement received.
//                DataStore.acknowledgementID = "NA";
                System.out.println("hellos " + DataStore.acknowledgementID);
                break;
            }
            if (System.currentTimeMillis() - packetSentTime > 100) {
                // timeout for acknowledgement.
                sendData();
            }
        }
    }

    private static String checksum(String filepath, MessageDigest md) throws IOException {

        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

    public void sendData() throws IOException {
        System.out.println("sending this packet " + packetNumber);
        socket = new DatagramSocket(4449);
        InetAddress nextHopIP = InetAddress.getByName(findTheNextHopIp());
        DatagramPacket packet
                = new DatagramPacket(dataToSend, dataToSend.length, nextHopIP, 4445);
        DataStore.getPacketsQueue().add(packet);
        socket.send(packet);
        socket.close();

        packetSentTime = System.currentTimeMillis();

        waitForAcknowledgement();
    }

    public void sendFileData() {
        this.destinationIP = destinationIP;
        BufferedInputStream bufferedInputStream = null;
        try {
            System.out.println("Reading file.");
            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
        } catch (FileNotFoundException exception) {
            System.err.println("File error " + exception.getMessage());
        }

        byte[] bufferFileData = new byte[DataStore.PACKET_DATA_SIZE];
        int offset = 0;

        try {
            buildFirstPacket();
            sendData();
            waitForAcknowledgement();

            System.out.println("first packet sent");
            while ((offset = bufferedInputStream.read(bufferFileData)) > 0) {
                fillUpDataToSend(bufferFileData, offset);
                sendData();
                waitForAcknowledgement();
            }

            buildLastPacket();
            sendData();
            waitForAcknowledgement();

            // making it zero for the next file.
            packetNumber = 0;

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hex = checksum(System.getProperty("user.dir") + "//" + fileName, md);
            System.out.println("\nHash of the file: \n" + hex);

        } catch (Exception e) {
            System.out.println("this exception");
            System.out.println(e);
        }

    }
}
