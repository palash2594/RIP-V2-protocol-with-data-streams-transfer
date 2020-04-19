/**
 *
 * @author: Palash Jain
 *
 * @version: 1.0
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class sends the RIP packet containing the routing table
 * over the multicast channel in every 5 seconds.
 */

public class SendPacket extends Thread {

    private DatagramSocket socket = null;
    private InetAddress group;
    private int count = 1; // if count is one that means request command will be used.

    /**
     * this method sends the RIP packet over the multicast channel.
     * @throws IOException
     */
    public void sendPacket() throws IOException {
        socket = new DatagramSocket();
        group = InetAddress.getByName(DataStore.MULTICAST_IP);
        RIPPacket ripPacket = new RIPPacket();
        byte[] packetToSend = ripPacket.preparePacket(count);
        count++;
        DatagramPacket packet = new DatagramPacket(packetToSend, packetToSend.length, group, 520);
        socket.send(packet);
        socket.close();
    }

    /**
     * this run method calls the sendPacket() method every 5 seconds.
     */
    public void run() {
        try {
            while (true) {
                sendPacket();
                sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
