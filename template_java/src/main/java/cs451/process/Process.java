package cs451.process;

import cs451.message.Compressor;
import cs451.message.Proposal;
import cs451.message.TimedPacket;
import cs451.parser.Host;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import static cs451.message.Packet.MAX_PACKET_SIZE;
import static cs451.utilities.Parameters.*;
import static cs451.message.Packet.MAX_COMPRESSION;

/**
 * It contains the information about the hosts and the
 * proposals to send, delivered and acked.
 */
public class Process {

    /**
     * The static objects represent something shared among all hosts
     */

    /** Host id of the local host */
    public static byte MY_HOST;

    /** Number of hosts in the system */
    public static int NUM_HOSTS;

    /** Thread-safe queue of shared proposals to send to every host */
    public static final Set<Proposal> proposalsToSend = new TreeSet<>();

    /** Information about this distant host */
    private final Host host;

    /** Timeout of the host */
    private final AtomicLong timeout = new AtomicLong(TIMEOUT);

    /** Window of messages in the resend queue */
    public int windowSize = 0;

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

    /**
     * Maps to represent all the proposals' ids delivered, divided by
     * type and associated by the active counts been delivered
     */
    private final Map<Integer, Compressor> proposalsDelivered = new HashMap<>(),
                                           ackDelivered = new HashMap<>(),
                                           nackDelivered = new HashMap<>();

    /** Data structure to deliver cleaning request */
    private final Compressor cleanDelivered = new Compressor(false);

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
        if (initial > MAX_TIMEOUT) {
            return;
        }
        timeout.set(2 * initial + THRESHOLD);
    }

    /**
     * Notify the host's activity
     * and update its timeout to the
     * emission time of the packet received.
     * @param lastTime time of emission of the packet
     */
    public void notify(long lastTime) {
        timeout.set(lastTime);
    }

    /**
     * @return whether the host has enough space to
     * send new packets or just continue resending toAck
     */
    public boolean hasSpace() {
        return windowSize < LINK_BATCH;
    }

    /**
     * Get a list of proposal from the queue passed.
     * @param len initial length of the packet with no proposals (header), used an array to pass by reference
     * @param ack type of queue passed (ack or nack)
     * @return list of max. 8 proposals that fit the maximum packet size
     */
    public static List<Proposal> getNextAckProposals(int[] len, ConcurrentLinkedQueue<Proposal> ack) {
        List<Proposal> proposals = new LinkedList<>();
        boolean retry = false;
        while (proposals.size() < MAX_COMPRESSION) {
            Proposal proposal = ack.poll();
            if (proposal == null || proposal.getBytes() > (MAX_PACKET_SIZE - len[0])) {
                if (proposal != null) {
                    // Insert back the proposal at the end of the queue
                    ack.add(proposal);
                }
                if (retry) {
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return proposals;
                }
                retry = true;
            } else {
                // Ensure that the proposal fit in the packet
                proposals.add(proposal);
                len[0] += proposal.getBytes();
            }
        }
        return proposals;
    }

    /**
     * @return queue of nack proposals yet to be sent
     */
    public ConcurrentLinkedQueue<Proposal> getNackToSend() {
        return nackToSend;
    }

    /**
     * @return queue of ack proposals yet to be sent
     */
    public ConcurrentLinkedQueue<Proposal> getAckToSend() {
        return ackToSend;
    }

    /**
     * @return next timed packet in queue to resend
     */
    public TimedPacket nextPacketToAck() {
        return toAck.poll();
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
        switch (proposal.getType()) {
            case 0:
                return commonDeliver(proposal.getProposalNumber(), proposal.getActiveProposalNumber(), proposalsDelivered);
            case 1:
                return commonDeliver(proposal.getProposalNumber(), proposal.getActiveProposalNumber(), ackDelivered);
            case 2:
                return commonDeliver(proposal.getProposalNumber(), proposal.getActiveProposalNumber(), nackDelivered);
            case 3:
                return cleanDelivered.add(proposal.getProposalNumber());
        }
        return false;
    }

    /**
     * Common deliver function for proposals (PROPOSAL,
     * ACK or NACK types). It just checks if that proposal
     * with the given active count has been delivered already.
     * @param proposalNumber original proposal's id
     * @param activeCount proposal's active count given by consensus
     * @param map delivery data structure
     * @return true if correctly added and not delivered before, else otherwise
     */
    private static boolean commonDeliver(int proposalNumber, int activeCount, Map<Integer, Compressor> map) {
        if (!map.containsKey(proposalNumber)) {
            map.put(proposalNumber, new Compressor(false, activeCount));
            return true;
        }
        return map.get(proposalNumber).add(activeCount);
    }

}
