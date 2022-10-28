package cs451.message;

import java.util.LinkedList;
import java.util.List;

public class Compressor {

    public static final int MAX_COMPRESSION = 8;

    public static List<List<Message>> compress(int start, int nMessages, int senderId) {

        List<List<Message>> packets = new LinkedList<>();
        int packetLen = 0, packetCount = 0;

        for (int i = 1; i <= nMessages; i++) {

            Message m = Message.createMessage(senderId, i);

            if (packetLen == 0) {
                packets.add(new LinkedList<>());
            }

            packets.get(packetCount).add(m);
            packetLen++;

            if (packetLen == MAX_COMPRESSION) {
                packetLen = 0;
                packetCount++;
            }

        }

        return packets;
    }

    private Compressor() {}

}
