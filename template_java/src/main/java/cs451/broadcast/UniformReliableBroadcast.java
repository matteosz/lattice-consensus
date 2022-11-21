package cs451.broadcast;

import cs451.message.Compressor;
import cs451.message.Message;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static cs451.channel.Link.getProcess;
import static cs451.message.Message.MESSAGE_LIMIT;
import static cs451.process.Process.getMyHost;
import static cs451.utilities.Parameters.MAJORITY;
import static cs451.utilities.Parameters.NUM_HOSTS;

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;
    private final Map<Byte, Compressor> urbDelivered;
    private final Map<Byte, Map<Byte, Compressor>> bebDelivered;

    public UniformReliableBroadcast(int port, Consumer<Message> callback) throws SocketException {
        super(callback);
        this.urbDelivered = new HashMap<>();
        this.bebDelivered = new HashMap<>();

        broadcast = new BestEffortBroadcast(port, this::urbDeliver);

        for (byte h = 0; h >= 0 && h < NUM_HOSTS; h++) {
            urbDelivered.put(h, new Compressor());
            if (h != getMyHost()) {
                bebDelivered.put(h, getProcess(h).getDelivered());
            } else {
                Map<Byte, Compressor> localAck = new HashMap<>();
                for (byte l = 0; l >= 0 && l < NUM_HOSTS; l++) {
                    Compressor compressed = new Compressor();
                    if (l == getMyHost()) {
                        compressed.setHead(1, MESSAGE_LIMIT);
                    }
                    localAck.put(l, compressed);
                }
                bebDelivered.put(h, localAck);
            }
        }
    }

    private void urbDeliver(Message message) {
        int messageId = message.getPayload();
        byte originId = message.getOrigin();
        Compressor fromOrigin = urbDelivered.get(originId);

        if (fromOrigin.contains(messageId)) {
            return;
        }

        if (bebDelivered.get(getMyHost()).get(originId).add(messageId)) {

            broadcast.bebBroadcast(message);

        } else if (canDeliver(originId, messageId)) {

            fromOrigin.add(messageId);
            callback(message);

        }
    }

    private boolean canDeliver(byte originId, int messageId) {
        int ack  = 0;
        for (Map<Byte, Compressor> entry : bebDelivered.values()) {
            if (entry.get(originId).contains(messageId)) {
                ack++;
                if (ack == MAJORITY) {
                    return true;
                }
            }
        }
        return false;
    }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
