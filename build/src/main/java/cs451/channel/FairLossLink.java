package cs451.channel;

import cs451.message.Packet;
import cs451.parser.Host;

import cs451.service.CommunicationService;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static cs451.channel.Network.getProcess;
import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.parser.HostsParser.hosts;
import static cs451.process.Process.MY_HOST;

/**
 * The lowest abstraction for the channels.
 * <p>
 * Main functions:
 * 1) It uses a blocking queue to store the datagram packets to send and a thread infinitely polling
 *    from the queue to send through the socket.
 * 2) Another thread listens to the socket and deliver packets to upper layers.
 */
public class FairLossLink {

    /** UDP socket. */
    private static DatagramSocket socket;

    /**  Blocking queue containing datagram packets to be sent. */
    private static final BlockingQueue<DatagramPacket> datagramsToSend = new LinkedBlockingQueue<>();

    /**
     * Initialize the FairLoss Link.
     * @throws SocketException
     */
    public static void start() throws SocketException {
        socket = new DatagramSocket(hosts.get(MY_HOST).getPort());
        ExecutorService workers = Executors.newFixedThreadPool(2);
        workers.execute(FairLossLink::dequeuePacket);
        workers.execute(FairLossLink::receivePackets);
    }

    /**
     * Create a datagram packet and insert it in the blocking queue.
     * @param packet packet to be serialized and inserted in the queue.
     * @param target host's id of the future recipient.
     */
    public static void enqueuePacket(Packet packet, byte target) {
        Host targetHost = getProcess(target).getHost();
        // Serialize the packet as a byte array
        byte[] buffer = packet.getBytes();
        try {
            // The method put will insert the element if the space is available, otherwise it'll wait
            datagramsToSend.put(new DatagramPacket(buffer, buffer.length, targetHost.getIpAsAddress(), targetHost.getPort()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Poll UDP datagrams from the queue and send them through the socket.
     */
    private static void dequeuePacket() {
        while (CommunicationService.running.get()) {
            try {
                // The method take will wait if the queue is empty
                socket.send(datagramsToSend.take());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Receive datagram packets through the socket,
     * deserialize them and deliver to the upper layer.
     */
    private static void receivePackets() {
        while (CommunicationService.running.get()) {
            // In the worst scenario the buffer will have the max UDP packet size
            DatagramPacket datagramPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            try {
                // The method receive will block until a datagram is received
                socket.receive(datagramPacket);
                // Call the consumer function on the deserialized packet
                StubbornLink.stubbornDeliver(new Packet(datagramPacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

