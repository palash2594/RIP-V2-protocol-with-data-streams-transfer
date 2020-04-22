import java.io.*;
import java.net.*;

/**
 * this class sends the self data and waits for acknowledgement.
 */

public class SendSelfData {
    private DatagramSocket socket;
    private InetAddress address;

    private int packetNumber = 0;
    private byte[] dataToSend;
    private InetAddress destinationIP;
    private String uniquePacketIdentifier;
    private long packetSentTime;

    public SendSelfData() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public byte[] convertIPToByteArray(InetAddress ipAddress) {
        String[] address = ipAddress.getHostAddress().trim().split("\\.");
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(address[i]);
        }
        return ipBytes;
    }

    public byte[] fillUpDataToSend(byte[] bufferFileData, InetAddress destinationIP) throws UnknownHostException {
        packetNumber = (packetNumber + 1) % 127;
        // first byte for packet/ acknowledgement
        byte firstByte = 0;

        // add source address
        InetAddress sourceIPAddress = InetAddress.getLocalHost();
        byte[] sourceIPBytes = convertIPToByteArray(sourceIPAddress);

        // add destination address
        byte[] destinationIPBytes = convertIPToByteArray(destinationIP);

        // add unique packet identifier -> sender ID + packet number
        uniquePacketIdentifier = String.valueOf(DataStore.getPodID()).concat(String.valueOf(packetNumber));
        byte[] uniquePacketIdentifierBytes = new byte[2];
        uniquePacketIdentifierBytes[0] = (byte) DataStore.getPodID();
        uniquePacketIdentifierBytes[1] = (byte) packetNumber;

        dataToSend = new byte[bufferFileData.length + sourceIPBytes.length +
                destinationIPBytes.length + uniquePacketIdentifierBytes.length + 1];

        // filling up all the data in dataToSend.
        dataToSend[0] = firstByte;
        System.arraycopy(sourceIPBytes, 0, dataToSend, 1, sourceIPBytes.length);
        System.arraycopy(destinationIPBytes, 0, dataToSend, sourceIPBytes.length + 1,
                destinationIPBytes.length);
        System.arraycopy(uniquePacketIdentifierBytes, 0, dataToSend,
                sourceIPBytes.length + destinationIPBytes.length + 1
                , uniquePacketIdentifierBytes.length);
        System.arraycopy(bufferFileData, 0, dataToSend, sourceIPBytes.length +
                destinationIPBytes.length + uniquePacketIdentifierBytes.length + 1, bufferFileData.length);

        return dataToSend;
    }

    public void waitForAcknowledgement() throws IOException {
        // wait for the acknowledgement which matches the uniquePacketIdentifier.
        while (true) {
            if (DataStore.acknowledgementID.equals(uniquePacketIdentifier)) {
                // acknowledgement received.
                DataStore.acknowledgementID = "NA";
                break;
            }
            if (System.currentTimeMillis() - packetSentTime > 1000) {
                // timeout for acknowledgement.
                sendData();
            }
        }
    }

    public void sendData() throws IOException {
        socket = new DatagramSocket(4446);
        DatagramPacket packet
                = new DatagramPacket(dataToSend, dataToSend.length, destinationIP, 4445);
        DataStore.getPacketsQueue().add(packet);
        socket.send(packet);
        socket.close();

        packetSentTime = System.currentTimeMillis();

        waitForAcknowledgement();
    }

    public void sendFileData(InetAddress destinationIP, String fileName) {
        this.destinationIP = destinationIP;
        BufferedInputStream bufferedInputStream = null;
        try {
            System.out.println("Reading file.");
            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
        } catch (FileNotFoundException exception) {
            System.err.println("File error " + exception.getMessage());
        }

        byte[] bufferFileData = new byte[30000];
        int offset = 0;

        try {
            while ((offset = bufferedInputStream.read(bufferFileData)) > 0) {
                fillUpDataToSend(bufferFileData, destinationIP);
                sendData();
                waitForAcknowledgement();
            }

            // making it zero for the next file.
            packetNumber = 0;

        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
