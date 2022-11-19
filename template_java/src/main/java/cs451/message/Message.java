package cs451.message;

public class Message {

    public static final int MESSAGE_SIZE = 1 + Integer.BYTES;
    public static final int MESSAGE_LIMIT = Integer.MAX_VALUE;

    private final byte origin, sender;
    private final int payload;

    public Message(byte origin, int payload) {
        this.origin = origin;
        this.sender = origin;
        this.payload = payload;
    }

    public Message(byte origin, byte sender, int payload) {
        this.origin = origin;
        this.sender = origin;
        this.payload = payload;
    }

    public int getPayload() {
        return payload;
    }

    public byte getOrigin() {
        return origin;
    }

    public byte getSender() {
        return sender;
    }
}
