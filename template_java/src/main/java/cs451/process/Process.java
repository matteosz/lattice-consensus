package cs451.process;

import cs451.message.Compressor;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import static cs451.consensus.LatticeConsensus.originals;
import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.utilities.Parameters.*;
import static cs451.message.Packet.MAX_COMPRESSION;

/**
 * It contains the information about the hosts and the
 * proposals to send, delivered and acked.
 */
public class Process {

    /**
     * The static objects represent something which is shared among all hosts
     */

    /** Host id of the local host */
    public static byte myHost;

    /** Thread-safe queue of shared proposals to send to every host */
    public static final ConcurrentLinkedDeque<Proposal> proposalsToSend = new ConcurrentLinkedDeque<>();

    /** Information about this distant host */
    private final Host host;

    /** Timeout of the host */
    private final AtomicLong timeout = new AtomicLong(TIMEOUT);

    /** Queue of packets associated with a timeout, to be resent to the current host */
    private final Queue<TimedPacket> toAck = new LinkedList<>();

    /** Queue of proposals (ACK type) to send to the current host */
    private final ConcurrentLinkedQueue<Proposal> ackToSend = new ConcurrentLinkedQueue<>();

    /** Queue of proposals (NACK type) to send to the current host */
    private final ConcurrentLinkedQueue<Proposal> nackToSend = new ConcurrentLinkedQueue<>();

    /** Compressor to represent all the packet id acked */
    private final Compressor packetsAcked = new Compressor(true);

    /** Compressor to represent all the packet id delivered */
    private final Compressor packetsDelivered = new Compressor(false);

    /** Compressors to represent all the proposal id delivered, divided by type */
    private final Compressor proposalsDelivered = new Compressor(false),
                             ackDelivered = new Compressor(false),
                             nackDelivered = new Compressor(false);

    /**
     * Initialize the proposals to send by loading a batch of original proposals
     */
    public static void initialize() {
        proposalsToSend.addAll(originals.subList(0, Math.min(PROPOSAL_BATCH, originals.size())));
    }

    /**
     * Initialize all data structures.
     * @param host information about the host
     */
    public Process(Host host) {
        this.host = host;
    }

    /**
     * @return current host information
     */
    public Host getHost() {
        return host;
    }

    /**
     * @return id of current host
     */
    public byte getId() {
        return host.getId();
    }

    /**
     * @return current host's timeout
     */
    public long getTimeout() {
        return timeout.get();
    }

    /**
     * Doubles the timeout and add a threshold
     * Truncate to a maximum timeout
     */
    public void expBackOff() {
        long initial = timeout.get();
        initial = initial == TIMEOUT? initial : initial - THRESHOLD;
        // Don't double the previous threshold
        timeout.set(Math.min(2 * initial, MAX_TIMEOUT) + THRESHOLD);
    }

    /**
     * Notify the host's activity
     * and update its timeout to the
     * emission time of the packet received.
     * @param lastTime time of emission of the packet
     */
    public void notify(long lastTime) {
        timeout.set(lastTime + THRESHOLD);
    }

    /**
     * @return whether the host has enough space to
     * send new packets or just continue resending toAck
     */
    public boolean hasSpace() {
        return toAck.size() < LINK_BATCH;
    }

    /**
     * Get a list of proposal from the queue passed.
     * @param len initial length of the packet with no proposals (header)
     * @param ack type of queue passed (ack or nack)
     * @return list of max. 8 proposals that fit the maximum packet size
     */
    public List<Proposal> getNextAckProposals(int[] len, ConcurrentLinkedQueue<Proposal> ack) {
        byte count = 0;
        List<Proposal> proposals = new LinkedList<>();
        while (proposals.size() < MAX_COMPRESSION && count < MAX_MISS) {
            Proposal proposal = ack.poll();
            if (proposal == null || proposal.getBytes() > (MAX_PACKET_SIZE - len[0])) {
                // No proposal to be sent yet or not fitting the packet
                ++count;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return proposals;
                }
                if (proposal != null) {
                    // Insert back the proposal at the end of the queue
                    ack.add(proposal);
                }
            } else {
                // Ensure that the proposal fit in the packet
                proposals.add(proposal);
                len[0] += proposal.getBytes();
            }
        }
        return proposals;
    }

    /**
     * @return nack proposals yet to be sent
     */
    public ConcurrentLinkedQueue<Proposal> getNackToSend() {
        return nackToSend;
    }

    /**
     * @return ack proposals yet to be sent
     */
    public ConcurrentLinkedQueue<Proposal> getAckToSend() {
        return ackToSend;
    }

    /**
     * @return first timed packet of the queue,
     *         null if empty
     */
    public List<TimedPacket> nextPacketsToAck() {
        List<TimedPacket> copy = new ArrayList<>(toAck);
        toAck.clear();
        return copy;
    }

    /**
     * Add a timed packet to the queue of packets to ack.
     * @param packet to be rechecked later
     */
    public void addPacketToAck(TimedPacket packet) {
        toAck.add(packet);
    }

    /**
     * Whether the host has already acked a given packet.
     * @param id packet id
     * @return true if already acked, false otherwise
     */
    public boolean hasAcked(int id) {
        return packetsAcked.contains(id);
    }

    /**
     * Add to the acked proposals to be sent the given proposal.
     * @param proposal to send of type ACK
     */
    public void addAckProposal(Proposal proposal) {
        ackToSend.add(proposal);
    }

    /**
     * Add to the nack proposals to be sent the given proposal.
     * @param proposal to send of type NACK
     */
    public void addNackProposal(Proposal proposal) {
        nackToSend.add(proposal);
    }

    /**
     * Add to the delivered a given packet id.
     * @param packetId to deliver
     * @return true if added correctly and not delivered before, false otherwise
     */
    public boolean deliverPacket(int packetId) {
        return packetsDelivered.add(packetId);
    }

    /**
     * Add to the acked a given packet id (ack).
     * @param ackId to ack
     */
    public void deliverAck(int ackId) {
        packetsAcked.add(ackId);
    }

    /**
     * Deliver a proposal depending on its type.
     * @param proposal to deliver
     * @return true if correctly added and not delivered before, else otherwise
     */
    public boolean deliver(Proposal proposal) {
        if (proposal.isAck()) {
            return ackDelivered.add(proposal.getProposalNumber());
        } else if (proposal.isNack()) {
            return nackDelivered.add(proposal.getProposalNumber());
        } else  {
            return proposalsDelivered.add(proposal.getProposalNumber());
        }
    }

}
