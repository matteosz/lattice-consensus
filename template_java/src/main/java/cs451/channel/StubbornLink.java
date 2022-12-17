package cs451.channel;

import cs451.message.Packet;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.process.Process;

import java.net.SocketException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.channel.Network.getNetwork;
import static cs451.channel.Network.getProcess;
import static cs451.message.Packet.MAX_COMPRESSION;
import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.message.Packet.HEADER;
import static cs451.process.Process.MY_HOST;
import static cs451.process.Process.proposalsToSend;

/**
 * Abstraction used to do the core work of sending packets.
 *
 * Main functions:
 * 1) Send the packets to all the host accordingly.
 * 2) Deliver once the underlying layer has delivered as well.
 */
public class StubbornLink {

    /** Running flag to stop the thread when the application stops */
    private static final AtomicBoolean running = new AtomicBoolean(true);

    /** Consumer function given by the upper layer, called in deliver time */
    private static Consumer<Packet> packetCallback;

    private static int packetNumber = 1;

    /**
     * Initialize the link and start the thread.
     * @param packetCallback a consumer function to call the delivery of the upper layer
     * @throws SocketException
     */
    public static void start(Consumer<Packet> packetCallback) throws SocketException {
        StubbornLink.packetCallback = packetCallback;
        FairLossLink.start(StubbornLink::stubbornDeliver);
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(StubbornLink::sendPackets);
    }

    /**
     * Consumer function given to the underlying layer.
     * Check if the packet is ack and reset the host's timeout.
     * Otherwise, it sends an ack back and call the delivery of the upper layer
     * @param packet received from fair-loss link
     */
    private static void stubbornDeliver(Packet packet) {
        if (packet.isAck()) {
            Process sender = getProcess(packet.getSenderId());
            // Reset timeout with emission time
            sender.notify(packet.getEmissionTime());
            // Deliver the ack to not resend anymore that packet
            sender.deliverAck(packet.getPacketId());
        } else {
            // Send an ack back, same identical packet only change the isAck flag
            FairLossLink.enqueuePacket(packet.convertToAck(), packet.getSenderId());
            // Deliver to upper layer
            packetCallback.accept(packet);
        }
    }

    /**
     * Core function to send packets.
     * It consists of 2 main phases:
     *  1) Go through the packets to resend, that have not been acked yet
     *     and check their timeout, if over then resend, if already acked stop resending.
     *  2) If at least half of the hosts have enough space, load new packets.
     */
    private static void sendPackets() {
        Collection<Process> processes = getNetwork().values();
        // Index to build the packets' ids, starting from 1
        while (running.get()) {
            boolean hasSpace = false;
            // First try to resend all possible ack for all processes
            Packet sharedPacket = null;
            if (resendPackets(processes)) {
                // Craft the shared packet if enough space
                sharedPacket = craftSharedPacket();
                hasSpace = true;
            }
            byte inc = 0;
            for (Process process : processes) {
                // Send the crafted shared packet
                if (sharedPacket != null) {
                    sendPacket(process, sharedPacket, false);
                }
                // ACK
                int[] ackLen = {HEADER};
                List<Proposal> ack = Process.getNextAckProposals(ackLen, process.getAckToSend());
                if (!ack.isEmpty()) {
                    sendPacket(process, new Packet(ack, packetNumber, MY_HOST, ackLen[0]), true);
                }
                if (hasSpace) {
                    // NACK
                    ackLen[0] = HEADER;
                    ack = Process.getNextAckProposals(ackLen, process.getNackToSend());
                    if (!ack.isEmpty()) {
                        // Use packet number + 1
                        sendPacket(process, new Packet(ack, packetNumber + 1, MY_HOST, ackLen[0]),
                            false);
                        inc = 1;
                    }
                }
            }
            packetNumber += inc + 1;
        }
    }

    /**
     * Try to resend all timed out packets if not acked yet.
     * @param processes collection of all hosts
     * @return true if at least half of all hosts have enough space, false otherwise
     */
    private static boolean resendPackets(Collection<Process> processes) {
        // Initial assumption: everyone has enough space
        int hasSpace = processes.size();
        for (Process process : processes) {
            TimedPacket timedPacket = process.nextPacketToAck();
            // Check if the that packet has not been acked yet to resend
            if (timedPacket != null) {
                if (!process.hasAcked(timedPacket.getPacket().getPacketId())) {
                    // If the current living time of the packet is greater than host's timeout
                    if (timedPacket.timeoutExpired()) {
                        // Double the host's timeout
                        process.expBackOff();
                        // Update the packet timestamp
                        timedPacket.update(process.getTimeout());
                        // Resend the packet
                        FairLossLink.enqueuePacket(timedPacket.getPacket(), process.getId());
                    }
                    // Re-insert in queue
                    process.addPacketToAck(timedPacket);
                } else if (!timedPacket.isAck()) {
                    --process.windowSize;
                    // Check if I freed enough space
                    if (!process.hasSpace()) {
                        --hasSpace;
                    }
                }
            }
        }
        // Check at least half hosts has enough space to continue
        return hasSpace >= processes.size() / 2;
    }

    /**
     * It creates a packet shared among all the hosts (broadcast).
     * Try to put in a packet the highest possible number of proposal.
     * This is limited by the packet's max. size.
     * @return shared packet with the packet proposals
     */
    private static Packet craftSharedPacket() {
        int len = HEADER;
        List<Proposal> sublist = new LinkedList<>();
        synchronized (proposalsToSend) {
            Iterator<Proposal> iterator = proposalsToSend.iterator();
            while (sublist.size() < MAX_COMPRESSION && iterator.hasNext()) {
                // Find how many bytes the current proposal occupies to find the fit
                Proposal curr = iterator.next();
                int bytes = curr.getBytes();
                if (MAX_PACKET_SIZE - len >= bytes) {
                    len += bytes;
                    sublist.add(curr);
                    iterator.remove();
                }
            }
        }
        // If I have at least a proposal to pack
        if (!sublist.isEmpty()) {
            // Create a shared packet with the given proposals
            return new Packet(sublist, packetNumber++, MY_HOST, len);
        }
        return null;
    }

    /**
     * Send a packet and put it the re-send queue
     * with the current process timeout
     * @param process to which send the packet
     * @param packet to be sent
     * @param isAck whether the packet is made of ack
     */
    private static void sendPacket(Process process, Packet packet, boolean isAck) {
        process.addPacketToAck(new TimedPacket(process.getTimeout(), packet, isAck));
        if (!isAck) {
            ++process.windowSize;
        }
        FairLossLink.enqueuePacket(packet, process.getId());
    }

    /**
     * Set atomically the running flag to false
     */
    public static void stopThreads() {
        running.set(false);
    }

}
