package cs451.process;

import cs451.message.Compressor;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.utilities.Parameters.*;
import static cs451.message.Packet.MAX_COMPRESSION;

/**
 * PROCESS
 *
 * Contain the information about the hosts and the
 * proposals to send, delivered and acked
 */
public class Process {

    /** The static objects represent something which is shared among all hosts */

    /** Host id of the local host */
    private static byte myHost;

    /** List of shared proposals to send to every host */
    public static final LinkedList<Proposal> proposalsToSend = new LinkedList<>();

    /** Information about this distant host */
    private final Host host;

    /** Timeout of the host */
    private final AtomicLong timeout = new AtomicLong(TIMEOUT);

    /** Queue of packets associated with a timeout, to be resent to the current host */
    private final Queue<TimedPacket> toAck = new LinkedList<>();

    /** Queue of proposals (NACK type) to send to the current host */
    private final Queue<Proposal> nackToSend = new LinkedList<>();

    /** Queue of proposals (ACK type) to send to the current host */
    private final Queue<Proposal> ackToSend = new LinkedList<>();

    /** Compressor to represent all the packet id acked */
    private final Compressor packetsAcked = new Compressor();

    /** Compressor to represent all the packet id delivered */
    private final Compressor packetsDelivered = new Compressor();

    /** Compressors to represent all the proposal id delivered, divided by type */
    private final Compressor proposalsDelivered = new Compressor(),
                             ackDelivered = new Compressor(),
                             nackDelivered = new Compressor();

    /**
     * Initialize the proposals to send by loading a batch of original proposals
     * @param id of local host
     * @param prop list of original proposals
     */
    public static void initialize(byte id, LinkedList<Proposal> prop) {
        myHost = id;
        // Load the first batch from original proposals
        List<Proposal> sublist = prop.subList(0, PROPOSAL_BATCH);
        proposalsToSend.addAll(sublist);
    }

    /**
     * @return local host id
     */
    public static byte getMyHost() {
        return myHost;
    }

    /**
     * Initialize all data structures
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
        // Do not double the previous threshold
        timeout.set(Math.min(2 * (timeout.get() - THRESHOLD), MAX_TIMEOUT) + THRESHOLD);
    }

    /**
     * Notify the host's activity
     * and update its timeout to the
     * emission time of the packet received
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
     * Get a list of proposal from the queue passed
     * @param len initial length of the packet with no proposals (header)
     * @param ack type of queue passed (ack or nack)
     * @return list of max. 8 proposals that fit the maximum packet size
     */
    public List<Proposal> getNextAckProposals(int[] len, Queue<Proposal> ack) {
        byte count = 0;
        List<Proposal> proposals = new LinkedList<>();
        synchronized (ack) {
            while (proposals.size() < MAX_COMPRESSION && count < MAX_MISS) {
                Proposal proposal;
                proposal = ack.poll();
                if (proposal == null) {
                    // No proposal to be sent yet
                    break;
                }
                // Ensure that the proposal fit in the packet
                if (proposal.getBytes() <= (MAX_PACKET_SIZE - len[0])) {
                    proposals.add(proposal);
                    len[0] += proposal.getBytes();
                } else if (ack.size() < 1) {
                    // If there's no other proposal yet then break
                    break;
                } else {
                    // If the current proposal doesn't fit try to fit another proposal
                    ++count;
                    // Insert back the proposal at the end of the queue
                    ack.add(proposal);
                }
            }
        }
        return proposals;
    }

    /**
     * @return nack proposals yet to be sent
     */
    public Queue<Proposal> getNackToSend() {
        return nackToSend;
    }

    /**
     * @return ack proposals yet to be sent
     */
    public Queue<Proposal> getAckToSend() {
        return ackToSend;
    }

    /**
     * @return first timed packet of the queue
     *         null if empty
     */
    public TimedPacket nextPacketToAck() {
        return toAck.poll();
    }

    /**
     * Add a timed packet to the queue of packets to ack
     * @param packet to be rechecked later
     */
    public void addPacketToAck(TimedPacket packet) {
        toAck.add(packet);
    }

    /**
     * @param id packet id
     * @return true if already acked, false otherwise
     */
    public boolean hasAcked(int id) {
        return packetsAcked.contains(id);
    }

    /**
     * Add to the acked proposals to be sent the given proposal
     * @param proposal to send of type ACK
     */
    public void addAckProposal(Proposal proposal) {
        synchronized (ackToSend) {
            ackToSend.add(proposal);
        }
    }

    /**
     * Add to the nack proposals to be sent the given proposal
     * @param proposal to send of type NACK
     */
    public void addNackProposal(Proposal proposal) {
        synchronized (nackToSend) {
            nackToSend.add(proposal);
        }
    }

    /**
     * Add to the delivered a given packet id
     * @param packetId to deliver
     * @return true if added correctly and not delivered before, false otherwise
     */
    public boolean deliverPacket(int packetId) {
        return packetsDelivered.add(packetId);
    }

    /**
     * Add to the acked a given packet id (ack)
     * @param ackId to ack
     */
    public void deliverAck(int ackId) {
        packetsAcked.add(ackId);
    }

    /**
     * Deliver a proposal
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
