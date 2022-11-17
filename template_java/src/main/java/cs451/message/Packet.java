package cs451.message;

import cs451.helper.Operations;

import java.util.List;
import java.util.function.Consumer;

public class Packet {

    public static final int MAX_COMPRESSION = 8;
    public static final int NUM_MEX_OS = 0, PCK_ID_OS = 1 + NUM_MEX_OS,
                     FIRST_SENDER_ID_OS = Integer.BYTES + PCK_ID_OS,
                     LAST_SENDER_ID_OS = 1 + FIRST_SENDER_ID_OS,
                     IS_ACK_OS = 1 + LAST_SENDER_ID_OS,
                     TIMESTAMP_OS = 1 + IS_ACK_OS,
                     MEX_OS = Integer.BYTES + TIMESTAMP_OS;
    public static final int MAX_PACKET_SIZE = MAX_COMPRESSION * Message.MESSAGE_SIZE + MEX_OS;

    private final int packetId;
    private int timestamp;
    private final byte[] data;
    private final boolean isAck;
    private byte originId, lastSenderId, numMessages;

    public Packet(byte[] data) {
        this.numMessages = data[NUM_MEX_OS];
        this.packetId = Operations.fromByteToInteger(data, PCK_ID_OS);
        this.originId = (byte) (data[FIRST_SENDER_ID_OS] + 1);
        this.lastSenderId = (byte) (data[LAST_SENDER_ID_OS] + 1);
        this.isAck = data[IS_ACK_OS] != 0;
        this.timestamp = Operations.fromByteToInteger(data, TIMESTAMP_OS);
        this.data = data;
    }

    public Packet(List<Message> messages, int packetId, int originId, int lastSenderId) {
        this(messages, packetId, originId, lastSenderId, false, (int) System.currentTimeMillis());
    }

    private Packet(List<Message> messages, int packetId, int originId, int lastSenderId, boolean isAck, int timestamp ) {

        int numMessages = messages.size();

        byte[] data = new byte[numMessages * Message.MESSAGE_SIZE + MEX_OS];

        data[NUM_MEX_OS] = (byte) numMessages;
        Operations.fromIntegerToByte(packetId, data, PCK_ID_OS);
        data[FIRST_SENDER_ID_OS] = (byte) (originId - 1);
        data[LAST_SENDER_ID_OS] = (byte) (lastSenderId - 1);
        data[IS_ACK_OS] = (byte) (isAck ? 1 : 0);
        Operations.fromIntegerToByte(timestamp, data, TIMESTAMP_OS);

        int ix = MEX_OS;
        for (Message m : messages) {
            Operations.fromIntegerToByte(m.getMessageId(), data, ix);
            ix += Message.MESSAGE_SIZE;
        }

        this.numMessages = (byte) numMessages;
        this.packetId = packetId;
        this.originId = (byte) originId;
        this.lastSenderId = (byte) lastSenderId;
        this.isAck = isAck;
        this.timestamp = timestamp;
        this.data = data;
    }

    private Packet(byte[] data, byte numMessages, int packetId, byte originId, byte lastSenderId, boolean isAck, int timestamp) {
        this.numMessages = numMessages;
        this.packetId = packetId;
        this.originId = originId;
        this.lastSenderId = lastSenderId;
        this.isAck = isAck;
        this.timestamp = timestamp;
        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    public Packet setLastSenderId(int newLastSenderId) {
        if (lastSenderId == newLastSenderId)
            return this;

        byte[] newData = data.clone();
        newData[LAST_SENDER_ID_OS] = (byte) (newLastSenderId - 1);

        return new Packet(newData, numMessages, packetId, originId, (byte) newLastSenderId, false, timestamp);
    }

    public void updateTimestamp() {
        this.timestamp = (int) System.currentTimeMillis();
        Operations.fromIntegerToByte(timestamp, data, TIMESTAMP_OS);
    }

    public Packet convertToAck(int newLastSenderId) {
        byte[] newData = data.clone();
        newData[LAST_SENDER_ID_OS] = (byte) (newLastSenderId - 1);
        newData[IS_ACK_OS] = 1;

        return new Packet(newData, numMessages, packetId, originId, (byte) newLastSenderId, true, timestamp);
    }

    public void applyToMessages(Consumer<Message> callback) {
        int ix = MEX_OS;
        for (int i = 0; i < numMessages; i++) {
            callback.accept(new Message(lastSenderId, originId, Operations.fromByteToInteger(data, ix)));
            ix += Message.MESSAGE_SIZE;
        }
    }

    public int getPacketId() {
        return packetId;
    }

    public int getLastSenderId() {
        return lastSenderId;
    }

    public int getOriginId() {
        return originId;
    }

    public boolean isAck() {
        return isAck;
    }

    public int getEmissionTime() {
        return (int) System.currentTimeMillis() - timestamp;
    }

}
