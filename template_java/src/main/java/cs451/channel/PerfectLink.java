package cs451.channel;

import cs451.message.Packet;
import cs451.message.Proposal;
import cs451.process.Process;

import java.net.SocketException;
import java.util.List;
import java.util.function.Consumer;

import static cs451.channel.Link.getProcess;

public class PerfectLink {

    private final StubbornLink link;
    private final Consumer<Proposal> proposalConsumer;

    public PerfectLink(int port, Consumer<Proposal> proposalConsumer) throws SocketException {
        this.proposalConsumer = proposalConsumer;
        link = new StubbornLink(port, this::perfectDeliver);
    }

    private void perfectDeliver(Packet packet) {
        Process sender = getProcess(packet.getSenderId());

        if (sender.deliverPacket(packet)) {
            packet.applyToProposals(p -> callback(p, sender));
        }
    }

    private void callback(Proposal proposal, Process sender) {
        if (sender.deliver(proposal)) {
            proposalConsumer.accept(proposal);
        }
    }

    public void load(List<Proposal> proposals, byte targetId) {
        getProcess(targetId).load(proposals);
    }
    public void send(Proposal proposal, byte targetId) {
        getProcess(targetId).addProposal(proposal);
    }

    public void stopThreads() {
        link.stopThreads();
    }
}
