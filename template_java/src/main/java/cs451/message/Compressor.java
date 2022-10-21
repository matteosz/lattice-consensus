package cs451.message;

import java.util.LinkedList;
import java.util.List;

public class Compressor {

    public static final int MAX_COMPRESSION = 8;
    private Compressor() {}

    public static List<List<Message>> compress(int nMessages, int senderId) {

        List<List<Message>> packet = new LinkedList<>();
        int packetLen = 0, packetCount = 0;

        for (int i = 1; i <= nMessages; i++) {
            Message m = Message.createMessage(senderId, i);

            if (packetLen == 0) {
                packet.add(new LinkedList<>());
            }

            packet.get(packetCount).add(m);
            packetLen++;

            if (packetLen == MAX_COMPRESSION) {
                packetLen = 0;
                packetCount++;
            }
        }

        return packet;
    }

}
