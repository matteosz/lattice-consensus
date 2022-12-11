package cs451.broadcast;

import static cs451.process.Process.proposalsToSend;
import static cs451.utilities.Parameters.BROADCAST_BATCH;

import cs451.channel.PerfectLink;

import cs451.message.Proposal;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * BEB broadcast:
 *
 * It serves to send to all hosts a given proposal.
 *
 * It has 2 main functions:
 *  1) Freeing the delivery of the underlying layer putting the
 *     delivered proposals in a queue to process them later on.
 *  2) Send to all the hosts a given proposal.
 */
public class BestEffortBroadcast {

    /**
     * Blocking queue of proposals delivered from underlying layer to be delivered by the upper one
     * with a maximum constant capacity to limit the amount of memory used
     */
    private static final BlockingQueue<Proposal> linkDelivered = new LinkedBlockingQueue<>(BROADCAST_BATCH);

    /** Running flag to stop the thread when the application stops */
    private static final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Initialize the Beb broadcast.
     * Then, take from the blocking queue the proposals
     * and call the consumer function based on its type
     * to deliver them to the upper layer.
     * @param port integer representing the port to bind the datagram socket
     * @param proposalConsumer consumer function for proposal of type PROPOSAL
     * @param ackConsumer consumer function for proposal of type ACK
     * @param nackConsumer consumer function for proposal of type NACK
     * @throws SocketException
     */
    public static void start(int port, Consumer<Proposal> proposalConsumer, Consumer<Proposal> ackConsumer, Consumer<Proposal> nackConsumer) throws SocketException {
        PerfectLink.start(port, BestEffortBroadcast::bebDeliver);
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
    private static void bebDeliver(Proposal proposal) {
        try {
            // The method put blocks if the queue doesn't have enough space
            linkDelivered.put(proposal);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Broadcast to all hosts the given proposal.
     * @param proposal to be broadcast
     */
    public static void broadcast(Proposal proposal) {
        // Simply put the proposal at the beginning of the shared list
        // The beginning ensures that will be loaded before the original proposals
        proposalsToSend.addFirst(proposal);
    }

    /**
     * Set atomically the running flag to false
     */
    public static void stopThreads() {
        running.set(false);
    }

}
