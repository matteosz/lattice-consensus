package cs451;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {

    private static byte[] getIntMessage(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static List<Message> getIntList(int start, int end) {
        List<Message> messages = new ArrayList<>(end-start+1);
        for (int i=start; i<=end; i++)
            messages.add(new Message(getIntMessage(i)));
        return messages;
    }

    public static byte[] getIntBytes(List<Message> messages) {
        byte[] buffer = new byte[messages.size()*4];
        for (int i=0; i<messages.size(); i++)
            System.arraycopy(messages.get(i).getContent(), 0, buffer, i*4, 4);
        return buffer;
    }

    private final byte[] content;

    public Message(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Arrays.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
}
