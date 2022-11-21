package cs451.message;

import cs451.utilities.Utilities;

import java.util.List;
import java.util.function.Consumer;

import static cs451.utilities.Utilities.fromIntegerToByteArray;
import static cs451.message.Message.MESSAGE_SIZE;

public class Packet {

    public static final int MAX_COMPRESSION = 8,
                     PCK_ID_OS = 0,
                     NUM_MEX_OS = Integer.BYTES + PCK_ID_OS,
                     SENDER_ID_OS = 1 + NUM_MEX_OS,
                     IS_ACK_OS = 1 + SENDER_ID_OS,
                     TIMESTAMP_OS = 1 + IS_ACK_OS,
                     MEX_OS = Integer.BYTES + TIMESTAMP_OS;
    public static final int MAX_PACKET_SIZE = MAX_COMPRESSION * MESSAGE_SIZE + MEX_OS;

    private final int packetId;
    private int timestamp;
    private final boolean isAck;
    private byte senderId;
    private final byte numMessages;
    private final byte[] data;

    public Packet(byte[] data) {
        this.packetId = Utilities.fromByteToIntegerArray(data, PCK_ID_OS);
        this.numMessages = data[NUM_MEX_OS];
        this.senderId = data[SENDER_ID_OS];
        this.isAck = data[IS_ACK_OS] != 0;
        this.timestamp = Utilities.fromByteToIntegerArray(data, TIMESTAMP_OS);
        this.data = data;
    }

    public Packet(List<Message> messages, int packetId, byte senderId) {
        this(messages, packetId, senderId, false, (int) System.currentTimeMillis());
    }

    private Packet(List<Message> messages, int packetId, byte senderId, boolean isAck, int timestamp ) {

        this.data = new byte[messages.size() * MESSAGE_SIZE + MEX_OS];

        this.packetId = packetId;
        this.numMessages = (byte) messages.size();
        this.senderId = senderId;
        this.isAck = isAck;
        this.timestamp = timestamp;

        fromIntegerToByteArray(packetId, this.data, PCK_ID_OS);
        this.data[NUM_MEX_OS] = this.numMessages;
        this.data[SENDER_ID_OS] = this.senderId;
        this.data[IS_ACK_OS] = (byte) (isAck? 1 : 0);
        fromIntegerToByteArray(timestamp, this.data, TIMESTAMP_OS);

        int ix = MEX_OS;
        for (Message m : messages) {
            this.data[ix] = m.getOrigin();
            fromIntegerToByteArray(m.getPayload(), this.data, ix + 1);
            ix += MESSAGE_SIZE;
        }
    }

    private Packet(byte[] data, byte numMessages, int packetId, byte senderId, boolean isAck, int timestamp) {
        this.packetId = packetId;
        this.numMessages = numMessages;
        this.senderId = senderId;
        this.isAck = isAck;
        this.timestamp = timestamp;
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
        newData[SENDER_ID_OS] = newLastSenderId;
        newData[IS_ACK_OS] = 1;
        return new Packet(newData, numMessages, packetId, newLastSenderId, true, timestamp);
    }

    public void applyToMessages(Consumer<Message> callback) {
        int ix = MEX_OS;
        for (byte m = 0; m < numMessages; m++) {
            callback.accept(new Message(this.data[ix], senderId, Utilities.fromByteToIntegerArray(data, ix + 1)));
            ix += MESSAGE_SIZE;
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
