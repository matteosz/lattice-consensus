package cs451.message;

import cs451.utilities.Utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static cs451.utilities.Utilities.fromByteToInteger;
import static cs451.utilities.Utilities.fromByteToIntegerArray;
import static cs451.utilities.Utilities.fromIntegerToByteArray;

public class Packet {

    public static final int
                     MAX_COMPRESSION = 8,
                     PCK_ID_OS = 0,
                     IS_ACK_OS = Integer.BYTES + PCK_ID_OS,
                     SENDER_ID_OS = 1 + IS_ACK_OS,
                     TIMESTAMP_OS = 1 + SENDER_ID_OS,
                     NUMBER_PROPOSALS_OS = Integer.BYTES + TIMESTAMP_OS,
                     MEX_OS = 1 + NUMBER_PROPOSALS_OS,
                     MAX_PACKET_SIZE = 4000;

    private final int packetId;
    private final boolean isAck;
    private byte senderId;
    private int timestamp;
    private byte numberOfProposals;
    private final byte[] data;

    public Packet(byte[] data) {
        this.packetId = Utilities.fromByteToIntegerArray(data, PCK_ID_OS);
        this.isAck = data[IS_ACK_OS] != 0;
        this.senderId = data[SENDER_ID_OS];
        this.timestamp = Utilities.fromByteToIntegerArray(data, TIMESTAMP_OS);
        this.numberOfProposals = data[NUMBER_PROPOSALS_OS];
        this.data = data;
    }

    public Packet(List<Proposal> proposals, int packetId, byte senderId, int len) {

        this.data = new byte[len];
        this.packetId = packetId;
        this.senderId = senderId;
        this.isAck = false;
        this.timestamp = (int) System.currentTimeMillis();
        this.numberOfProposals = (byte) proposals.size();

        fromIntegerToByteArray(packetId, this.data, PCK_ID_OS);
        this.data[IS_ACK_OS] = 0;
        this.data[SENDER_ID_OS] = this.senderId;
        fromIntegerToByteArray(this.timestamp, this.data, TIMESTAMP_OS);
        this.data[NUMBER_PROPOSALS_OS] = (byte) proposals.size();

        int ptr = MEX_OS;
        for (Proposal prop : proposals) {
            fromIntegerToByteArray(prop.getProposalNumber(), this.data, ptr);
            ptr += Integer.BYTES;
            fromIntegerToByteArray(prop.getActiveProposalNumber(), this.data, ptr);
            ptr += Integer.BYTES;
            this.data[ptr++] = prop.getType();
            if (!prop.isAck()) {
                fromIntegerToByteArray(prop.getLength(), this.data, ptr);
                ptr += Integer.BYTES;
                for (int x : prop.getProposedValues()) {
                    fromIntegerToByteArray(x, this.data, ptr);
                    ptr += Integer.BYTES;
                }
            }
        }
    }

    private Packet(byte[] data, int packetId, byte senderId, boolean isAck, int timestamp, byte numberOfProposals) {
        this.packetId = packetId;
        this.isAck = isAck;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.numberOfProposals = numberOfProposals;
        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    public void setLastSenderId(byte newLastSenderId) {
        data[SENDER_ID_OS] = newLastSenderId;
        senderId = newLastSenderId;
    }

    public void updateTimestamp() {
        this.timestamp = (int) System.currentTimeMillis();
        fromIntegerToByteArray(timestamp, data, TIMESTAMP_OS);
    }

    public Packet convertToAck(byte newLastSenderId) {
        byte[] newData = data.clone();
        newData[IS_ACK_OS] = 1;
        newData[SENDER_ID_OS] = newLastSenderId;
        return new Packet(newData, packetId, newLastSenderId, true, timestamp, numberOfProposals);
    }

    public void applyToProposals(Consumer<Proposal> callback) {
        int ptr = MEX_OS;
        for (byte m = 0; m < numberOfProposals; m++) {
            Proposal proposal;
            int numProposal = fromByteToIntegerArray(data, ptr);
            ptr += Integer.BYTES;
            int activeId = fromByteToIntegerArray(data, ptr);
            ptr += Integer.BYTES;
            byte type = data[ptr++];
            if (Proposal.isAck(type)) {
                proposal = new Proposal(numProposal, type, senderId, null, activeId);
            } else {
                int numValues = fromByteToIntegerArray(data, ptr);
                ptr += Integer.BYTES;
                Set<Integer> proposedValues = new HashSet<>(numValues);
                for (int i = 0; i < numValues; i++) {
                    proposedValues.add(fromByteToIntegerArray(data, ptr));
                    ptr += Integer.BYTES;
                }
                proposal = new Proposal(numProposal, type, senderId, proposedValues, activeId);
            }
            callback.accept(proposal);
        }
    }

    public int getPacketId() {
        return packetId;
    }

    public byte getSenderId() {
        return senderId;
    }

    public boolean isAck() {
        return isAck;
    }

    public int getEmissionTime() {
        return (int) System.currentTimeMillis() - timestamp;
    }

}
