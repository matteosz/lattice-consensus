package cs451.broadcast;

import static cs451.channel.Network.getNetwork;
import static cs451.process.Process.proposalsToSend;

import cs451.channel.PerfectLink;

import cs451.consensus.LatticeConsensus;
import cs451.message.Proposal;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * It serves to send to all hosts a given proposal.
 *
 * It has 2 main functions:
 *  1) Freeing the delivery of the underlying layer putting the
 *     delivered proposals in a queue to process them later on.
 *  2) Send to all the hosts a given proposal.
 */
public class BestEffortBroadcast {

    /**
     * Blocking queue of proposals delivered from underlying layer
     * to be delivered by the upper one
     */
    private static final BlockingQueue<Proposal> linkDelivered = new LinkedBlockingQueue<>();

    /** Running flag to stop the thread when the application stops */
    private static final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Initialize the Beb broadcast.
     * Then, take from the blocking queue the proposals
     * and call the consumer function based on its type
     * to deliver them to the upper layer.
     * @param proposalConsumer consumer function for proposal of type PROPOSAL
     * @param ackConsumer consumer function for proposal of type ACK
     * @param nackConsumer consumer function for proposal of type NACK
     * @throws SocketException
     */
    public static void start(Consumer<Proposal> proposalConsumer, Consumer<Proposal> ackConsumer, Consumer<Proposal> nackConsumer) throws SocketException {
        PerfectLink.start(BestEffortBroadcast::bebDeliver);
        // Start delivering
        while (running.get()) {
            try {
                Proposal proposal = linkDelivered.take();
                switch (proposal.getType()) {
                    case 0:
                        // If it's PROPOSAL type
                        proposalConsumer.accept(proposal);
                        break;
                    case 1:
                        // If it's ACK type
                        ackConsumer.accept(proposal);
                        break;
                    case 2:
                        // If it's NACK type
                        nackConsumer.accept(proposal);
                        break;
                        // It's a clean message
                    case 3:
                        LatticeConsensus.clean(proposal.getProposalNumber());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Put in the blocking queue the proposal to be delivered.
     * @param proposal to deliver
     */
    public static void bebDeliver(Proposal proposal) {
        try {
            // The method put blocks if the queue doesn't have enough space
            linkDelivered.put(proposal);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Broadcast to all host a given proposal and deliver to my host.
     * Simply put the proposal in a synchronized tree set, that
     * ensures processing in ascending order.
     * @param proposal to broadcast
     */
    public static void broadcast(Proposal proposal) {
        // Deliver to myself
        bebDeliver(Proposal.createProposal(proposal));
        // Broadcast to anyone else
        synchronized (proposalsToSend) {
            proposalsToSend.add(proposal);
        }
    }

    /**
     * Broadcast to all hosts a proposal of type ACK (or CLEAN).
     * @param proposal of ACK/CLEAN type to broadcast
     */
    public static void broadcastDelivered(Proposal proposal) {
        for (byte id : getNetwork().keySet()) {
            PerfectLink.sendAck(proposal, id);
        }
    }

    /**
     * Set atomically the running flag to false
     */
    public static void stopThreads() {
        running.set(false);
    }

}
