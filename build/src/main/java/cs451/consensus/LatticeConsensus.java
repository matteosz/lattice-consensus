package cs451.consensus;

import static cs451.message.Packet.MAX_COMPRESSION;
import static cs451.process.Process.MY_HOST;
import static cs451.process.Process.NUM_HOSTS;
import static cs451.utilities.Parameters.GC_BATCH;
import static cs451.utilities.Parameters.PROPOSAL_BATCH;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.PerfectLink;
import cs451.message.Compressor;
import cs451.message.Proposal;
import cs451.parser.ConfigParser;
import cs451.service.CommunicationService;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * It implements a weak form of consensus in asynchronous networks.
 * <p>
 * Main functions:
 *  1) Propose a set values to other hosts.
 *  2) Decide on a set of common value.
 */
public class LatticeConsensus {

    /** Half of the hosts, that together with the local host represents the majority. */
    private static int majority;

    /** Mapping the proposal original id to the respective active count. */
    private final static Map<Integer, Integer> activeProposal = new HashMap<>();

    /** Mapping to a given proposal id the number of ack (int[0]) and nack (int[1]). */
    private final static Map<Integer, Integer[]> ackCount = new HashMap<>();

    /** Mapping to a given proposal id the set of integers as proposed and accepted values. */
    private static final Map<Integer, Set<Integer>> proposedValue = new HashMap<>(),
                                                    acceptedValue = new HashMap<>();

    /**
     * Compressor to save the delivered proposal as range.
     * Since proposal's ids start from 0, it's initialized with -1
     * to anchor the first proposal.
     */
    private static final Compressor delivered = new Compressor(false, 0);

    /** List of original proposals to send globally. */
    public static final LinkedList<Proposal> originals = new LinkedList<>();

    /** Counter to keep track of delivered proposals to send new ones. */
    private static byte finished = 0;

    /** Counter to keep track of finished batches. */
    private static int batchFinished = 0;

    /** Map to keep track of counter of delivered proposal to clean. */
    private static final Map<Integer, Integer> deliveredCount = new HashMap<>();

    /**
     * Start the consensus by loading a batch of proposals
     * and starting to broadcast those proposals.
     * @throws SocketException
     */
    public static void start() throws SocketException {
        majority = NUM_HOSTS >> 1;
        // Initialize data structures, loading a first batch of proposals
        Iterator<Proposal> iterator = originals.listIterator();
        while (iterator.hasNext()) {
            Proposal proposal = iterator.next();
            // Clear the entry from list
            iterator.remove();
            // Populate the consensus data structure given this proposal as loaded
            broadcastProposal(proposal);
        }
        // Start the BEB broadcast and all the underlying instances
        BestEffortBroadcast.start();
    }

    /**
     * Deliver a proposal of type PROPOSAL.
     * @param proposal delivered by underlying layer.
     */
    public static void deliverProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        // If accepted values is a subset of the proposed values
        if (!acceptedValue.containsKey(id) || proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, proposal.getProposedValues());
            // Send an ack to the proposal's sender
            PerfectLink.sendAck(new Proposal(id, (byte) 1, MY_HOST, null, proposal.getActiveProposalNumber()), proposal.getSender());
        } else {
            // Otherwise add all the proposed values to the accepted values
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            // Send a nack with the new accepted values
            PerfectLink.sendNack(Proposal.createProposal(id, (byte) 2, MY_HOST, acceptedValue.get(id), proposal.getActiveProposalNumber()), proposal.getSender());
        }
    }

    /**
     * Deliver a proposal of type ACK.
     * @param proposal delivered by underlying layer.
     */
    public static void deliverAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.getOrDefault(id, 0)) {
            // Increase the ack counter
            ++ackCount.get(id)[0];
            // Now check 2 events, since ack has been incremented
            checkNAck(id);
            checkAck(id);
        }
    }

    /**
     * Deliver a proposal of type NACK.
     * @param proposal delivered by underlying layer.
     */
    public static void deliverNAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.getOrDefault(id, 0)) {
            // Add to my proposed values all the proposal's values
            proposedValue.get(id).addAll(proposal.getProposedValues());
            // Increment the nack counter
            ++ackCount.get(id)[1];
            // Check the nack event since nack has been incremented
            checkNAck(id);
        }
    }

    /**
     * Nack event: when the number of nack increases this event is triggered.
     * @param id of proposal that triggered the event.
     */
    private static void checkNAck(int id) {
        Integer[] ack = ackCount.get(id);
        // If nack > 0 and nack + ack >= f + 1 and the proposal is currently active
        if ((ack[1] > 0) && ((ack[0] + ack[1]) > majority)) {
            // Increment active count
            int activeId = activeProposal.get(id) + 1;
            activeProposal.put(id, activeId);
            // Simulate delivery to myself
            if (proposedValue.get(id).containsAll(acceptedValue.get(id))) {
                // Set ack to 1 and nack to 0
                ack[0] = 1; ack[1] = 0;
            } else {
                proposedValue.get(id).addAll(acceptedValue.get(id));
                // Set ack to 0 and nack to 1
                ack[0] = 0; ack[1] = 1;
            }
            acceptedValue.get(id).addAll(proposedValue.get(id));
            // Broadcast to everyone the new proposal with different active count
            BestEffortBroadcast.broadcast(Proposal.createProposal(id, (byte) 0, MY_HOST, proposedValue.get(id), activeId), true);
        }
    }

    /**
     * Ack event: when the number of ack increases this event is triggered.
     * @param id proposal id that triggered the event.
     */
    private static void checkAck(int id) {
        // If received the majority of ack and the proposal is currently active
        if (ackCount.get(id)[0] > majority) {
            // Increment the window since I've delivered a proposal
            if (++finished == Math.min(MAX_COMPRESSION, PROPOSAL_BATCH)) {
                if (ConfigParser.readProposals()) {
                    loadNext();
                }
                finished = 0;
                ++batchFinished;
            }
            // Take last delivered proposal
            int lastDelivered = delivered.takeLast() + 1;
            delivered.add(id);
            int lastToDeliver = delivered.takeLast();
            // Deliver contiguous proposals if possible
            while (lastDelivered <= lastToDeliver) {
                CommunicationService.deliver(proposedValue.get(lastDelivered));
                // Try to clean
                proposedValue.remove(lastDelivered);
                if (!clean(lastDelivered)) {
                    BestEffortBroadcast.broadcastDelivered(new Proposal(lastDelivered));
                }
                ++lastDelivered;
            }
            // Remove the delivered proposal from active ones
            activeProposal.remove(id);
            ackCount.remove(id);
            // Force memory reclamation
            if (batchFinished == GC_BATCH) {
                batchFinished = 0;
                System.gc();
            }
        }
    }

    /**
     * Clean proposal's metadata after being sure
     * everyone has decided that round.
     * @param id proposal's id decided by a distant host.
     * @return true if cleaned, false if still not enough delivered.
     */
    public static boolean clean(int id) {
        int prev = deliveredCount.getOrDefault(id, 0) + 1;
        if (prev == NUM_HOSTS) {
            // Everyone has decided -> can clean
            acceptedValue.remove(id);
            deliveredCount.remove(id);
            return true;
        }
        deliveredCount.put(id, prev);
        return false;
    }

    /**
     * Load the next proposal from original ones.
     */
    private static void loadNext() {
        Iterator<Proposal> iterator = originals.listIterator();
        while (iterator.hasNext()) {
            Proposal proposal = iterator.next();
            // Clear the entry from list
            iterator.remove();
            // Populate the consensus data structure given this proposal as loaded
            broadcastProposal(proposal);
        }
    }

    /**
     * Populate the data structures to store
     * metadata about a given proposal and broadcast it.
     * @param proposal to load.
     */
    private static void broadcastProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        activeProposal.put(id, 1);
        proposedValue.put(id, new HashSet<>(proposal.getProposedValues()));
        Integer[] ack = new Integer[] {0, 0};
        // Simulate delivery to myself
        if (!acceptedValue.containsKey(id) || proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, new HashSet<>(proposal.getProposedValues()));
            ack[0] = 1;
        } else {
            proposedValue.get(id).addAll(acceptedValue.get(id));
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            ack[1] = 1;
        }
        ackCount.put(id, ack);
        // Add to the shared proposals with normal priority
        BestEffortBroadcast.broadcast(proposal, false);
    }

}
