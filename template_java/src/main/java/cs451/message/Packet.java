package cs451.message;

import cs451.helper.Operations;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Packet {

    public static final int MAX_COMPRESSION = 8;
    public static final int NUM_MEX_OS = 0, PCK_ID_OS = Integer.BYTES + NUM_MEX_OS,
                     FIRST_SENDER_ID_OS = Integer.BYTES + PCK_ID_OS,
                     LAST_SENDER_ID_OS = 1 + FIRST_SENDER_ID_OS,
                     IS_ACK_OS = 1 + LAST_SENDER_ID_OS, MEX_OS = 1 + IS_ACK_OS;
    public static final int HEADER = MEX_OS;
    public static final int MAX_PACKET_SIZE = MAX_COMPRESSION*Message.MESSAGE_SIZE + HEADER;

    private final int numMessages, packetId;
    private int originId, senderId;
    private boolean isAck;
    private final byte[] data;

    public static Packet getPacket(byte[] data) {

        int numMessages = Operations.fromByteToInteger(data, NUM_MEX_OS);
        int packetId = Operations.fromByteToInteger(data, PCK_ID_OS);
        int firstSenderId = data[FIRST_SENDER_ID_OS];
        int lastSenderId = data[LAST_SENDER_ID_OS];
        boolean isAck = data[IS_ACK_OS] != 0;

        return new Packet(data, numMessages, packetId, firstSenderId, lastSenderId, isAck);
    }

    private Packet(byte[] data, int numMessages, int packetId, int originId, int senderId, boolean isAck) {
        this.numMessages = numMessages;
        this.packetId = packetId;
        this.originId = originId;
        this.senderId = senderId;
        this.isAck = isAck;
        this.data = data;
    }

    public Packet(List<Message> messages, int packetId, int originId, int senderId) {
        this(messages, packetId, originId, senderId, false);
    }

    private Packet(List<Message> messages, int packetId, int originId, int senderId, boolean isAck) {

        int numMessages = messages.size();

        byte[] data = new byte[numMessages*Message.MESSAGE_SIZE+HEADER];

        Operations.fromIntegerToByte(numMessages, data, NUM_MEX_OS);
        Operations.fromIntegerToByte(packetId, data, PCK_ID_OS);
        data[FIRST_SENDER_ID_OS] = (byte) originId;
        data[LAST_SENDER_ID_OS] = (byte) senderId;
        data[IS_ACK_OS] = (byte) (isAck ? 1 : 0);

        int ix = MEX_OS;
        for (Message m : messages) {
            Operations.fromIntegerToByte(m.getMessageId(), data, ix);
            ix += Message.MESSAGE_SIZE;
        }

        this.numMessages = numMessages;
        this.packetId = packetId;
        this.originId = originId;
        this.senderId = senderId;
        this.isAck = isAck;
        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    public Packet setSenderId(int newLastSenderId) {
        byte[] newData = data.clone();
        newData[LAST_SENDER_ID_OS] = (byte) newLastSenderId;

        return new Packet(newData, numMessages, packetId, originId, newLastSenderId, false);
    }

    public Packet convertToAck(int newLastSenderId) {

        byte[] newData = data.clone();
        newData[LAST_SENDER_ID_OS] = (byte) newLastSenderId;
        newData[IS_ACK_OS] = 1;

        return new Packet(newData, numMessages, packetId, originId, newLastSenderId, true);
    }

    public Packet backFromAck(int oldSenderId) {
        this.senderId = oldSenderId;
        return this;
    }

    public List<Message> getMessages() {
        List<Message> messagesPacked = new LinkedList<>();

        int ix= MEX_OS;
        for (int i = 0; i < numMessages; i++) {
            int mexId = Operations.fromByteToInteger(data, ix);
            ix += Message.MESSAGE_SIZE;
            messagesPacked.add(new Message(senderId, mexId));
        }

        return messagesPacked;
    }

    public int getPacketId() {
        return packetId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getOriginId() {
        return originId;
    }

    public boolean isAck() {
        return isAck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return packetId == packet.packetId && originId == packet.originId && senderId == packet.senderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetId, originId, senderId);
    }
}
