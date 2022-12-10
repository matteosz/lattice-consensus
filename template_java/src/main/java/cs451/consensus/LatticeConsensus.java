package cs451.consensus;

import static cs451.process.Process.getMyHost;
import static cs451.process.Process.proposalsToSend;
import static cs451.utilities.Parameters.PROPOSAL_BATCH;

import cs451.broadcast.BestEffortBroadcast;
import cs451.channel.PerfectLink;
import cs451.message.Compressor;
import cs451.message.Proposal;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * LATTICE consensus
 *
 * It implements a weak form of consensus in asynchronous networks
 *
 * Main functions:
 *  1) Propose a set values to other hosts
 *  2) Decide on a set of common value
 */
public class LatticeConsensus {

    /** Window of max. number of proposals to be moving on a given time */
    public static final AtomicInteger window = new AtomicInteger(PROPOSAL_BATCH);

    /** Consumer function to be called for delivering (e.g. writing to file) */
    private static Consumer<Set<Integer>> callback;

    /** Half of the hosts, that together with the local host represents the majority */
    private static int majority;

    /** Array of boolean flags, one for each original proposal representing the active ones */
    private static boolean[] active;

    /** Mapping the proposal original id to the respective active count */
    private final static Map<Integer, Integer> activeProposal = new HashMap<>();

    /** Mapping to a given proposal id the number of ack (byte[0]) and nack (byte[1]) */
    private final static Map<Integer, byte[]> ackCount = new HashMap<>();

    /** Mapping to a given proposal id the set of integers as proposed and accepted values */
    private static Map<Integer, Set<Integer>> proposedValue = new HashMap<>(),
                                              acceptedValue = new HashMap<>();

    /**
     * Compressor to save the delivered proposal as range
     * Since proposal's ids start from 0, it's initialized with -1
     * to anchor the first proposal
     */
    private static Compressor delivered = new Compressor(-1);

    /** List of original proposals to send globally */
    private static LinkedList<Proposal> originals;

    /**
     *
     * @param port
     * @param numHosts
     * @param deliverCallback
     * @param proposals
     * @throws SocketException
     */
    public static void start(int port, int numHosts, Consumer<Set<Integer>> deliverCallback, LinkedList<Proposal> proposals) throws SocketException {
        callback = deliverCallback;
        majority = numHosts / 2;
        originals = proposals;
        active = new boolean[proposals.size()];
        // Initialize data structures, loading a first batch of proposals
        int miniBatch = 0;
        // Go through ordered iterator (FIFO)
        Iterator<Proposal> iterator = proposals.listIterator();
        while (iterator.hasNext()) {
            Proposal proposal = iterator.next();
            // Populate the consensus data structure given this proposal as loaded
            populateProposal(proposal);
            // Clear the entry from list
            iterator.remove();
            if (++miniBatch == PROPOSAL_BATCH) {
                break;
            }
        }
        // Start the BEB broadcast and all the underlying instances
        BestEffortBroadcast.start(port, LatticeConsensus::deliverProposal, LatticeConsensus::deliverAck, LatticeConsensus::deliverNAck);
    }

    /**
     * Deliver a proposal of type PROPOSAL
     * @param proposal delivered by underlying layer
     */
    private static void deliverProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        // If accepted values is a subset of the proposed values
        if (proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, proposal.getProposedValues());
            // Send an ack to the proposal's sender
            PerfectLink.sendAck(new Proposal(id, (byte) 1, getMyHost(), null, proposal.getActiveProposalNumber()), proposal.getSender());
        } else {
            // Otherwise add all the proposed values to the accepted values
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            // Send a nack with the new accepted values
            PerfectLink.sendNack(Proposal.createProposal(id, (byte) 2, getMyHost(), acceptedValue.get(id), proposal.getActiveProposalNumber()), proposal.getSender());
        }
    }

    /**
     * Deliver a proposal of type ACK
     * @param proposal delivered by underlying layer
     */
    private static void deliverAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.get(id)) {
            // Increase the ack counter
            ackCount.get(id)[0]++;
            // Now check 2 events, since ack has been incremented
            checkAck(id);
            checkNAck(proposal);
        }
    }

    /**
     * Deliver a proposal of type NACK
     * @param proposal delivered by underlying layer
     */
    private static void deliverNAck(Proposal proposal) {
        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();
        // If the active proposal number of the received proposal is the current one
        if (activeId == activeProposal.get(id)) {
            // Add to my proposed values all the proposal's values
            proposedValue.get(id).addAll(proposal.getProposedValues());
            // Increment the nack counter
            ackCount.get(id)[1]++;
            // Check the nack event since nack has been incremented
            checkNAck(proposal);
        }
    }

    /**
     * Nack event: when the number of nack increases this event is triggered
     * @param proposal that triggered the event
     */
    private static void checkNAck(Proposal proposal) {
        int proposalId = proposal.getProposalNumber();
        byte[] ack = ackCount.get(proposalId);
        // If nack > 0 and nack + ack >= f + 1 and the proposal is currently active
        if (ack[1] > 0 && (ack[0] + ack[1] > majority) && active[proposalId]) {
            // Increment active count
            int activeId = activeProposal.get(proposalId) + 1;
            activeProposal.put(proposalId, activeId);
            // Set ack to 1 since I'm not sending the message to myself to save bandwidth
            // I need to "simulate" the delivery
            ack[0] = 1;
            acceptedValue.put(proposalId, new HashSet<>(proposedValue.get(proposalId)));
            // Set nack to 0
            ack[1] = 0;
            // Broadcast to everyone the new proposal with different active count
            BestEffortBroadcast.broadcast(Proposal.createProposal(proposalId, (byte) 0, getMyHost(), proposedValue.get(proposalId), activeId));
        }
    }

    /**
     * Ack event: when the number of ack increases this event is triggered
     * @param id proposal id that triggered the event
     */
    private static void checkAck(int id) {
        // If received the majority of ack and the proposal is currently active
        if (ackCount.get(id)[0] > majority && active[id]) {
            // Increment the window since I've delivered a proposal
            window.incrementAndGet();
            // Take the next proposal from original ones
            loadNext();

            // Take last delivered proposal
            int lastDelivered = delivered.takeLast() + 1;
            delivered.add(id);
            int lastToDeliver = delivered.takeLast();
            // Deliver contiguous proposals if possible
            while (lastDelivered <= lastToDeliver) {
                callback.accept(proposedValue.get(lastDelivered));
                // Clear proposedValue to save memory
                proposedValue.get(id).clear();
                lastDelivered++;
            }

            active[id] = false;
            // Clear accepted value to save memory
            acceptedValue.get(id).clear();
        }
    }

    /**
     * Load the next proposal from original ones
     */
    private static void loadNext() {
        try {
            Proposal proposal = originals.removeFirst();
            populateProposal(proposal);
            synchronized (proposalsToSend) {
                // Add in tail to the shared proposals
                proposalsToSend.addLast(proposal);
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Populate the data structures to store metadata about
     * a given proposal
     * @param proposal to load
     */
    private static void populateProposal(Proposal proposal) {
        // Original proposal id
        int id = proposal.getProposalNumber();
        proposedValue.put(id, new HashSet<>(proposal.getProposedValues()));
        // Since I'm not sending to myself messages, I set accepted value to be the same as the proposed one
        acceptedValue.put(id, new HashSet<>(proposal.getProposedValues()));
        // For the previous reason, ack starts from 1, nack from 0
        ackCount.put(id, new byte[]{1, 0});
        // At the beginning the active count for the proposal is 1
        activeProposal.put(id, 1);
        active[id] = true;
    }

}
