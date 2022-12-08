package cs451.consensus;

import static cs451.process.Process.getMyHost;

import cs451.broadcast.BestEffortBroadcast;
import cs451.message.Compressor;
import cs451.message.Proposal;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LatticeConsensus {

    private final BestEffortBroadcast broadcast;
    private final BiConsumer<Set<Integer>, Integer> callback;
    private final int majority;
    private final boolean[] active;
    private final Map<Integer, Integer> activeProposal;
    // byte[2] -> ack - nack
    private final Map<Integer, byte[]> ackCount;
    private final Map<Integer, Set<Integer>> proposedValue, acceptedValue;
    private final Compressor delivered;

    public LatticeConsensus(int port, int numHosts, BiConsumer<Set<Integer>, Integer> deliverCallback, int numProposals)
        throws SocketException {
        callback = deliverCallback;
        majority = numHosts / 2;

        active = new boolean[numProposals + 1];

        ackCount = new HashMap<>();
        activeProposal = new HashMap<>();

        proposedValue = new HashMap<>();
        acceptedValue = new HashMap<>();

        delivered = new Compressor();
        delivered.add(0);

        for (int i = 0; i < numProposals; i++) {
            proposedValue.put(i + 1, new HashSet<>());
            acceptedValue.put(i + 1, new HashSet<>());
            ackCount.put(i + 1, new byte[]{1, 0});
            activeProposal.put(i + 1, 1);
            active[i] = true;
        }

        broadcast = new BestEffortBroadcast(port, numHosts, this::deliverProposal, this::deliverAck, this::deliverNAck);
    }

    public void start(List<Proposal> proposals) {
        for (Proposal proposal : proposals) {
            int id = proposal.getProposalNumber();
            proposedValue.get(id).addAll(proposal.getProposedValues());
            acceptedValue.get(id).addAll(proposal.getProposedValues());
        }
        broadcast.load(proposals);
    }

    private void deliverProposal(Proposal proposal) {
        int id = proposal.getProposalNumber();
        if (proposal.getProposedValues().containsAll(acceptedValue.get(id))) {
            acceptedValue.put(id, proposal.getProposedValues());
            broadcast.getLink().send(new Proposal(id, (byte) 1, getMyHost(), null, proposal.getActiveProposalNumber()), proposal.getSender());
        } else {
            acceptedValue.get(id).addAll(proposal.getProposedValues());
            broadcast.getLink().send(Proposal.createProposal(id, (byte) 2, getMyHost(), acceptedValue.get(id), proposal.getActiveProposalNumber()), proposal.getSender());
        }
    }

    private void deliverAck(Proposal proposal) {

        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();

        if (activeId == activeProposal.get(id)) {
            ackCount.get(id)[0]++;
            checkAck(id);
            checkNAck(proposal);
        }
    }

    private void deliverNAck(Proposal proposal) {

        int id = proposal.getProposalNumber(), activeId = proposal.getActiveProposalNumber();

        if (activeId == activeProposal.get(id)) {
            proposedValue.get(id).addAll(proposal.getProposedValues());
            ackCount.get(id)[1]++;
            checkNAck(proposal);
        }
    }

    private void checkNAck(Proposal proposal) {

        int proposalId = proposal.getProposalNumber();
        byte[] ack = ackCount.get(proposalId);

        if (ack[1] > 0 && (ack[0] + ack[1] > majority) && active[proposalId]) {
            int activeId = activeProposal.get(proposalId) + 1;
            activeProposal.put(proposalId, activeId);
            ack[0] = 1;
            acceptedValue.get(proposalId).clear();
            acceptedValue.get(proposalId).addAll(proposedValue.get(proposalId));
            ack[1] = 0;

            proposal.setNack(activeId, proposedValue.get(proposalId));
            broadcast.bebBroadcast(proposal);
        }
    }

    private void checkAck(int id) {
        if (ackCount.get(id)[0] > majority && active[id]) {
            active[id] = false;

            int lastDelivered = delivered.takeLast() + 1;
            delivered.add(id);
            int lastToDeliver = delivered.takeLast();

            while (lastDelivered <= lastToDeliver) {
                callback.accept(proposedValue.get(lastDelivered), lastDelivered);
                clean(lastDelivered);
                lastDelivered++;
            }
        }
    }

    private void clean(int id) {

    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
