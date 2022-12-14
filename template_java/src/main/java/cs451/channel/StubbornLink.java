package cs451.channel;

import cs451.consensus.LatticeConsensus;
import cs451.message.Packet;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.process.Process;

import cs451.utilities.Parameters;
import java.net.DatagramPacket;
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
import static cs451.message.Packet.MEX_OS;
import static cs451.process.Process.myHost;
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
        if (Parameters.DEBUG) {
            System.out.println("Delivered packet from fair-loss link:\n" + packet);
        }
        if (packet.isAck()) {
            if (Parameters.DEBUG) {
                System.out.println("Delivered the ACK of packet: #id = " + packet.getPacketId());
            }
            Process sender = getProcess(packet.getSenderId());
            // Reset timeout with emission time
            sender.notify(packet.getEmissionTime());
            // Deliver the ack to not resend anymore that packet
            sender.deliverAck(packet.getPacketId());
        } else {
            // Send an ack back, same identical packet only change the isAck flag
            FairLossLink.enqueuePacket(packet.convertToAck(), packet.getSenderId());
            if (Parameters.DEBUG) {
                System.out.println("Sent back the ACK of packet: #id = " + packet.getPacketId() + " to p." + packet.getSenderId());
            }
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
        // View of all processes except my host
        Collection<Process> processes = getNetwork().values();
        // Index to build the packets, starting from 1
        int packetNumber = 1;

        while (running.get()) {
            // First try to resend all possible ack for all processes
            resendPackets(processes);
            /* if (!resendPackets(processes)) {
                if (Parameters.DEBUG) {
                    System.out.println("No more space on the link!");
                }
                continue;
            }*/
            // Craft the shared packet
            Packet sharedPacket = craftSharedPacket(packetNumber);
            if (sharedPacket == null) {
                --packetNumber;
            }
            byte inc = 0;
            for (Process process : processes) {
                // ACK
                int[] ackLen = {MEX_OS};
                List<Proposal> ack = Process.getNextAckProposals(ackLen, process.getAckToSend());
                if (ack.size() > 0) {
                    // Use packet number + 1
                    sendPacket(process, new Packet(ack, packetNumber + 1, myHost, ackLen[0]));
                    if (inc < 1) {
                        inc = 1;
                    }
                    if (Parameters.DEBUG) {
                        System.out.println("Sent packet #" + (packetNumber + 1) + " of " + ack.size() + " ACKS to " + process.getId());
                    }
                }
                // NACK
                ackLen[0] = MEX_OS;
                ack = Process.getNextAckProposals(ackLen, process.getNackToSend());
                if (ack.size() > 0) {
                    // Use packet number + 2
                    sendPacket(process, new Packet(ack, packetNumber + 2, myHost, ackLen[0]));
                    if (inc < 2) {
                        inc = 2;
                    }
                    if (Parameters.DEBUG) {
                        System.out.println("Sent packet #" + (packetNumber + 2) + " of " + ack.size() + " NACKS to " + process.getId());
                    }
                }
                // Send the crafted shared packet
                if (sharedPacket != null) {
                    sendPacket(process, sharedPacket);
                }
            }
            packetNumber += inc + 1;
        }
    }

    /**
     * Try to resend all timed out packets if not acked yet.
     * @param processes collection of all hosts
     * @return true if at least half hosts have enough space, false otherwise
     */
    private static void resendPackets(Collection<Process> processes) {
        // Initial assumption: everyone has enough space
        //int hasSpace = processes.size();
        for (Process process : processes) {
            TimedPacket timedPacket = process.nextPacketToAck();
            // Check if the that packet has not been acked yet to resend
            if (timedPacket != null && !process.hasAcked(
                timedPacket.getPacket().getPacketId())) {
                // If the current living time of the packet is greater than host's timeout
                if (timedPacket.timeoutExpired()) {
                    if (Parameters.DEBUG) {
                        System.out.println("Timeout was expired, resending");
                    }
                    // Double the host's timeout
                    process.expBackOff();
                    // Update the packet timestamp
                    timedPacket.update(process.getTimeout());
                    // Resend the packet
                    FairLossLink.enqueuePacket(timedPacket.getPacket(), process.getId());
                }
                // Re-insert in queue
                process.addPacketToAck(timedPacket);
            }
            /* Check if I freed enough space
            if (!process.hasSpace()) {
                --hasSpace;
            }*/
        }
        // Check at least half has enough space to continue
        //return hasSpace >= processes.size() / 2;
    }

    /**
     * It creates a packet shared among all the hosts (broadcast).
     * Try to put in a packet the highest possible number of proposal.
     * This is limited by a maximum threshold and the packet's size
     * @return shared packet with the packet proposals
     */
    private static Packet craftSharedPacket(int packetNumber) {
        int len = MEX_OS;
        // Get the current window of proposals permitted
        int limit = Math.min(MAX_COMPRESSION, LatticeConsensus.window.get());
        // If there's space in the window
        if (limit > 0) {
            List<Proposal> sublist = new LinkedList<>();
            Iterator<Proposal> iterator = proposalsToSend.iterator();
            while (iterator.hasNext() && sublist.size() < limit) {
                // Find how many bytes the current proposal occupies to find the fit
                Proposal curr = iterator.next();
                if (MAX_PACKET_SIZE - len >= curr.getBytes()) {
                    len += curr.getBytes();
                    sublist.add(curr);
                    iterator.remove();
                } else {
                    break;
                }
            }
            int size = sublist.size();
            // If I have at least a proposal to pack
            if (size > 0) {
                // Shrink the window with the number of proposals sent
                LatticeConsensus.window.addAndGet(-size);
                // Create a shared packet with the given proposals
                if (Parameters.DEBUG) {
                    System.out.println("Crafted a shared packet with #" + size + " proposals to send to everyone: #id = " + packetNumber);
                }
                return new Packet(sublist, packetNumber, myHost, len);
            }
        }
        return null;
    }

    /**
     * Send a packet and put it the re-send queue
     * with the current process timeout
     * @param process to which send the packet
     * @param packet to be sent
     */
    private static void sendPacket(Process process, Packet packet) {
        process.addPacketToAck(new TimedPacket(process.getTimeout(), packet));
        FairLossLink.enqueuePacket(packet, process.getId());
    }

    /**
     * Set atomically the running flag to false
     */
    public static void stopThreads() {
        running.set(false);
    }

}
