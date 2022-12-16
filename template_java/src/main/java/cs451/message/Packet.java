package cs451.message;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static cs451.process.Process.MY_HOST;
import static cs451.utilities.Utilities.fromByteToIntegerArray;
import static cs451.utilities.Utilities.fromByteToLongArray;
import static cs451.utilities.Utilities.fromIntegerToByteArray;
import static cs451.utilities.Utilities.fromLongToByteArray;

/**
 * It represents a UDP packet. It contains metadata +
 * a set of proposals in a serialized form (byte array).
 */
public class Packet {

    /** Static constants used for the offset of metadata in the byte array */
    public static final int
                     MAX_COMPRESSION = 8,
                     PCK_ID_OS = 0,
                     IS_ACK_OS = Integer.BYTES + PCK_ID_OS,
                     SENDER_ID_OS = 1 + IS_ACK_OS,
                     TIMESTAMP_OS = 1 + SENDER_ID_OS,
                     NUMBER_PROPOSALS_OS = Long.BYTES + TIMESTAMP_OS,
                     HEADER = 1 + NUMBER_PROPOSALS_OS;

    /**
     * Max. dimension of a packet in bytes, it will be at most HEADER + 8 * MAX_PROPOSAL_SIZE,
     * where the maximum proposal size is (4 + ds) * 4
     */
    public static int MAX_PACKET_SIZE;

    /** Integer representing the unique packet id */
    private final int packetId;

    /** Flag to indicate whether the packet is an ack */
    private final boolean isAck;

    /** Host's id of the sender */
    private final byte senderId;

    /** Timestamp of the packet's creation */
    private long timestamp;

    /** Number of proposals packed */
    private final byte numberOfProposals;

    /** Serialized packed to be converted to datagram packet */
    private byte[] data;

    /**
     * Build a packet by extracting the metadata from the byte array.
     * @param data serialized packet (byte array)
     */
    public Packet(byte[] data) {
        this.packetId = fromByteToIntegerArray(data, PCK_ID_OS);
        this.isAck = data[IS_ACK_OS] == 1;
        this.senderId = data[SENDER_ID_OS];
        this.timestamp = fromByteToLongArray(data, TIMESTAMP_OS);
        this.numberOfProposals = data[NUMBER_PROPOSALS_OS];
        this.data = data;
    }

    /**
     * Build a packet given metadata and list of proposals.
     * It serializes the packet into a byte array.
     * @param proposals list of proposals to pack
     * @param packetId id of packet
     * @param senderId id of sender
     * @param len length of the byte array
     */
    public Packet(List<Proposal> proposals, int packetId, byte senderId, int len) {
        // Allocate the array
        this.data = new byte[len];
        this.packetId = packetId;
        this.senderId = senderId;
        this.isAck = false;
        this.timestamp = System.currentTimeMillis();
        this.numberOfProposals = (byte) proposals.size();

        // Write the header
        fromIntegerToByteArray(packetId, this.data, PCK_ID_OS);
        this.data[IS_ACK_OS] = 0;
        this.data[SENDER_ID_OS] = this.senderId;
        fromLongToByteArray(this.timestamp, this.data, TIMESTAMP_OS);
        this.data[NUMBER_PROPOSALS_OS] = this.numberOfProposals;

        // Write the proposals
        int ptr = HEADER;
        for (Proposal prop : proposals) {
            // Proposal ID
            fromIntegerToByteArray(prop.getProposalNumber(), this.data, ptr);
            ptr += Integer.BYTES;
            // Proposal Active Count
            fromIntegerToByteArray(prop.getActiveProposalNumber(), this.data, ptr);
            ptr += Integer.BYTES;
            // Proposal TYPE
            this.data[ptr++] = prop.getType();
            // If the proposal is ACK/CLEAN type then it has no proposed value
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

    /**
     * Private constructor that simply assign the passed parameters.
     * @param data byte array
     * @param packetId id of the packet
     * @param senderId id of the sender
     * @param isAck flag if it's ack or not
     * @param timestamp at creation time
     * @param numberOfProposals packed
     */
    private Packet(byte[] data, int packetId, byte senderId, boolean isAck, long timestamp, byte numberOfProposals) {
        this.packetId = packetId;
        this.isAck = isAck;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.numberOfProposals = numberOfProposals;
        this.data = data;
    }

    /**
     * @return byte array (serialized packet)
     */
    public byte[] getBytes() {
        return data;
    }

    /**
     * Change the timestamp to current time
     * and update the data array
     */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
        fromLongToByteArray(timestamp, data, TIMESTAMP_OS);
    }

    /**
     * Generate a new packet which is the ack of the given one.
     * @return ack packet
     */
    public Packet convertToAck() {
        // For the ack we simply need the header of packet
        byte[] newData = new byte[HEADER];
        // Simply change the isAck flag and the sender
        newData[IS_ACK_OS] = 1;
        newData[SENDER_ID_OS] = MY_HOST;
        return new Packet(newData, packetId, MY_HOST, true, timestamp, numberOfProposals);
    }

    /**
     * Apply a consumer function to the proposals contained in a packet.
     * @param callback consumer function to apply
     */
    public void applyToProposals(Consumer<Proposal> callback) {
        int ptr = HEADER;
        for (byte m = 0; m < numberOfProposals; m++) {
            Proposal proposal;
            int numProposal = fromByteToIntegerArray(data, ptr);
            ptr += Integer.BYTES;
            int activeId = fromByteToIntegerArray(data, ptr);
            ptr += Integer.BYTES;
            byte type = data[ptr++];
            // If type is ACK
            if (type == 1) {
                proposal = new Proposal(numProposal, type, senderId, null, activeId);
            } else if (type == 3) {
                proposal = new Proposal(numProposal);
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
        // Make sure the garbage collector frees data
        data = new byte[0];
    }

    /**
     * @return packet id
     */
    public int getPacketId() {
        return packetId;
    }

    /**
     * @return sender id
     */
    public byte getSenderId() {
        return senderId;
    }

    /**
     * @return true if it's ack, false otherwise
     */
    public boolean isAck() {
        return isAck;
    }

    /**
     * @return packet's timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return delta between current time and timestamp
     */
    public long getEmissionTime() {
        return System.currentTimeMillis() - timestamp;
    }

    @Override
    public String toString() {
        return "Packet{" +
            "packetId=" + packetId +
            ", isAck=" + isAck +
            ", senderId=" + senderId +
            ", timestamp=" + timestamp +
            ", numberOfProposals=" + numberOfProposals +
            "}";
    }
}
