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

public class UniformReliableBroadcast extends Broadcast {

    private final BestEffortBroadcast broadcast;
    private final Map<Byte, Compressor> urbDelivered;
    private final Map<Byte, Map<Byte, Compressor>> bebDelivered;

    public UniformReliableBroadcast(int port, int numHosts, Consumer<Message> callback) throws SocketException {
        super(callback);
        this.urbDelivered = new HashMap<>();
        this.bebDelivered = new HashMap<>();

        broadcast = new BestEffortBroadcast(port, numHosts, this::urbDeliver);

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            urbDelivered.put(h, new Compressor());
            if (h != getMyHost()) {
                bebDelivered.put(h, getProcess(h).getDelivered());
            } else {
                Map<Byte, Compressor> localAck = new HashMap<>();
                for (byte l = 0; l >= 0 && l < numHosts; l++) {
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

        if (urbDelivered.get(originId).contains(messageId)) {
            return;
        }

        if (bebDelivered.get(getMyHost()).get(originId).add(messageId)) {

            broadcast.bebBroadcast(message);

        } else if (bebDelivered.values()
                .stream()
                .filter(x -> x.get(originId).contains(messageId))
                .count() > broadcast.getNumHosts() / 2) {

            urbDelivered.get(originId).add(messageId);
            callback(message);

        }
    }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }
}
