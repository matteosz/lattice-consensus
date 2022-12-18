package cs451.message;

import java.util.HashSet;
import java.util.Set;

/**
 * It represents a proposal in a consensus round.
 * <p>
 * A proposal is a set of integers plus some metadata.
 */
public class Proposal {

    /** Length in bytes of an ack proposal -> 1 byte + 2 integers. */
    private static final int ACK_COUNT = 1 + 2 * Integer.BYTES;

    /** Proposal unique id. */
    private final int proposalNumber;

    /** Active proposal number used in consensus. */
    private int activeProposalNumber;

    /**
     * Type of proposal: 0 -> PROPOSAL.
     *                   1 -> ACK.
     *                   2 -> NACK.
     *                   3 -> CLEAN.
     */
    private final byte type;

    /** Sender id of the proposal. */
    private byte sender;

    /** Real proposal, set of integers. */
    private Set<Integer> proposedValues;

    /**
     * Simple constructor for a proposal.
     * @param proposalNumber id of the proposal.
     * @param type of proposal (0, 1, 2).
     * @param sender id.
     * @param proposedValues set of integer.
     * @param activeProposalNumber current active count in the consensus round.
     */
    public Proposal(int proposalNumber, byte type, byte sender,
        Set<Integer> proposedValues, int activeProposalNumber) {
        this.proposalNumber = proposalNumber;
        this.type = type;
        this.sender = sender;
        this.proposedValues = proposedValues;
        this.activeProposalNumber = activeProposalNumber;
    }

    /**
     * Create a proposal of type CLEAN.
     * @param proposalNumber id of proposal to clean.
     */
    public Proposal(int proposalNumber) {
        this.proposalNumber = proposalNumber;
        this.type = 3;
    }

    /**
     * Statically create a proposal given some proposed values.
     * These values are hard copied into a new hash set.
     * @param proposalNumber id of the proposal.
     * @param type (0, 1, 2).
     * @param sender id.
     * @param values proposed values to use in the proposal.
     * @param activeProposalNumber active count in consensus.
     * @return new proposal.
     */
    public static Proposal createProposal(int proposalNumber, byte type, byte sender, Set<Integer> values, int activeProposalNumber) {
        return new Proposal(proposalNumber, type, sender, new HashSet<>(values), activeProposalNumber);
    }

    /**
     * Deep copy the given proposal.
     * @param proposal to copy.
     * @return new copy.
     */
    public static Proposal createProposal(Proposal proposal) {
        return new Proposal(proposal.getProposalNumber(), proposal.getType(), proposal.getSender(), new HashSet<>(proposal.getProposedValues()), proposal.getActiveProposalNumber());
    }

    /**
     * @return proposal id.
     */
    public int getProposalNumber() {
        return proposalNumber;
    }

    /**
     * @return active proposal count.
     */
    public int getActiveProposalNumber() {
        return activeProposalNumber;
    }

    /**
     * @return type of the proposal (0, 1, 2, 3).
     */
    public byte getType() {
        return type;
    }

    /**
     * @return set of proposed values (int).
     */
    public Set<Integer> getProposedValues() {
        return proposedValues;
    }

    /**
     * @return sender id.
     */
    public byte getSender() {
        return sender;
    }

    /**
     * @return number of proposed values.
     */
    public int getLength() {
        return proposedValues.size();
    }

    /**
     * Compute the length that the proposal has in bytes when
     * serialized within a packet.
     * @return number of bytes used.
     */
    public int getBytes() {
        if (type == 1 || type == 3) {
            return ACK_COUNT;
        } else {
            // If not an ack then it's present also the number of proposals (int)
            // and the set of proposals (int as well)
            return ACK_COUNT + (1 + proposedValues.size()) * Integer.BYTES;
        }
    }

    /**
     * @return true if type is ACK or CLEAN, false otherwise.
     */
    public boolean isAck() {
        return type == 1 || type == 3;
    }

}
