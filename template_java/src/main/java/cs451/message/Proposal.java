package cs451.message;

import java.util.HashSet;
import java.util.Set;

public class Proposal implements Comparable<Proposal> {
    private static final int ACK_COUNT = 1 + 2 * Integer.BYTES;
    private final int proposalNumber;
    private int activeProposalNumber;
    private final byte type,
    /**
     * Type mapping 0 -> PROPOSAL
     *              1 -> ACK
     *              2 -> NACK
     */
                        sender;
    private Set<Integer> proposedValues;

    public Proposal(int proposalNumber, byte type, byte sender,
        Set<Integer> proposedValues, int activeProposalNumber) {
        this.proposalNumber = proposalNumber;
        this.type = type;
        this.sender = sender;
        this.proposedValues = proposedValues;
        this.activeProposalNumber = activeProposalNumber;
    }

    public static Proposal createProposal(Proposal proposal) {
        Set<Integer> proposes;
        if (proposal.isAck()) {
            proposes = null;
        } else {
            proposes = new HashSet<>();
            proposes.addAll(proposal.getProposedValues());
        }
        return new Proposal(proposal.getProposalNumber(), proposal.getType(),
            proposal.getSender(), proposes, proposal.getActiveProposalNumber());
    }

    public static Proposal createProposal(int proposalNumber, byte type, byte sender, Set<Integer> values, int activeProposalNumber) {
        Set<Integer> proposes = new HashSet<>();
        proposes.addAll(values);
        return new Proposal(proposalNumber, type, sender, proposes, activeProposalNumber);
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public int getActiveProposalNumber() {
        return activeProposalNumber;
    }

    public void setNack(int activeProposalNumber, Set<Integer> proposedValues) {
        this.activeProposalNumber = activeProposalNumber;
        this.proposedValues = proposedValues;
    }

    public byte getType() {
        return type;
    }

    public Set<Integer> getProposedValues() {
        return proposedValues;
    }

    public byte getSender() {
        return sender;
    }

    public int getLength() {
        return proposedValues.size();
    }

    public int getBytes() {
        if (type == 1) {
            return ACK_COUNT;
        } else {
            return ACK_COUNT + (1 + proposedValues.size()) * Integer.BYTES;
        }
    }

    public boolean isAck() {
        return type == 1;
    }


    public static boolean isAck(byte type) {
        return type == 1;
    }

    @Override
    public int compareTo(Proposal o) {
        int diff = this.proposalNumber - o.proposalNumber;
        return diff==0? this.type - o.type : diff;
    }
}
