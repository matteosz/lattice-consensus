package cs451.broadcast;

import cs451.channel.PerfectLink;

import cs451.message.Proposal;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static cs451.process.Process.getMyHost;
import static cs451.utilities.Parameters.BROADCAST_BATCH;

public class BestEffortBroadcast {

    private final PerfectLink link;
    private final BlockingQueue<Proposal> linkDelivered;
    private final Consumer<Proposal> proposalConsumer, ackConsumer, nackConsumer;
    private final int numHosts;
    private final AtomicBoolean running;

    public BestEffortBroadcast(int port, int numHosts, Consumer<Proposal> proposalConsumer, Consumer<Proposal> ackConsumer, Consumer<Proposal> nackConsumer) throws SocketException {
        this.proposalConsumer = proposalConsumer;
        this.ackConsumer = ackConsumer;
        this.nackConsumer = nackConsumer;

        this.numHosts = numHosts;
        this.running = new AtomicBoolean(true);
        this.linkDelivered = new LinkedBlockingQueue<>();

        this.link = new PerfectLink(port, this::bebDeliver);
    }

    private void bebDeliver(Proposal proposal) {
        try {
            linkDelivered.put(proposal);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void load(List<Proposal> proposals) {

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            if (h != getMyHost()) {
                link.load(proposals, h);
            }
        }
        while (running.get()) {
            try {
                Proposal proposal = linkDelivered.take();
                switch (proposal.getType()) {
                    case 0:
                        proposalConsumer.accept(proposal);
                        break;
                    case 1:
                        ackConsumer.accept(proposal);
                        break;
                    case 2:
                        nackConsumer.accept(proposal);
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void bebBroadcast(Proposal proposal) {

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            if (h == proposal.getSender() && h != getMyHost()) {
                continue;
            }
            if (h == getMyHost()) {
                //proposalConsumer.accept(proposal);
            } else {
                link.send(Proposal.createProposal(proposal), h);
            }
        }
    }

    public PerfectLink getLink() {
        return link;
    }

    public void stopThreads() {
        running.set(false);
        link.stopThreads();
    }
}
