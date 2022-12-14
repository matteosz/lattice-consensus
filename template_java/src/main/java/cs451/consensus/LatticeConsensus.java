package cs451.consensus;

import static cs451.message.Packet.MAX_COMPRESSION;
import static cs451.parser.ConfigParser.totalProposal;
import static cs451.parser.HostsParser.hosts;
import static cs451.process.Process.myHost;
import static cs451.process.Process.proposalsToSend;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.PerfectLink;
import cs451.message.Compressor;
import cs451.message.Proposal;
import cs451.parser.ConfigParser;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * It implements a weak form of consensus in asynchronous networks.
 *
 * Main functions:
 *  1) Propose a set values to other hosts.
 *  2) Decide on a set of common value.
 */
public class LatticeConsensus {


    /** Consumer function to be called for delivering (e.g. writing to file) */
    private static Consumer<Set<Integer>> callback;

    /** Half of the hosts, that together with the local host represents the majority */
    private static int majority;

    /** Array of boolean flags, one for each original proposal representing the active ones */
    private static boolean[] active;

    /** Mapping the proposal original id to the respective active count */
    private final static Map<Integer, Integer> activeProposal = new HashMap<>();

    /** Mapping to a given proposal id the number of ack (int[0]) and nack (int[1]) */
    private final static Map<Integer, int[]> ackCount = new HashMap<>();

    /** Mapping to a given proposal id the set of integers as proposed and accepted values */
    private static final Map<Integer, Set<Integer>> proposedValue = new HashMap<>(),
                                                    acceptedValue = new HashMap<>();

    /**
     * Compressor to save the delivered proposal as range.
     * Since proposal's ids start from 0, it's initialized with -1
     * to anchor the first proposal
     */
    private static final Compressor delivered = new Compressor(false, -1);

    /** List of original proposals to send globally */
    public static LinkedList<Proposal> originals;

    /** Counter to keep track of delivered proposals to send new ones */
    private static byte finished = 0;

    /** Map to keep track of counter of delivered proposal to clean */
    private static final Map<Integer, byte[]> deliveredCount = new HashMap<>();

    /**
     * Start the consensus by loading a batch of proposals
     * and starting to broadcast those proposals.
     * @param deliverCallback consumer function to write logs
     * @throws SocketException
     */
    public static void start(Consumer<Set<Integer>> deliverCallback) throws SocketException {
        callback = deliverCallback;
        majority = hosts.size() / 2;
        active = new boolean[totalProposal];
        // Initialize data structures, loading a first batch of proposals
        Iterator<Proposal> iterator = originals.listIterator();
        while (iterator.hasNext()) {
            Proposal proposal = iterator.next();
            // Populate the consensus data structure given this proposal as loaded
            populateProposal(proposal);
            // Load this proposal to be broadcast
            proposalsToSend.add(proposal);
            // Clear the entry from list
            iterator.remove();
        }
        // Start the BEB broadcast and all the underlying instances
        BestEffortBroadcast.start(LatticeConsensus::deliverProposal, LatticeConsensus::deliverAck, LatticeConsensus::deliverNAck);
    }

    /**
     * Deliver a proposal of type PROPOSAL.
     * @param proposal delivered by underlying layer
     */
    private static void deliverProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        // Check that the given proposal is loaded, else load it
        if (!acceptedValue.containsKey(id)) {
            acceptedValue.put(id, new HashSet<>());
        }
        // If accepted values is a subset of the proposed values
        if (proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, proposal.getProposedValues());
            // Send an ack to the proposal's sender
            PerfectLink.sendAck(new Proposal(id, (byte) 1, myHost, null, proposal.getActiveProposalNumber()), proposal.getSender());
        } else {
            // Otherwise add all the proposed values to the accepted values
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            // Send a nack with the new accepted values
            PerfectLink.sendNack(Proposal.createProposal(id, (byte) 2, myHost, acceptedValue.get(id), proposal.getActiveProposalNumber()), proposal.getSender());
        }
    }

    /**
     * Deliver an "internal" proposal, i.e. proposal
     * sent by the localhost to itself.
     * It avoids to physically send it.
     * @param proposal to deliver
     */
    private static void internalDeliver(Proposal proposal) {
        int id = proposal.getProposalNumber();
        // If accepted values is a subset of the proposed values
        if (proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, proposal.getProposedValues());
            // Send and receive an ack to the proposal's sender
            deliverAck(new Proposal(id, (byte) 1, myHost, null, proposal.getActiveProposalNumber()));
        } else {
            // Otherwise add all the proposed values to the accepted values
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            // Send a nack with the new accepted values
            deliverNAck(Proposal.createProposal(id, (byte) 2, myHost, acceptedValue.get(id), proposal.getActiveProposalNumber()));
        }
    }

    /**
     * Deliver a proposal of type ACK.
     * @param proposal delivered by underlying layer
     */
    private static void deliverAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.get(id)) {
            // Increase the ack counter
            ++ackCount.get(id)[0];
            // Now check 2 events, since ack has been incremented
            checkAck(id);
            checkNAck(proposal);
        }
    }

    /**
     * Deliver a proposal of type NACK.
     * @param proposal delivered by underlying layer
     */
    private static void deliverNAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.get(id)) {
            // Add to my proposed values all the proposal's values
            proposedValue.get(id).addAll(proposal.getProposedValues());
            // Increment the nack counter
            ++ackCount.get(id)[1];
            // Check the nack event since nack has been incremented
            checkNAck(proposal);
        }
    }

    /**
     * Nack event: when the number of nack increases this event is triggered.
     * @param proposal that triggered the event
     */
    private static void checkNAck(Proposal proposal) {
        int proposalId = proposal.getProposalNumber();
        int[] ack = ackCount.get(proposalId);
        // If nack > 0 and nack + ack >= f + 1 and the proposal is currently active
        if (ack[1] > 0 && (ack[0] + ack[1] > majority) && active[proposalId]) {
            // Increment active count
            int activeId = activeProposal.get(proposalId) + 1;
            activeProposal.put(proposalId, activeId);
            Proposal newProposal = Proposal.createProposal(proposalId, (byte) 0, myHost, proposedValue.get(proposalId), activeId);
            // Broadcast to everyone the new proposal with different active count
            BestEffortBroadcast.broadcast(newProposal);
            // Set both ack and nack to 0
            ack[0] = 0; ack[1] = 0;
            internalDeliver(newProposal);
        }
    }

    /**
     * Ack event: when the number of ack increases this event is triggered.
     * @param id proposal id that triggered the event
     */
    private static void checkAck(int id) {
        // If received the majority of ack and the proposal is currently active
        if (ackCount.get(id)[0] > majority && active[id]) {
            // Increment the window since I've delivered a proposal
            ++finished;
            if (finished == MAX_COMPRESSION) {
                if (ConfigParser.readProposals()) {
                    loadNext();
                }
                finished = 0;
            }
            // Take last delivered proposal
            int lastDelivered = delivered.takeLast() + 1;
            delivered.add(id);
            int lastToDeliver = delivered.takeLast();
            // Deliver contiguous proposals if possible
            while (lastDelivered <= lastToDeliver) {
                // Send decided lastDelivered to then clean
                BestEffortBroadcast.broadcastDelivered(new Proposal(lastDelivered));
                callback.accept(proposedValue.get(lastDelivered));
                clean(lastDelivered++);
            }
            active[id] = false;
        }
    }

    /**
     * Clean proposal's metadata after being sure
     * everyone has decided that round.
     * @param id proposal's id decided by a distant host
     */
    public static void clean(int id) {
        if (!deliveredCount.containsKey(id)) {
            deliveredCount.put(id, new byte[] {1});
        } else {
            int count = ++deliveredCount.get(id)[0];
            if (count == 2 * majority || count < 0) {
                // Everyone has decided
                acceptedValue.get(id).clear();
                proposedValue.get(id).clear();
            }
        }
    }

    /**
     * Load the next proposal from original ones
     */
    private static void loadNext() {
        Iterator<Proposal> iterator = originals.listIterator();
        while (iterator.hasNext()) {
            Proposal proposal = iterator.next();
            // Add in tail to the shared proposals
            proposalsToSend.add(proposal);
            // Populate the consensus data structure given this proposal as loaded
            populateProposal(proposal);
            // Clear the entry from list
            iterator.remove();
        }
    }

    /**
     * Populate the data structures to store
     * metadata about a given proposal.
     * @param proposal to load
     */
    private static void populateProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        proposedValue.put(id, new HashSet<>(proposal.getProposedValues()));
        activeProposal.put(id, 1);
        if (!deliveredCount.containsKey(id)) {
            deliveredCount.put(id, new byte[] {0});
        }
        active[id] = true;
        if (!acceptedValue.containsKey(id)) {
            // Since I'm not sending to myself messages, I set accepted value to be the same as the proposed one
            acceptedValue.put(id, new HashSet<>(proposal.getProposedValues()));
            // For the previous reason, ack starts from 1, nack from 0
            ackCount.put(id, new int[]{1, 0});
        } else {
            ackCount.put(id, new int[]{0, 0});
            internalDeliver(proposal);
        }
    }

}
