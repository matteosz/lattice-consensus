package cs451.message;

public class Message {

    public static final int MESSAGE_SIZE = Integer.BYTES;

    private final byte lastSender, firstSender;
    private final int messageId;

    public Message(int lastSender, int firstSender, int messageId) {
        this.lastSender = (byte) lastSender;
        this.firstSender = (byte) firstSender;
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getFirstSender() {
        return firstSender;
    }

    public int getLastSender() {
        return lastSender;
    }

}
