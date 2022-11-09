package cs451.message;

//import cs451.helper.Operations;

import java.util.Objects;

public class Message {

    public static final int MESSAGE_SIZE = Integer.BYTES;

    private final int senderId;
    private final int messageId;
    //private final byte[] content;

    public static Message createMessage(int senderId, int messageId) {
        return new Message(senderId, messageId);
    }

    private Message(int senderId, int messageId) {
        this.senderId = senderId;
        this.messageId = messageId;
        /* content = new byte[MESSAGE_SIZE];
        Operations.fromIntegerToByte(messageId, content, 0); */
    }

    public int getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return senderId == message.senderId && messageId == message.messageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, messageId);
    }
}
