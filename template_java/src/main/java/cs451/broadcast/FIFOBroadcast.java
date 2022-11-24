package cs451.broadcast;

import cs451.message.Compressor;
import cs451.message.Message;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import static cs451.process.Process.getMyHost;

public class FIFOBroadcast {

    private final UniformReliableBroadcast broadcast;
    private final Consumer<Integer> broadcastCallback;
    private final BiConsumer<Byte, Integer> deliverCallback;
    private final Map<Byte, Compressor> fifoDelivered;

    public FIFOBroadcast(int port, int numHosts, Consumer<Integer> broadcastCallback, BiConsumer<Byte, Integer> deliverCallback) throws SocketException {
        this.broadcastCallback = broadcastCallback;
        this.deliverCallback = deliverCallback;
        this.fifoDelivered = new HashMap<>();
        this.broadcast = new UniformReliableBroadcast(port, numHosts, this::fifoDeliver);

        for (byte h = 0; h >= 0 && h < numHosts; h++) {
            Compressor compressed = new Compressor();
            // Use 0 as anchor for first message incoming (it will start from 1)
            compressed.add(0);
            fifoDelivered.put(h, compressed);
        }
     }

    public void load(int numMessages) {
        broadcast.load(numMessages);
    }

    private void fifoDeliver(Message message) {
        byte originId = message.getOrigin();
        Compressor compressedFromOrigin = fifoDelivered.get(originId);
        int lastDelivered = compressedFromOrigin.takeLast();

        compressedFromOrigin.add(message.getPayload());
        int lastToDeliver = compressedFromOrigin.takeLast(), prev = lastDelivered + 1;

        while (prev <= lastToDeliver) {
            if (originId == getMyHost()) {
                broadcastCallback.accept(prev);
            }
            deliverCallback.accept(originId, prev++);
        }
    }

    public void stopThreads() {
        broadcast.stopThreads();
    }

}
