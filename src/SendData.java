import java.io.IOException;
import java.net.*;

public class SendData {
    private DatagramSocket socket;
    private InetAddress address;

    public SendData() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void waitForAcknowledgement() {
        // wait for the acknowledgement which matches the uniquePacketIdentifier.

    }

    public void extractUniqueIdentifier(byte[] data){
        // TODO: 4/21/20 assumed that UID present in bytes 9 and 10


    }

    public void sendDataFromQueue() throws IOException {


        while (true) {
            // keep checking if there are any packets to send in the queue
            socket = new DatagramSocket(4446);

            if (DataStore.getPacketsQueue().size() > 0) {
                DatagramPacket packet = DataStore.getPacketsQueue().get(0);

//                DataStore.acknowledgementID = extractUniqueIdentifier(packet.getData());

                socket.send(packet);
                socket.close();

                waitForAcknowledgement();
            }
        }
    }
}
