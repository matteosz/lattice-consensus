package cs451.message;

public class Message {

    public static final int MESSAGE_SIZE = Integer.BYTES;

    private final byte sender;
    private final int message;

    public Message(byte sender, int message) {
        this.sender = sender;
        this.message = message;
    }

    public int getMessage() {
        return message;
    }

    public int getOrigin() {
        return sender + 1;
    }

}
