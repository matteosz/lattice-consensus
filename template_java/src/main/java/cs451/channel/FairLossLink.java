package cs451.channel;

import cs451.message.Packet;
import cs451.parser.Host;

import cs451.utilities.Parameters;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.channel.Network.getProcess;
import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.parser.HostsParser.hosts;
import static cs451.process.Process.myHost;

/**
 * The lowest abstraction for the channels.
 *
 * Main functions:
 * 1) It uses a blocking queue to store datagram packets to send and a thread infinitely polling
 *    from the queue to send through the socket.
 * 2) Another thread listens to the socket and deliver packets to upper layers.
 */
public class FairLossLink {

    /** UDP socket */
    private static DatagramSocket socket;

    /**  Blocking queue containing datagram packets to be sent */
    private static final BlockingQueue<DatagramPacket> datagramsToSend = new LinkedBlockingQueue<>();

    /** Running flag to stop the threads when the application stops */
    private static final AtomicBoolean running = new AtomicBoolean(true);

    /** Consumer function given by the upper layer, called in deliver time */
    private static Consumer<Packet> packetCallback;

    /**
     * Initialize the FairLoss Link.
     * @param packetCallback a consumer function to call the delivery of the upper layer
     * @throws SocketException
     */
    public static void start(Consumer<Packet> packetCallback) throws SocketException {
        FairLossLink.packetCallback = packetCallback;
        socket = new DatagramSocket(hosts.get(myHost).getPort());
        ExecutorService workers = Executors.newFixedThreadPool(2);
        workers.execute(FairLossLink::dequeuePacket);
        workers.execute(FairLossLink::receivePackets);
    }

    /**
     * Create a datagram packet and insert it in the blocking queue.
     * @param packet packet to be serialized and inserted in the queue
     * @param target host's id of the future recipient
     */
    public static void enqueuePacket(Packet packet, byte target) {
        // Get the recipient's information
        Host targetHost = getProcess(target).getHost();
        // Serialize the packet as a byte array
        byte[] buffer = packet.getBytes();
        if (Parameters.DEBUG) {
            System.out.println("Sending through fair-loss new packet to p: " + target);
            System.out.println(packet);
        }
        try {
            // The method put will insert the element if the space is available, otherwise it'll wait
            datagramsToSend.put(new DatagramPacket(buffer, buffer.length, targetHost.getIpAsAddress(), targetHost.getPort()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Poll UDP datagrams from the queue and send them through the socket
     */
    private static void dequeuePacket() {
        while (running.get()) {
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
     * deserialize them and deliver to the upper layer
     */
    private static void receivePackets() {
         while (running.get()) {
            // In the worst scenario the buffer will have the max UDP packet size
            DatagramPacket datagramPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            try {
                // The method receive will block until a datagram is received
                socket.receive(datagramPacket);
                // Call the consumer function of the deserialized packet
                packetCallback.accept(new Packet(datagramPacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
    }

    /**
     * Set atomically the running flag to false
     */
    public static void stopThreads() {
        running.set(false);
    }

}

