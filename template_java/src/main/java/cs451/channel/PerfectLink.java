package cs451.channel;

import cs451.broadcast.BestEffortBroadcast;
import cs451.message.Packet;
import cs451.message.Proposal;
import cs451.process.Process;

import java.net.SocketException;
import java.util.function.Consumer;

import static cs451.channel.Network.getProcess;
import static cs451.process.Process.MY_HOST;

/**
 * The highest channel abstraction.
 *
 * Main function:
 *  1) Deliver the packets, extract the proposals contained and deliver
 *     them to the upper layer.
 */
public class PerfectLink {

    /** Consumer function to apply to delivered proposals, given by upper layer */
    private static Consumer<Proposal> proposalConsumer;

    /**
     * Perfect link initialization.
     * @param proposalConsumer consumer function to call the delivery of the upper layer
     * @throws SocketException
     */
    public static void start(Consumer<Proposal> proposalConsumer) throws SocketException {
        PerfectLink.proposalConsumer = proposalConsumer;
        StubbornLink.start(PerfectLink::perfectDeliver);
    }

    /**
     * Consumer function given to the underlying layer.
     * Deliver the packet given its id,
     * then deliver the proposals contained in the packet,
     * calling the consumer function given by the upper layer.
     * @param packet received from stubborn link
     */
    private static void perfectDeliver(Packet packet) {
        Process sender = getProcess(packet.getSenderId());
        if (sender.deliverPacket(packet.getPacketId())) {
            packet.applyToProposals(p -> callback(p, sender));
        }
    }

    /**
     * Call the consumer function after delivering the proposal.
     * @param proposal to deliver
     * @param sender of the proposal
     */
    private static void callback(Proposal proposal, Process sender) {
        if (sender.deliver(proposal)) {
            proposalConsumer.accept(proposal);
        }
    }

    /**
     * Send a proposal of type ACK to the target.
     * @param proposal proposal ACK to be sent
     * @param targetId id of recipient
     */
    public static void sendAck(Proposal proposal, byte targetId) {
        if (targetId == MY_HOST) {
            // Don't send to myself but immediately deliver
            BestEffortBroadcast.bebDeliver(proposal);
        } else {
            getProcess(targetId).addAckProposal(proposal);
        }
    }

    /**
     * Send a proposal of type NACK to the target.
     * @param proposal proposal NACK to be sent
     * @param targetId id of recipient
     */
    public static void sendNack(Proposal proposal, byte targetId) {
        if (targetId == MY_HOST) {
            // Don't send to myself but immediately deliver
            BestEffortBroadcast.bebDeliver(proposal);
        } else {
            getProcess(targetId).addNackProposal(proposal);
        }
    }

}
